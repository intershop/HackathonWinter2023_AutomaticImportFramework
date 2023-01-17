package com.intershop.platform.utils.pipelet.encode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

public class URLEncode extends Pipelet
{
    /**
     * Constant used to access the pipeline dictionary with key 'String'
     */
    public static final String DN_STRING = "String";
    /**
     * Constant used to access the pipeline dictionary with key 'EncodedString'
     */
    public static final String DN_ENCODED_STRING = "EncodedString";
    /**
     * Constant used to access the pipeline dictionary with key 'Encoding'
     */
    public static final String DN_ENCODING = "Encoding";
    /**
     * Constant used to access pipelet configuration with key 'Encoding'
     */
    public static final String CN_ENCODING = "Encoding";
    /**
     * Member attribute that holds the pipelet configuration value 'Encoding'
     */
    private String cfg_encoding = "";
    
    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        // lookup 'String' in pipeline dictionary
        String string = (String)dict.get(DN_STRING);

        if(string != null)
        {
            String encoding = (String)dict.get(DN_ENCODING);
            if(encoding == null)
            {
                encoding = cfg_encoding;
            }
            try 
            {
                String result = URLEncoder.encode(string, encoding);
                dict.put(DN_ENCODED_STRING, result);
            } 
            catch (UnsupportedEncodingException ex)
            {
                Logger.error(this.getClass().getName(), "Unknown encoding '%s'", encoding);
                throw new PipeletExecutionException("Unsupported encoding. Giving up.", ex);
            }
            
        }
        
        return PIPELET_NEXT;
    }
    
    public void init()
    {        
        // store 'Encoding' config value in field variable        
        cfg_encoding = (String)getConfiguration().get(CN_ENCODING);
        if(cfg_encoding == null)
        {
            cfg_encoding = System.getProperty("file.encoding");
        }

    }
}
