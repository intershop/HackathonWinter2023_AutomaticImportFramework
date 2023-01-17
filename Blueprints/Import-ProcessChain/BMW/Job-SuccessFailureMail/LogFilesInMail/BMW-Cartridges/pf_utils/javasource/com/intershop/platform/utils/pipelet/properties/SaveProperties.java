package com.intershop.platform.utils.pipelet.properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.intershop.beehive.core.capi.cartridge.Cartridge;
import com.intershop.beehive.core.capi.cartridge.CartridgeMgr;
import com.intershop.beehive.core.capi.file.FileUtils;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * This pipelet is used to save the provided properties to the file system using the given path.
 * <p />
 * When a cartridge name is given the file will be stored relative to the according cartridge
 * directory. Otherwise it will be stored relative to the shared directory.
 * <p />
 * The error connector will be used when either the provided cartridge name or the provided path
 * is invalid.
 * 
 * @author JCMeyer
 * @version 1.0, 2015-08-03
 * @since 3.1.0
 */
public class SaveProperties extends Pipelet
{
    /**
     * Constant used to access the pipeline dictionary with key 'Properties'
     * 
     * The properties to be stored.
     */
    public static final String DN_PROPERTIES = "Properties";

    /**
     * Constant used to access the pipeline dictionary with key 'Path'
     * 
     * The path controlling where the property file will be stored.
     */
    public static final String DN_PATH = "Path";

    /**
     * Constant used to access the pipeline dictionary with key 'CartridgeName'
     * 
     * An optional cartridge name. When provided the file will be stored relative to the according cartridge directory.
     */
    public static final String DN_CARTRIDGE_NAME = "CartridgeName";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        Properties properties = dict.getRequired(DN_PROPERTIES);

        String path = dict.getRequired(DN_PATH);

        String cartridgeName = dict.getOptional(DN_CARTRIDGE_NAME);

        if (cartridgeName == null)
        {
            path = FileUtils.getSharedDirectory() + File.separator + path;
        }
        else
        {
            CartridgeMgr cartridgeMgr = NamingMgr.getManager(CartridgeMgr.class);
            Cartridge cartridge = cartridgeMgr.getCartridge(cartridgeName);
            if (cartridge == null)
            {
                Logger.error(this, "Unable to find cartridge for provided name '{}'.", cartridge);
                return PIPELET_ERROR;
            }
            path = cartridge.getCartridgeDirectory() + File.separator + path;
        }

        try
        {
            properties.store(new FileOutputStream(path), null);
        }
        catch (IOException e)
        {
            Logger.error(this, "Error saving properties to: '" + path + "'.", e);
            return PIPELET_ERROR;
        }

        return PIPELET_NEXT;
    }
}
