/**
 * 
 */
package com.intershop.platform.utils.pipelet.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.jayway.jsonpath.JsonPath;

/**
 * parse a JSON string to extract a value
 */

public class ParseJSONForValue extends Pipelet
{
    /**
     * Constant used to access the pipeline dictionary with key 'Key',
     * the key to parse for
     */
    private static String DN_KEY = "Key";
    
    /**
     * Constant used to access the pipeline dictionary with key 'Json',
     * the JSON to parse
     */
    private static String DN_JSON = "Json";
    
    /**
     * Constant used to access the pipeline dictionary with key 'Value',
     * the value parsed from JSON
     */
    private static String DN_VALUE = "Value";
    
    /**
     * Constant used to access the pipeline dictionary with key 'Contained',
     * true, if the value has been found.
     */
    private static String DN_IS_Contained = "Contained";
    
    

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        String jsonExp = (String) dict.getRequired(DN_KEY);
        String jsonString =  (String) dict.getRequired(DN_JSON);
        
        String pincodes = JsonPath.parse(jsonString).read(jsonExp);

        dict.put(DN_VALUE, pincodes );
        
        return PIPELET_NEXT;
    }

}



