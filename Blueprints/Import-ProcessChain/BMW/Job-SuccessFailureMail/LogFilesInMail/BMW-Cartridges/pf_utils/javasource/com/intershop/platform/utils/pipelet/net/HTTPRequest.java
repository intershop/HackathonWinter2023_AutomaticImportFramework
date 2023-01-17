package com.intershop.platform.utils.pipelet.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.foundation.util.Base64;

/**
 * Send a HTTP request and return the response
 */
public class HTTPRequest extends Pipelet
{

    /**
     * Constant used to access the pipeline dictionary with key 'Method', HTTP
     * methid, default "GET"
     */
    public static final String DN_METHOD = "Method";

    /**
     * Constant used to access the pipeline dictionary with key 'URL'
     */
    public static final String DN_URL = "URL";

    /**
     * Constant used to access the pipeline dictionary with key 'AuthLogin'.
     * login for basic authentication , default none
     */
    public static final String DN_AUTH_LOGIN = "AuthLogin";

    /**
     * Constant used to access the pipeline dictionary with key 'AuthPasswort'.
     * assword for basic authentication , default none
     */
    public static final String DN_AUTH_PASSWD = "AuthPasswort";

    /**
     * Constant used to access the pipeline dictionary with key 'ProxyHost',
     * proxy host IP/ dns name , default none
     */
    public static final String DN_PROXY_HOST = "ProxyHost";

    /**
     * Constant used to access the pipeline dictionary with key 'ProxyPort'n
     * proxy port - verifued to be bumber between 100 and 65553, , default none
     */
    public static final String DN_PROXY_PORT = "ProxyPort";

    /**
     * Constant used to access the pipeline dictionary with key 'RetryCount',
     * number of retries to call the server, default 0
     */
    public static final String DN_RETRY_COUNT = "RetryCount";

    /**
     * Constant used to access the pipeline dictionary with key 'RetryInterval',
     * minutes to the next retry, default 0
     */
    public static final String DN_RETRY_INTERVAL = "RetryInterval";

    /**
     * Constant used to access the pipeline dictionary with key 'ContentType',
     * HTTP Contenf-Type, defaults to "", examplse Content-type:
     * application/json; charset=utf-8
     */
    public static final String DN_CONTENT_TYPE = "ContentType";

    /**
     * Constant used to access the pipeline dictionary with key 'ResponseCode',
     * HTTP Response Code
     */
    public static final String DN_RESPONSE_CODE = "ResponseCode";

    /**
     * Constant used to access the pipeline dictionary with key 'Content', HTTP
     * Content to send/ received, defaults to "",
     */
    public static final String DN_CONTENT = "Content";

    /**
     * Constant used to access the pipeline dictionary with key 'Encoding', 
     * the character encoding of the content to sent to the server", default UTF-8
     */
    private static final String DN_ENCODING = "Encoding";

    private class HTTPResponse
    {
        private String content = "";
        private int returnCode = 0;
        private String returnMessage = "";

        public int getReturnCode()
        {
            return returnCode;
        }

        public void setReturnCode(int returnCode)
        {
            this.returnCode = returnCode;
        }

        public String getContent()
        {
            return content;
        }

        public void setContent(String content)
        {
            this.content = content;
        }

        public String getReturnMessage()
        {
            return returnMessage;
        }

        public void setReturnMessage(String returnMessage)
        {
            this.returnMessage = returnMessage;
        }

    }

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        URL urlObject = null; // url
        String uRL = (String)dict.get(DN_URL);
        if (null == uRL)
        {
            throw new PipeletExecutionException("Mandatory input parameter 'URL' not available in pipeline dictionary.");
        }

        // create URL object
        try
        {
            urlObject = new URL(uRL);
        }
        catch(MalformedURLException mue)
        {
            Logger.error(this, "Error at URL creation.", mue);
            return Pipelet.PIPELET_ERROR;
        }

        // Method
        String method = (String)dict.get(DN_METHOD);
        if ((null == method) || ("".equals(method.trim())))
        {
            method = "GET";
        }
        else
        {
            method = method.toUpperCase();
        }

        if ((!"GET".equals(method)) && 
                        (!"PUT".equals(method)) && 
                        (!"POST".equals(method))&& 
                        (!"DELETE".equals(method))&& 
                        (!"OPTIONS".equals(method)))
        {
            throw new PipeletExecutionException("Wrong 'Method', supported are 'GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'!.");
        }

        // AuthLogin and AutPassword
        String authLogin = (String)dict.get(DN_AUTH_LOGIN);
        String authPassword = null;
        if ((null != authLogin) && (!"".equals(authLogin.trim())))
        {
            authLogin = authLogin.trim();
            authPassword = (String)dict.get(DN_AUTH_PASSWD);
            if ((null != authPassword) && (!"".equals(authPassword.trim())))
            {
                authPassword = authPassword.trim();
            }
        }
        
        

        // proxyHost and proxyPort
        String proxyHost = (String)dict.get(DN_PROXY_HOST);
        int proxyPort = 8080;
        if ((null != proxyHost) && (!"".equals(proxyHost.trim())))
        {
            proxyHost = proxyHost.trim();
            String sProxyPort = (String)dict.get(DN_PROXY_PORT);
            if ((null != sProxyPort) && (!"".equals(sProxyPort.trim())))
            {
                sProxyPort = sProxyPort.trim();
                try
                {
                    proxyPort = Integer.valueOf(sProxyPort);
                    if (!((100 <= proxyPort) && (proxyPort <= 65535)))
                    {
                        throw new PipeletExecutionException("The 'ProxyPort' out of range!");
                    }
                }
                catch(NumberFormatException eNF)
                {
                    throw new PipeletExecutionException("The 'ProxyPort' is not a avlid number!");
                }
            }
        }

        // proxyHost and proxyPort
        String sRetryCount = (String)dict.get(DN_RETRY_COUNT);
        int retryCount = 0;
        int retryInterval = 0;
        if ((null != sRetryCount) && (!"".equals(sRetryCount.trim())))
        {
            sRetryCount = sRetryCount.trim();
            String sRetryInterval = (String)dict.get(DN_RETRY_INTERVAL);
            if ((null != sRetryInterval) && (!"".equals(sRetryInterval.trim())))
            {
                sRetryInterval = sRetryInterval.trim();
                try
                {
                    retryCount = Integer.valueOf(sRetryCount);
                    retryInterval = Integer.valueOf(sRetryInterval);
                }
                catch(NumberFormatException eNF)
                {
                    retryInterval = 1;
                    Logger.error(this,
                                    "The 'RetryCount' or 'RetryInterval' is not a avlid number, set to '+ retryInterval +'!");
                }
            }
        }

        String contentType = (String)dict.get(DN_CONTENT_TYPE);
        if ((null != contentType) && (!"".equals(contentType.trim())))
        {
            contentType = contentType.trim();
        }
        else
        {
            contentType = "application/json; charset=utf-8";
        }
        
        
        String encoding = (String)dict.get(DN_ENCODING);
        if ((null != encoding) && (!"".equals(encoding.trim())))
        {
            encoding = encoding.trim();
        }
        else
        {
            encoding = "UTF-8";
        }
        
        // "{\"status\": \"RUNNING\",\"type\": \"Job\"}";
        String content = (String)dict.get(DN_CONTENT);
        if ((null != content) && (!"".equals(content.trim())))
        {
            content = content.trim();
        }

        HTTPResponse response = new HTTPResponse();
        int responseCode = -1;
        try
        {
            response = readResponse(urlObject, method, contentType, encoding, authLogin, authPassword, content);
        }
        catch(IOException ex)
        {
            if(ex.getMessage().startsWith("Server returned HTTP response code:"))
            {
                response.setReturnCode( getReturnCodeFromMsg(ex) );
                dict.put(DN_RESPONSE_CODE, response.getReturnCode());
            }
            throw new PipeletExecutionException(ex);
        }

        dict.put(DN_RESPONSE_CODE, response.getReturnCode());
        dict.put(DN_CONTENT, response.getContent());

        return PIPELET_NEXT;
    }

    /**
     * opens a connection to the order server writes an empty message, if the
     * orderserver answers in a defined way, the method will return true if a
     * timeout or an undefined answer occurs it will return false
     * 
     * @param content
     *            Description of the Parameter
     * @param encoding
     *            Description of the Parameter
     * @param url_string
     *            Description of the Parameter
     * @return Description of the Return Value
     * @exception MalformedURLException
     *                Description of the Exception
     * @exception IOException
     *                Description of the Exception
     */

    private HTTPResponse readResponse( URL url,
                    String method,
                    String contentTyoe,
                    String encoding,
                    String authLogin, 
                    String authPassword, 
            String content ) throws IOException
    {
        HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();
        connection.setDoOutput( true );
        connection.setRequestMethod( method );

        if (null != authLogin)
        {
            String userpassword = authLogin + ":" + authPassword;
            String encodedAuthorization = Base64.encode( userpassword.getBytes() );
            connection.setRequestProperty("Authorization", "Basic "+
                  encodedAuthorization);
        }

   
        connection.setRequestProperty( "Content-type", contentTyoe );
        connection.setRequestProperty( "Accept-Charset", contentTyoe );

        if(("POST".equals(method)) || ("PUT".equals(method)))
        {
            if((null != content) && (! "".equals(content.trim())))
            {
                OutputStream out = connection.getOutputStream();

                byte[] bContent = content.getBytes( encoding );
                out.write( bContent );
                out.close();
            }
        }

        int responseCode = connection.getResponseCode();

        /*
        BufferedReader inStream
        = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );

        char[] responseArr = new char[1];

        while ( -1 != inStream.read( responseArr, 0, 1 ) )
            responseStr += responseArr[0];

        startTimerThread(connection, url);
        inStream.close();
        */

        BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
        String inputLine;
        StringBuffer responseStr = new StringBuffer("");
        while ((inputLine = in.readLine()) != null) 
            responseStr.append(inputLine);
        in.close();        
        
        HTTPResponse response = new HTTPResponse();
        response.setContent(responseStr.toString());
        response.setReturnCode(responseCode);
        return response;
    }

    /**
     * This method starts a thread to server as a timer to close the
     * HTTPUrlConnection in the class.It will count for 5 * 60 seconds and then
     * close the connection.
     * 
     * @return void
     */
    private void startTimerThread(final HttpURLConnection connection, final URL url)
    {
        Thread timer = new Thread("HTTPUrlConnectionTimer") // creating the
                                                            // Timer Thread.
        {
            long startTime = System.currentTimeMillis();// Star Timer for Timer
            long timeLimit = 5 * 60 * 1000; // To store timelimit i.e. 5*60*1000
                                            // (5 Minutes)
            long currentTime = 0;

            /**
             * Run method of the Timer Thread.
             */
            @Override
            public void run()
            {
                Logger.debug(this, "Timer thread triggered ");
                while((System.currentTimeMillis() - startTime) < timeLimit)
                {
                    try
                    {
                        Thread.sleep(timeLimit);
                    }
                    catch(InterruptedException e)
                    {
                        Logger.debug(this, " Timer thread interrupted ");
                    }
                }
                Logger.debug(this,
                                " Timer Elapsed  5*60 seconds so closing the HTTP Connection to the server "
                                                + url.toString());
                connection.disconnect();
                Logger.debug(this, " Timer thread exiting by closing the connection" + url);
            }
        };
        // starting the thread.
        timer.start();
    }
    
    
    /**
     * helper
     */
    String getPrintableTrace(Throwable ex)
    {
        StringBuffer stactKrace = new StringBuffer(ex.getMessage());
        StackTraceElement[] sE = ex.getStackTrace();
        for( int i = 0; i< sE.length; i++)
        {
            StackTraceElement element = sE[i];
            stactKrace.append("\n    at ")
            .append(element.getClassName()).append(":")
            .append(element.getMethodName()).append("(")
            .append(element.getLineNumber()).append(")");
        }
       return stactKrace.toString();
    }
    
    int getReturnCodeFromMsg(Throwable ex)
    {
        String message = ex.getMessage();
        message = message.substring("Server returned HTTP response code: ".length());
        message = message.substring(0, message.indexOf(" for"));
        return Integer.valueOf(message.trim());
    }

}
