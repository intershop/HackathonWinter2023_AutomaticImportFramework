package com.intershop.platform.utils.capi.template;

import com.intershop.beehive.core.capi.common.Provider;

/**
 * An interface used by a template module which declares the methods used inside
 * templates. A single implementation of TemplateToolsProvider registered under
 * the name TemplateToolsProvider.REGISTRY_NAME is used globally in the system.
 * 
 * <br />
 * 
 * Clients may create an implementation of this interface. To ensure
 * compatibility in future releases the class implementing this interface should
 * be a subclass of <code>TemplateToolsProviderImpl</code>.
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

public interface TemplateToolsProvider extends Provider
{
    /**
     * Constant used for NamingMgr.
     */
    static final public String REGISTRY_NAME = "TemplateToolsProvider";

    /**
     * Looks up a template tools implementation for the passed name. The
     * template tools must be located in the current server. There is no direct
     * access to a template tools of a remote server.
     * 
     * @param templateToolsName
     *            the template tools name
     * @return the template tools implementation or null if not found
     */
    public abstract TemplateTools lookupTemplateTools(String templateToolsName);

    /**
     * Registers a TemplateTools implementation.
     * 
     * @param name
     *            the TemplateTools name
     * @param className
     *            the TemplateTools implementation name
     */
    public abstract void registerTemplateTools(String name, String className);

    /**
     * Unregisters a TemplateTools implementation.
     * 
     * @param name
     *            the TemplateTools name
     */
    public abstract void unregisterTemplateTools(String name);
}
