package com.intershop.platform.utils.clazz.pipelet;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.orm.capi.common.ORMException;
import com.intershop.beehive.orm.internal.util.ClassUtils;

/**
 * Get a Java Class by its class or interface name. The error connector is used if the class cannot be found.
 */
public class GetClassByName extends Pipelet
{
    protected static String DN_CLASS_NAME = "ClassName";

    protected static String DN_CLASS = "Class";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        String className = aPipelineDictionary.getRequired(DN_CLASS_NAME);
        
        Class<?> clazz = null;
        try
        {
            clazz = ClassUtils.getClass(className);
        }
        catch(ORMException exORM)
        {
            return PIPELET_ERROR;
        }
        
        aPipelineDictionary.put(DN_CLASS, clazz);

        return PIPELET_NEXT;
    }
}
