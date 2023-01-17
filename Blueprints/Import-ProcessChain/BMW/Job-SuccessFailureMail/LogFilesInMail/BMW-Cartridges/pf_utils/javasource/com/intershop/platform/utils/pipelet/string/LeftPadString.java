package com.intershop.platform.utils.pipelet.string;

import org.apache.commons.lang.StringUtils;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * Pads the left end of a provided string according to given parameters.
 * <p />
 * For further details see {@link StringUtils#leftPad(String, int, String)}.
 * 
 * @author jc.meyer@intershop.de
 * @version 1.0, 2018-02-02
 * @since 3.6.0
 *
 *        First basic functionality.
 */
public class LeftPadString extends Pipelet
{
    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        String input = dict.getRequired("Input");
        
        Integer size = dict.getRequired("Size");
        
        String pad = dict.getRequired("Pad");
        
        String output = StringUtils.leftPad(input, size, pad);
        
        dict.put("Output", output);
        
        return PIPELET_NEXT;
    }
}
