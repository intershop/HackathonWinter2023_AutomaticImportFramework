package com.intershop.platform.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.intershop.beehive.core.capi.cartridge.Cartridge;
import com.intershop.beehive.core.capi.cartridge.CartridgeMgr;
import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.environment.LifecycleListenerException;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.platform.utils.capi.template.TemplateToolsProvider;

public class PfUtilsCartridge extends Cartridge
{
    /**
     * Initializes the cartridge with values from a template tools properties
     * object, if the cartridge has not been initialized yet.
     * 
     * @param properties
     *            A properties object.
     */
    @Override
    public void onInitHook(Properties properties) throws LifecycleListenerException
    {
        super.onInitHook(properties);
        // register all TemplateTools for the cartridge
        registerTemplateTools();
    }

    /**
     * Registers all Template Tools of the Cartridge at the NamingMgr instance.
     * This will be done by loading the Provider class assignments from the
     * file: /resources/naming/providers.properties which is located in the JAR
     * file of the Cartridge. Afterwards each Provider class is registered with
     * its name at the NamingMgr instance.
     */
    @SuppressWarnings("unchecked")
    public void registerTemplateTools()
    {
        TemplateToolsProvider templateToolsProvider = (TemplateToolsProvider)NamingMgr.getInstance().lookupProvider(
                        TemplateToolsProvider.REGISTRY_NAME);

        CartridgeMgr cartridgeMgr = (CartridgeMgr)NamingMgr.getInstance().lookupManager(CartridgeMgr.REGISTRY_NAME);

        Map<String, String> templatetools = new HashMap<String, String>();
        // loop all cartridges and take the templatetools entries of the last found file
        // get cartridges to load iterator returns all cartridges of the cartridgelist
        Iterator<String> iter = cartridgeMgr.createCartridgesToLoadIterator();
        while (iter.hasNext())
        {
            String cartridge = iter.next();
            String fileName = "resources/" + cartridge + "/naming/templatetools.properties";
            Hashtable<String, String> result = loadClassAssignments(fileName);
            if ((result != null) && (!result.isEmpty())) {
                templatetools.putAll(result);
            }
        }

        if ((templatetools == null) || (templatetools.isEmpty()))
        {
            Logger.debug(this, "No templatetools found for cartridge '{}'.", getName());
            return;
        }

        // register all TemplateTools of the cartridge at the
        // TemplateToolsProvider
        for (String templatetoolsName : templatetools.keySet()) {
            String templatetoolsClassName = templatetools.get(templatetoolsName);
            templateToolsProvider.registerTemplateTools(templatetoolsName.trim(), templatetoolsClassName.trim());
        }        
    }

    /**
     * This method loads a property file containing name - class name pairs from
     * the given resource location.
     * 
     * @return A properties object with the name mappings or <code>null</code>,
     *         if it wasn't found.
     */
    private Hashtable loadClassAssignments(String fileName)
    {
        try
        {
            Properties result = new Properties();
            InputStream istream = getResourceStream(fileName);
            if (istream != null)
            {
                result.load(istream);
            }
            else
            {
                Logger.debug(this, "The resource file '{}' for the cartridge '{}" + "' was not found or is empty.",
                                fileName, getName());
            }
            return result;
        }
        catch(IOException ex)
        {
            throw new SystemException(ex);
        }
    }
}
