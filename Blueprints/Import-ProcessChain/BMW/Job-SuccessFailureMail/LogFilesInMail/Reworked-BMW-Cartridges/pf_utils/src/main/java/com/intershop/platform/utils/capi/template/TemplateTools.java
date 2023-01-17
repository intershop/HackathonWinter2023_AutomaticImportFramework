package com.intershop.platform.utils.capi.template;

/**
 * This defines the business interface methods for TemplatesTools as defined in
 * the object model.
 * 
 * This is the base interface for all TemplateTools.
 * 
 * <br />
 * 
 * Clients may create an implementation of this interface. To ensure
 * compatibility in future releases the class implementing this interface should
 * be a subclass of <code>TemplateToolsImpl</code>.
 * 
 * <br />
 * 
 * <b>History:</b> <br />
 * 
 * @author t.hartmann@intershop.de
 * @version 1.0, 07/20/2012
 * 
 * <br />
 * 
 */

public abstract interface TemplateTools
{
    /**
     * Returns the unique name of the class.
     * 
     * @return String The tools name.
     */
    public String getName();
}
