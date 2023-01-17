package com.intershop.platform.utils.pipelet.resource;

import java.util.ResourceBundle;

import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * This pipelet is used to clean up the cached resource bundle information for a class defined through the given class name.
 * <p />
 * The error connector is used in case no according class can be found.
 * 
 * @author JCMeyer
 * @version 1.0, 2015-08-03
 * @since 3.1.0
 */
public class RefreshResourceBundle extends Pipelet
{
    /**
     * Constant used to access the pipeline dictionary with key 'ClassName'
     * 
     * The class name defining the class to refresh the resource bundle for.
     */
    public static final String DN_CLASS_NAME = "ClassName";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        String className = dict.getRequired(DN_CLASS_NAME);

        Class<?> namedClass;
        try
        {
            namedClass = Class.forName(className);
            ClassLoader loader = namedClass.getClassLoader();
            ResourceBundle.clearCache(loader);
        }
        catch (ClassNotFoundException e)
        {
            Logger.error(this, "Unable to find class for given class name.", e);
            return PIPELET_ERROR;
        }

        return PIPELET_NEXT;
    }
}
