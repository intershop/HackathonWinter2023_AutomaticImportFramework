package com.intershop.platform.utils.dbinit.preparer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.intershop.beehive.core.capi.cartridge.Cartridge;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.dbinit.capi.Preparable;
import com.intershop.beehive.core.dbinit.capi.PreparerConfig;
import com.intershop.platform.utils.capi.properties.ExistingDomainPropertiesFilter;

import serp.bytecode.BCClass;
import serp.bytecode.Project;

/**
 * This abstract preparer implementation can be used to build preparer wrappers which do process static resource bundle
 * information provided through DBInit properties before passing it to the original prepares. This allows to apply any
 * kind of transformation or dynamic creation of the otherwise rather limited preparer input data. it further allows to
 * optionally proved a {@link Predicate} based filter to remove unwanted configuration records by respective criteria
 * before actually passing the data to the original preparer.
 * <p>
 * To implement what has been described above let your custom preparer extend this abstract preparer and make sure to
 * implement at least {@link #buildConfigurationRecords(Properties)}. If this isn't sufficient for a specific use case,
 * consider replacing the default implementation of {@link #processBundleStream(InputStream, Predicate)}. It allows for
 * full resource stream level transformation.
 * <p>
 *  See <code>com.intershop.adapter.epaas.dbinit.preparer</code> for {@link AbstractBundleProcessingPreparer}
 *  based preparer implementations - cartridge {@code ac_epaas}.
 *  <p>
 *  See {@link ExistingDomainPropertiesFilter} for an example of a properties filter to be optionally passed to the
 *  constructor of this abstract preparer.
 * 
 * @author JCMeyer
 *
 */
public abstract class AbstractBundleProcessingPreparer implements Preparable
{
    public final static String MULTIPLE_VALUE_DELIMITER = "\\|";
    
    private Class<?> preparerClass = null;
    
    private Predicate<Properties> configurationRecordFilter = null;
    
    private Preparable delegate = null;
    
    public AbstractBundleProcessingPreparer(Class<?> preparerClass)
    {
        this(preparerClass, null);
    }
    
    public AbstractBundleProcessingPreparer(Class<?> preparerClass, Predicate<Properties> configurationRecordFilter)
    {
        this.preparerClass = preparerClass;
        this.configurationRecordFilter = configurationRecordFilter;
        
        try
        {
            Class<?> loaded = new Loader().loadClass(preparerClass.getName());
            delegate = (Preparable) loaded.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            Logger.error(this,"Error instantiating preparer delegate for class '{}'. Reason: {}", preparerClass, ex);
        }
    }
    
    /**
     * This in-line {@link ClassLoader} implementation is responsible for injecting the custom resource stream loading
     * functionality which applies the transformation mechanism as implemented by the respective preparer extending the
     * {@link AbstractBundleProcessingPreparer}. 
     *
     */
    protected class Loader extends ClassLoader
    {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException
        {
            Class<?> loadedClass = null;
            
            if (name.equals(preparerClass.getName()))
            {
                BCClass bcc = new Project().loadClass(preparerClass);
                byte[] classData = bcc.toByteArray();
                loadedClass =  defineClass(name, classData, 0, classData.length);
            }
            else
            {
                loadedClass =  super.loadClass(name);
            }
            
            return loadedClass;
        }
        
        @Override
        public InputStream getResourceAsStream(String name)
        {
            InputStream bundleStream = super.getResourceAsStream(name);
            Logger.debug(this, "Processing original bundle stream read from '{}',", name);
            bundleStream = processBundleStream(bundleStream, configurationRecordFilter);
            return bundleStream;
        }
    }
    
    @Override
    public boolean prepare()
    {
        Logger.debug(this, "Starting preparation using class loader: {}", delegate.getClass().getClassLoader());
        return delegate.prepare();
    }
    
    @Override
    public boolean checkParameters()
    {
        return delegate.checkParameters();
    }
    
    @Override
    public PreparerConfig getConfiguration()
    {
        return delegate.getConfiguration();
    }
    
    @Override
    public boolean migrate()
    {
        return delegate.migrate();
    }
    
    @Override
    public void setCartridge(Cartridge cartridge)
    {
        delegate.setCartridge(cartridge);
    }
    
    @Override
    public void setConfiguration(PreparerConfig config)
    {
        delegate.setConfiguration(config);
    }
    
    @Override
    public void setParameters(String[] params)
    {
        delegate.setParameters(params);
    }
    
    @Override
    public boolean useImplicitTransaction()
    {
        return delegate.useImplicitTransaction();
    }
    
    /**
     * The method is expected to read from the given properties, apply any kind of transformation on them and
     * afterwards return the results as a {@link Collection} of {@link Properties} compatible to the respective
     * preparer.
     * 
     * @param allServiceConfigurationProperties
     * @return a collection of properties compatible to the respective preparer
     */
    protected abstract Collection<Properties> buildConfigurationRecords(Properties allServiceConfigurationProperties);

    /**
     * A default implementation processing the original bundle stream. It builds a {@link Properties} object from the
     * stream and passes it to abstract method {@link #buildConfigurationRecords(Properties)}. The method is expected
     * to read from the given properties, apply any kind of transformation on them and afterwards return the results
     * as a {@link List} of {@link Properties}.
     * <p>
     * All returned {@link Properties} are then converted back to a single input stream. Its contents are expected to
     * be suitable for usage by the respective preparer {@link Class} as passed to the constructor of this abstract
     * {@link Preparable}.
     * <p>
     * In case a different implementation - not calling method {@link #buildConfigurationRecords(Properties)} - should
     * be req2uired - feel free to override this default in your actual preparer implementation based on this abstract
     * {@link Preparable}.
     * 
     * @param originalBundleStream
     * @param configurationRecordFilter
     * @return
     */
    protected InputStream processBundleStream(InputStream originalBundleStream, Predicate<Properties> configurationRecordFilter)
    {
        Collection<Properties> configurationRecords = Collections.<Properties>emptyList();
        InputStream substitutedInputStream = null;
        try
        {
            Properties allConfigurationProperties = new Properties();
            allConfigurationProperties.load(originalBundleStream);
            configurationRecords = buildConfigurationRecords(allConfigurationProperties);
        }
        catch (IOException ex)
        {
            Logger.error(this, "Error processing original bundle stream.", ex);
        }
        
        if (configurationRecordFilter != null)
        {
            configurationRecords = Collections2.filter(configurationRecords, configurationRecordFilter);
        }
        
        try (ByteArrayOutputStream substitutionOutputStream = new ByteArrayOutputStream())
        {
            for (Properties configurationRecord : configurationRecords)
            {
                configurationRecord.store(substitutionOutputStream, null);
            }
            substitutionOutputStream.close();
            substitutedInputStream = new ByteArrayInputStream(substitutionOutputStream.toByteArray());
        }
        catch (IOException ex)
        {
            Logger.error(this, "Error writing processed bundle stream.", ex);
        }
        
        if (Logger.isDebugEnabled())
        {
            Logger.debug(this, "Processing of bundle stream resulted in the below output sent to {}:", preparerClass);
            try (BufferedReader isr = new BufferedReader(new InputStreamReader(substitutedInputStream)))
            {
                substitutedInputStream.mark(Integer.MAX_VALUE);
                while (isr.ready())
                {
                    Logger.debug(this, "> {}", isr.readLine());
                }
                substitutedInputStream.reset();
            }
            catch (IOException e)
            {
            }
        }
        
        return substitutedInputStream;
    }
    
    /**
     * Determines the number of configuration records available in the given properties. Make sure to pass a guaranteed
     * property name available at all records. Example below has two records:
     * <pre>
     * ServiceConfiguration.1.ServiceDefinitionID
     * ServiceConfiguration.1.Activated
     * 
     * ServiceConfiguration.2.ServiceDefinitionID
     * ServiceConfiguration.2.Activated
     * </pre>
     *
     * @param properties the properties to count the number of records in
     * @param configurationName configuration name in above example is {@code ServiceConfiguration}
     * @param propertyName property name  in above example is {@code ServiceDefinitionID} or {@code Activated}
     * 
     * @return the number of configuration records 
     */
    protected int countConfigurationRecords(Properties properties, String configurationName, String propertyName)
    {
        int recordPos = 1;
        for (; properties.containsKey(configurationName + "." + recordPos + "." + propertyName); recordPos++);
        return recordPos - 1;
    }
    
    /**
     * Builds a new single configuration record based on the given configuration properties and name. It reads all
     * given property names from the given read index and writes them to the given write index. The new record can
     * hold a different position information when write index differs from read index.
     * <p>
     * Properties not available according to the given look-up parameters will be ignored silently and therefore
     * not being written to the built configuration record.
     * 
     * @param configurationProperties
     * @param configurationName
     * @param propertyNames
     * @param readIndex
     * @param writeIndex
     * 
     * @return a new single configuration record
     */
    protected Properties buildConfigurationRecord(Properties configurationProperties, String configurationName, String[] propertyNames, int readIndex, int writeIndex)
    {
        Properties configurationRecord = new Properties();
        
        for (String propertyName : propertyNames)
        {
            String readKey = buildIndexedConfigurationPropertyKey(configurationName, readIndex, propertyName);
            String value = configurationProperties.getProperty(readKey);
            if (null != value)
            {
                String writeKey = buildIndexedConfigurationPropertyKey(configurationName, writeIndex, propertyName);
                configurationRecord.setProperty(writeKey, value);
            }
        }
        
        return configurationRecord;
    }
    
    /**
     * Builds an indexed configuration property key based on the given parameters.
     * <p>
     * Example: {@code ServiceConfiguration.1.ServiceDefinitionID}
     * 
     * @param configurationName
     * @param propertyName
     * @param index
     * 
     * @return
     */
    protected String buildIndexedConfigurationPropertyKey(String configurationName, int index, String propertyName)
    {
        return configurationName + "." + index + "." + propertyName;
    }
}
