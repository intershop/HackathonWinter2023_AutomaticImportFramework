package com.intershop.platform.utils.dbinit.preparer;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.domain.DomainMgr;
import com.intershop.beehive.core.capi.environment.ORMMgr;
import com.intershop.beehive.core.capi.localization.LocaleInformation;
import com.intershop.beehive.core.capi.localization.LocaleMgr;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.dbinit.capi.Preparer;
import com.intershop.beehive.foundation.util.PropertyUtils;
import com.intershop.component.service.internal.service.ServiceConfigurationPO;
import com.intershop.component.service.internal.service.ServiceConfigurationPOAlternateKey;
import com.intershop.component.service.internal.service.ServiceConfigurationPOFactory;
import com.intershop.component.service.internal.service.ServiceDefinitionKeyPO;
import com.intershop.component.service.internal.service.ServiceDefinitionKeyPOAlternateKey;
import com.intershop.component.service.internal.service.ServiceDefinitionKeyPOFactory;
import com.intershop.component.service.internal.service.ServiceRepositoryPO;
import com.intershop.component.service.internal.service.ServiceRepositoryPOAlternateKey;
import com.intershop.component.service.internal.service.ServiceRepositoryPOFactory;

/**
 * This preparer class is taken from ICM 7.7.0 and contains the following adjustments:
 * <ul>
 *   <li>ensured Java 7 compatibility</li>
 *   <li>improved overall code quality - a lot of room was available</li>
 *   <li>added support for parameter type 'Text'</li>
 *   <li>treating it a warning instead of error if service configuration doesn't exist</li>
 *   <li>replaced misleading error message around preparation global try-catch be something descriptive</li>
 *   <li>not asking {@link ClassLoader} for a resource stream - reads properties only via {@link ResourceBundle#getBundle(String)}</li>
 * </ul>
 * 
 * This preparer adds/updates parameters to/of an existing service configuration.
 * 
 * It requires a property file with the following content:
 * 
 * The first 3 lines identify the ServiceConfiguration:
 * ServiceConfiguration.{Number}.ServiceDefinitionID={DefinitionID}
 * ServiceConfiguration.{Number}.Name={Name}
 * ServiceConfiguration.{Number}.DomainName={DomainName}
 * 
 * The next 3 lines represents one parameter. You can add here more parameters
 * 
 * ServiceConfiguration.{Number}.Parameter.{ParameterNumber}.Name={ParameterName}
 * ServiceConfiguration.{Number}.Parameter.{ParameterNumber}.Value={ParameterValue}
 * ServiceConfiguration.{Number}.Parameter.{ParameterNumber}.Type={ParameterType}
 * 
 * ... add more parameters here
 * 
 * example: 
 * 
 * ServiceConfiguration.1.ServiceDefinitionID=SmtpMailService
 * ServiceConfiguration.1.Name=SmtpMailService
 * ServiceConfiguration.1.DomainName=Primetech

 * ServiceConfiguration.1.Parameter.1.Name=testName
 * ServiceConfiguration.1.Parameter.1.Value=testValue
 * ServiceConfiguration.1.Parameter.1.Type=String

 * ServiceConfiguration.1.Parameter.2.Name=testBooleanName
 * ServiceConfiguration.1.Parameter.2.Value=true
 * ServiceConfiguration.1.Parameter.2.Type=Boolean
 *
 */
public class ServiceConfigurationParameterPreparer extends Preparer
{
    @Inject
    protected ORMMgr ormMgr;

    @Inject
    protected LocaleMgr localeMgr;

    @Inject
    protected DomainMgr domainMgr;

    @Inject
    protected ServiceConfigurationPOFactory serviceConfigurationPOFactory;

    @Inject
    protected ServiceDefinitionKeyPOFactory serviceDefinitionKeyPOFactory;

    @Inject
    protected ServiceRepositoryPOFactory serviceRepositoryPOFactory;

    private final static String SERVICE_CONFIGURATION_PREFIX = "ServiceConfiguration";
    private final static String SERVICE_CONFIGURATION_SERVICE_DEFINITION_ID = "ServiceDefinitionID";
    private final static String SERVICE_CONFIGURATION_PARAMETER = "Parameter";
    private final static String SERVICE_CONFIGURATION_PARAMETER_NAME = "Name";
    private final static String SERVICE_CONFIGURATION_PARAMETER_VALUE = "Value";
    private final static String SERVICE_CONFIGURATION_PARAMETER_TYPE = "Type";
    private final static String SERVICE_CONFIGURATION_NAME = "Name";
    private final static String SERVICE_CONFIGURATION_DOMAIN_NAME = "DomainName";

    // data properties file
    private String propertyFile;

    // localization data properties file
    private String propertyLocalizationFile;

    private final static String SERVICE_PREFIX = SERVICE_CONFIGURATION_PREFIX + ".";
    private final static String SERVICE_SUFFIX = "." + SERVICE_CONFIGURATION_NAME;

    @Override
    public boolean checkParameters()
    {
        switch(getNumberOfParameters())
        {
            case 1:
                propertyFile = getParameter(0);
                break;
            case 2:
                propertyFile = getParameter(0);
                propertyLocalizationFile = getParameter(1);
                break;
            default:
                Logger.error(this, "Wrong number of parameters set: '{}'. Expected 1 or 2 file parameters.", getNumberOfParameters());
                return false;
        }
        
        return true;
    }

    @Override
    public boolean prepare()
    {
        boolean success = true;
        
        try
        {
            // create the resource bundle from the property file
            ResourceBundle resourceBundle = ResourceBundle.getBundle(propertyFile);
            Set<String> serviceKeyPlaceholders = PropertyUtils.getGroupPlaceholdersForBundle(resourceBundle, SERVICE_PREFIX, SERVICE_SUFFIX, 1);

            // iterate over the configurations, the next configuration is tried if one configuration has an error
            for (Iterator<String> ps = serviceKeyPlaceholders.iterator(); ps.hasNext(); success |= preparePlaceholder(resourceBundle, ps.next()));

            // check if a localization data file is set
            if (propertyLocalizationFile != null)
            {
                ormMgr.getORMEngine().getTransactionManager().getCurrentTransaction().store();
                for (Iterator<LocaleInformation> locales = localeMgr.createLocaleIterator(); locales.hasNext(); updateLocalization(locales.next()));
            }
        }
        catch (MissingResourceException ex)
        {
            success = false;
            Logger.error(this, "Error preparing configuration parameters described in '{}'. Reason: {}", propertyFile, ex);
        }

        return success;
    }

    @Override
    public boolean migrate()
    {
        return prepare();
    }

    /**
     * @param resourceBundle
     * @param placeholder
     * @return <code>true</code> if preparation of placeholder was successful, otherwise <code>false</code>
     */
    protected boolean preparePlaceholder(final ResourceBundle resourceBundle, final String placeholder)
    {
        String prefix = SERVICE_PREFIX + placeholder + ".";

        // Get the two values needed to create the alternate key for ServiceDefinitionKeyPO, so we can
        // get the UUID we need for the serviceDefinitionKeyID value of the ServicePermissionPO.
        String serviceDefinitionID = resourceBundle.getString(prefix + SERVICE_CONFIGURATION_SERVICE_DEFINITION_ID);
        String serviceDefinitionName = getServiceDefinitionName(resourceBundle, prefix);

        String domainName = getDomainName(resourceBundle, prefix);
        Domain domain = domainMgr.getDomainByName(domainName);
        if (domain == null)
        {
            Logger.error(this, "Service definition domain name '{}' not found.", domainName);
            return false;
        }

        ServiceRepositoryPO repository = serviceRepositoryPOFactory.getObjectByAlternateKey(new ServiceRepositoryPOAlternateKey(domain.getUUID()));
        if (repository == null)
        {
            repository = serviceRepositoryPOFactory.create(domain, domain.getUUID());
            ormMgr.getORMEngine().getTransactionManager().getCurrentTransaction().store();
        }

        if (checkForValidPreparerConfiguration(prefix, serviceDefinitionID, serviceDefinitionName, domainName, domain))
        {
            ServiceConfigurationPOAlternateKey configKey = new ServiceConfigurationPOAlternateKey(domain.getUUID(), serviceDefinitionName);
            ServiceConfigurationPO config = serviceConfigurationPOFactory.getObjectByAlternateKey(configKey);
            if (config != null)
            {
                // get the name, value, and type of additional payment
                // service configuration parameters, if available
                String paramPrefix = prefix + SERVICE_CONFIGURATION_PARAMETER + ".";
                String paramSuffix = "." + SERVICE_CONFIGURATION_PARAMETER_NAME;

                Set<String> paramGroupingPlaceholders = PropertyUtils.getGroupPlaceholdersForBundle(resourceBundle, paramPrefix, paramSuffix, 3);
                // create the ServiceConfiguration parameters
                for (String paramGroupingPlaceholder : paramGroupingPlaceholders)
                {
                    if (! prepareServiceConfigurationParameter(resourceBundle, config, paramPrefix, paramGroupingPlaceholder))
                    {
                        return false;
                    }
                }
            }
            else
            {
                Logger.warn(this, "Service configuration {} in domain {} does not exist - skipping.", serviceDefinitionName, domain.getDomainName());
                return true;
            }
        }
        else
        {
            Logger.error(this, "Error in configuration {}, continue with the next configuration.", placeholder);
            return false;
        }
        
        return true;
    }

    /**
     * Check if all preparer configuration values are available.
     * 
     * @param prefix the service prefix (not checked)
     * @param serviceDefinitionID
     * @param name
     * @param domainName
     * @param domain
     * 
     * @return <code>true</code> if configuration is complete or <code>false</code> otherwise
     */
    protected boolean checkForValidPreparerConfiguration(String prefix, String serviceDefinitionID, String name, String domainName, Domain domain)
    {
        boolean result = true;
        if (serviceDefinitionID == null)
        {
            Logger.error(this, "Service definition id with key '{}{}' not found.", prefix,
                            SERVICE_CONFIGURATION_SERVICE_DEFINITION_ID);
            result = false;
        }

        if (name == null)
        {
            Logger.error(this, "Name with key '{}{}' not found.", prefix, SERVICE_CONFIGURATION_NAME);
            result = false;
        }

        if (domainName == null)
        {
            Logger.error(this, "Domain name with key '{}{}' not found.", prefix, SERVICE_CONFIGURATION_DOMAIN_NAME);
            result = false;
        }
        
        if (domain == null)
        {
            Logger.error(this, "Domain with name {} not found.", domainName);
            result = false;
        }
        
        return result;
    }

    protected boolean prepareServiceConfigurationParameter(final ResourceBundle resourceBundle, ServiceConfigurationPO config, String paramPrefix, String paramGroupingPlaceholder)
    {
        boolean success = true;
        
        String prefix = paramPrefix + paramGroupingPlaceholder + ".";
        
        String paramValue = getResourceString(resourceBundle, prefix + SERVICE_CONFIGURATION_PARAMETER_VALUE);
        if (null != paramValue)
        {
            String paramName = getResourceString(resourceBundle, prefix + SERVICE_CONFIGURATION_PARAMETER_NAME);
            String paramType = getResourceString(resourceBundle, prefix + SERVICE_CONFIGURATION_PARAMETER_TYPE);
            Logger.debug(this, "Service configuration new parameter value: {}:{}={} ", paramName, paramType, paramValue);

            if (paramType.equalsIgnoreCase("String"))
            {
                config.putString(paramName, paramValue);
            }
            else if (paramType.equalsIgnoreCase("Boolean"))
            {
                config.putBoolean(paramName, Boolean.valueOf(paramValue));
            }
            else if (paramType.equalsIgnoreCase("Integer"))
            {
                config.putInteger(paramName, Integer.valueOf(paramValue));
            }
            else if (paramType.equalsIgnoreCase("Text"))
            {
                config.putText(paramName, paramValue);
            }
            else
            {
                Logger.error(this, "Unsupported value type '{}' for service configuration key '{}'.", paramType, prefix + SERVICE_CONFIGURATION_PARAMETER_TYPE);
                success = false;
            }
        }
        
        return success;
    }

    /**
     * Get resource string from resource bundle or return <code>null</code> if not found
     *
     * @param bundle the bundle
     * @param key the key to use
     * 
     * @return the value or null if not found
     */
    protected String getResourceString(ResourceBundle bundle, String key)
    {
        try
        {
            return bundle.getString(key).trim();
        }
        catch (MissingResourceException misEx)
        {
            return null;
        }
    }

    protected void updateLocalization(LocaleInformation locale)
    {
        ResourceBundle bundle = getResourceBundle(this.propertyLocalizationFile, locale.getJavaLocale());
        if (bundle == null)
        {
            // no logging, base class does it!
            return;
        }

        for (String key : Collections.list(bundle.getKeys()))
        {
            StringTokenizer tokenizer = new StringTokenizer(key, ".");
            String serviceDefinitionID = tokenizer.nextToken();
            String serviceCartridgeID = tokenizer.nextToken();
            String configDomainName = tokenizer.nextToken();

            if (StringUtils.isBlank(serviceDefinitionID) || StringUtils.isBlank(serviceCartridgeID) || StringUtils.isBlank(configDomainName))
            {
                Logger.warn(this, "Service configuration key (ServiceDefinitionID.CartridgeID.DomainName): '{}' not valid.", key);
                continue;
            }

            Domain domain = domainMgr.getDomainByName(configDomainName);
            if (null == domain)
            {
                Logger.warn(this, "Could not find the service definition domain.");
                continue;
            }

            ServiceDefinitionKeyPOAlternateKey defAK = new ServiceDefinitionKeyPOAlternateKey(serviceDefinitionID, serviceCartridgeID);
            ServiceDefinitionKeyPO defPO = serviceDefinitionKeyPOFactory.getObjectByAlternateKey(defAK);

            if (null == defPO)
            {
                Logger.warn(this, "Could not find service definition by serviceID and cartridgeID.");
                continue;
            }

            @SuppressWarnings("unchecked")
            Collection<ServiceConfigurationPO> serviceConfigurations = serviceConfigurationPOFactory.getObjectsBySQLWhere("servicedefinitionkeyid=? and domainid=?",
                                new String[] { defPO.getUUID(), domain.getDomainID() });

            if (null == serviceConfigurations)
            {
                Logger.warn(this, "Could not find service configuration: '{}' to create service configuration localization for.", key);
                continue;
            }

            ServiceConfigurationPO serviceConfigurationPO = null;
            for (ServiceConfigurationPO po : serviceConfigurations)
            {
                serviceConfigurationPO = po;
            }

            tokenizer = new StringTokenizer(bundle.getString(key), ";");
            String displayName = null, description = null;
            switch (tokenizer.countTokens())
            {
                case 1:
                    displayName = tokenizer.nextToken();
                    break;
                case 2:
                    displayName = tokenizer.nextToken();
                    description = tokenizer.nextToken();
                    break;
                default:
                    Logger.warn(this, "Unexpected number of tokens ({}) for key '{}'.", tokenizer.countTokens(), key);
                    break;
            }

            if (StringUtils.isNotBlank(displayName))
            {
                serviceConfigurationPO.setDisplayName(displayName, locale);
            }

            if (StringUtils.isNotBlank(description))
            {
                serviceConfigurationPO.setDescription(description, locale);
            }
        }
    }

    protected String getServiceDefinitionName(final ResourceBundle resourceBundle, String prefix)
    {
        return resourceBundle.getString(prefix + SERVICE_CONFIGURATION_NAME);
    }

    protected String getDomainName(final ResourceBundle resourceBundle, String prefix)
    {
        return resourceBundle.getString(prefix + SERVICE_CONFIGURATION_DOMAIN_NAME);
    }

}
