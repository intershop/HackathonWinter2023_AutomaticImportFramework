package com.intershop.platform.utils.pipelet.common;

import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;

/**
* Pipelet reads the value of a key stored under key "MappedEntry" 
* and stores it as form value under the name given in "KeyNameHolder"
* 
* @author t.hofbeck@intershop.de
* @version 1.0, 2017-10-19
* @since 3.6.0
*/
public class SetIndirectDictFormValue extends Pipelet
{
    public static final String IO_KEYNAMEHOLDER_NAME = "KeyNameHolder";
    public static final String IO_MAPPEDENTRY_NAME = "MappedEntry";

    public void init() throws PipelineInitializationException, SystemException
    {
    }

    public int execute(PipelineDictionary dict) throws PipeletExecutionException, SystemException
    {
        String keyNameHolder = (String)dict.get("KeyNameHolder");
        if (keyNameHolder == null)
        {
            throw new PipeletExecutionException(
                            "Mandatory input parameter 'KeyNameHolder' not available in pipeline dictionary.");
        }

        dict.setFormValue(keyNameHolder, (String)dict.get("MappedEntry"));

        return 1;
    }
}
