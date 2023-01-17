package com.intershop.platform.utils.internal.template;

import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.foundation.util.ConHashMap;
import com.intershop.platform.utils.capi.template.TemplateTools;
import com.intershop.platform.utils.capi.template.TemplateToolsProvider;

/**
 * The TemplateTools provider is responsible for looking up template tools
 * resources in the system.
 *  
 * <br />
 * 
 * <b>History:</b> <br />
 * 
 * @author t.hartmann@intershop.de
 * @version 1.0, 07/20/2012
 * @comment basic functionality.
 * 
 * <br />
 * 
 */

public class TemplateToolsProviderImpl implements TemplateToolsProvider
{

    /**
     * The object caches used by the TemplateToolsProvider to cache created
     * TemplateTools objects.
     */
    private ConHashMap templateToolsInstanceCache;
    private ConHashMap templateToolsNameCache;

    /**
     * The simple constructor to initialize the caches.
     */
    public TemplateToolsProviderImpl() throws SystemException
    {
        templateToolsInstanceCache = new ConHashMap();
        templateToolsNameCache = new ConHashMap();
    }
    
    /**
     * Looks up a template tools implementation for the passed name. The
     * template tools must be located in the current server. There is no direct
     * access to a template tools of a remote server.
     * 
     * @param templateToolsName
     *            The template tools name.
     * @return The template tools implementation or null if not found.
     */
    @Override
    @SuppressWarnings("unchecked")
    public TemplateTools lookupTemplateTools(String templateToolsName)
    {
        // double check to avoid synchronized
        TemplateTools templateTools = (TemplateTools)templateToolsInstanceCache.get(templateToolsName);
        if (templateTools == null)
        {
            synchronized(this)
            {
                templateTools = (TemplateTools)templateToolsInstanceCache.get(templateToolsName);
                if (templateTools == null)
                {
                    String className = (String)templateToolsNameCache.get(templateToolsName);
                    if (className != null)
                    {
                        Class clazz = getClass(className);
                        if (clazz != null)
                        {
                            // force a cast to ensure it's really a template tools class
                            templateTools = (TemplateTools)createInstance(clazz);
                            if (templateTools != null)
                            {
                                templateToolsInstanceCache.put(templateToolsName, templateTools);
                            }
                        }
                    }
                }
            }
        }

        return templateTools;
    }

    /**
     * Registers a TemplateTools implementation.
     * 
     * @param name
     *            The TemplateTools name.
     * @param className
     *            The TemplateTools implementation name.
     */
    @Override
    public void registerTemplateTools(String name, String className)
    {
        // We cannot immediately create an instance of the TemplateTools,
        // because maybe they have a static initializer in which other
        // TemplateTools are accessed. Delay instance creation until all
        // TemplateTools have been registered.
        synchronized(this)
        {
            templateToolsNameCache.put(name, className);
            templateToolsInstanceCache.remove(name);
        }
        Logger.debug(this, "The TemplateTools '{}' has been registered at the "
                        + "TemplateToolsProvider instance with the class '{}'.", name, className);
    }

    /**
     * Unregisters a TemplateTools implementation.
     * 
     * @param name
     *            The TemplateTools name.
     */
    @Override
    public void unregisterTemplateTools(String name)
    {
        synchronized(this)
        {
            templateToolsNameCache.remove(name);
            templateToolsInstanceCache.remove(name);
        }
    }

    /**
     * Loads the class for the passed class name. Returns the class object if no
     * exception occurred, otherwise null.
     * 
     * @param className
     *            The name of the class to load.
     * @return The loaded class or <code>null</code> if an
     *         <code>java.lang.ClassNotFoundException</code> occurred.
     */

    private Class getClass(String className)
    {
        try
        {
            return getClass().getClassLoader().loadClass(className);
        }
        catch(ClassNotFoundException ex)
        {
            return null;
        }
    }

    /**
     * Creates an instance of the passed class. Returns this instance if no
     * exception occurred, otherwise null.
     * 
     * @param objectClass
     *            the class from that an instance is created
     * @return the created object or <code>null</code> if an exception occurred
     */

    private Object createInstance(Class<TemplateTools> objectClass)
    {
        Object object = null;

        try
        {
            object = objectClass.newInstance();
        }
        catch(InstantiationException ex)
        {
            Logger.debug(this, "An exception occured during creating a class instance: {}", ex);
        }
        catch(IllegalAccessException ex)
        {
            Logger.debug(this, "An exception occured during creating a class instance: {}", ex);
        }

        return object;
    }

}
