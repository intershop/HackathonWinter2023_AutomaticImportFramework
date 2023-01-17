package com.intershop.platform.utils.pipelet.net;

import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Pipelet calls a given URL and returns an input stream.
 * <p>
 * <b> History: </b> <br />
 *
 * @author h.grasser@intershop.de <br />
 * @version 1.0, 02/09/2011 <br />
 * @comment basic functionality. <br />
 */
public class CallURL extends Pipelet
{

    /**
     * Constant used to access the pipeline dictionary with key 'URL'
     */
    public static final String DN_URL = "URL";
    /**
     * Constant used to access the pipeline dictionary with key 'ProductStream'
     */
    public static final String DN_INPUT_STREAM = "InputStream";
    /**
     * Constant used to access the pipeline dictionary with key 'FileName'
     */
    public static final String DN_FILE_NAME = "FileName";
    public static final String CN_CLOSE_ON_SUCCESS = "CloseOnSuccess";
    protected Object cfg_closeOnSuccess;

    /**
     * The pipelet's execute method is called whenever the pipelets gets
     * executed in the context of a pipeline and a request. The pipeline
     * dictionary valid for the currently executing thread is provided as a
     * parameter.
     * 
     * @param dict
     *            The pipeline dictionary to be used.
     * @throws PipeletExecutionException
     *             Thrown in case of severe errors that make the pipelet execute
     *             impossible (e.g. missing required input data).
     */
    public int execute(PipelineDictionary dict)
    throws PipeletExecutionException {        
        // lookup 'URL' in pipeline dictionary
        URL urlObject  = null;     // url
        String uRL = (String)dict.get(DN_URL);
        URLConnection       urlConnection;          // url connection
        InputStream stream;
        if (null == uRL)
        {
            throw new PipeletExecutionException("Mandatory input parameter 'URL' not available in pipeline dictionary.");
        }

        // create URL object
        try
        {
            urlObject = new URL(uRL);
        }
        catch (MalformedURLException mue)
        {
            Logger.error(this, "Error at URL creation.", mue);
            return Pipelet.PIPELET_ERROR;
        }

        
        // create a URL connection
        try
        {
            urlConnection = urlObject.openConnection();
        }
        catch (IOException ioe1)
        {
            Logger.error(this, "Error at creating url connection.", ioe1);
            return Pipelet.PIPELET_ERROR;
        }
        
        // set connecton parameters
        urlConnection.setAllowUserInteraction(false);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(false);
        urlConnection.setUseCaches(false);

        // connect
        try
        {
            urlConnection.connect();
        }
        catch (IOException ioe2)
        {
            Logger.error(this, "Error at opening url connection.", ioe2);            
            return Pipelet.PIPELET_ERROR;
        }
        
        // get the stream
        try
        {
            stream = urlConnection.getInputStream();
            if ("true".equals(cfg_closeOnSuccess)) {
                stream.close();
                stream = null;
            }
        }
        catch (IOException ioe3)
        {
            Logger.error(this, "Error when reading input stream from url connection.", ioe3);
            return Pipelet.PIPELET_ERROR;
        }

        // store 'ProductStream' in pipeline dictionary
        dict.put(DN_INPUT_STREAM, stream);
        
        String filename = urlObject.getFile().substring(urlObject.getFile().lastIndexOf("/")+1);
        
        // store 'FileName' in pipeline dictionary
        dict.put(DN_FILE_NAME, filename);
        return Pipelet.PIPELET_NEXT;
    }

    @Override
    public void init() throws PipelineInitializationException
    {
        super.init();
        cfg_closeOnSuccess = getConfiguration().get(CN_CLOSE_ON_SUCCESS);
    }
}