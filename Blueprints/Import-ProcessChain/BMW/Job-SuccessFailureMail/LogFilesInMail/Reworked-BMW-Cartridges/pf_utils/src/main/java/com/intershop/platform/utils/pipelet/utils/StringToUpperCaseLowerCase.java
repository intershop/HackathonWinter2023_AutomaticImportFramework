package com.intershop.platform.utils.pipelet.utils;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * Converts a string to uppercase and lowercase.
 * 
 * @author aseitz
 *
 */
public class StringToUpperCaseLowerCase extends Pipelet
{

    public static final String DN_INPUT_STRING = "InputString";
    
    public static final String DN_STRING_UPPER_CASE = "StringUpperCase";
    
    public static final String DN_STRING_LOWER_CASE = "StringLowerCase";
    
    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        String input = (String)dict.getOptional(DN_INPUT_STRING);
        
        if (input != null) {
            dict.put(DN_STRING_LOWER_CASE, input.toLowerCase());
            dict.put(DN_STRING_UPPER_CASE, input.toUpperCase());
        }

        return PIPELET_NEXT;
    }

}
