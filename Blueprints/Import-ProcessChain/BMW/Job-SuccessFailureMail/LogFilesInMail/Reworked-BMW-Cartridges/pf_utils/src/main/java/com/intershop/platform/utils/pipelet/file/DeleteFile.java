package com.intershop.platform.utils.pipelet.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

public class DeleteFile extends Pipelet
{
    private static final String ERROR_CODE_NO_SUCH_FILE = "NoSuchFile";
    private static final String ERROR_CODE_DIRECTORY_NOT_EMPTY = "DirectoryNotEmpty";
    private static final String ERROR_CODE_SECURITY_EXCEPTION = "SecurityException";
    private static final String ERROR_CODE_IO_EXCEPTION = "IOException";

    /**
     * Constant used to access the pipeline dictionary with key 'File'
     */
    public static final String DN_FILE = "File";

    /**
     * Constant used to access the pipeline dictionary with key 'ErrorCode'
     *
     * If there was an error while copying the files to the output stream or something similar, the error connector of the pipelet is used. To indicate what happened, an error code will be put into the pipeline dictionary.
     */
    public static final String DN_ERROR_CODE = "ErrorCode";

    /**
     * Constant used to access the pipeline dictionary with key 'Exception'
     */
    public static final String DN_EXCEPTION = "Exception";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        // lookup 'File' in pipeline dictionary
        File file = aPipelineDictionary.getRequired(DN_FILE);

        //boolean deleted = file.delete();
        try
        {
            java.nio.file.Files.delete(file.toPath());
        }
        catch (NoSuchFileException exNoSuchFile)
        {
            aPipelineDictionary.put(DN_ERROR_CODE, ERROR_CODE_NO_SUCH_FILE);
            aPipelineDictionary.put(DN_EXCEPTION, exNoSuchFile);
            return PIPELET_ERROR;
        }
        catch (DirectoryNotEmptyException exDirectoryNotEmpty)
        {
            aPipelineDictionary.put(DN_ERROR_CODE, ERROR_CODE_DIRECTORY_NOT_EMPTY);
            aPipelineDictionary.put(DN_EXCEPTION, exDirectoryNotEmpty);
            return PIPELET_ERROR;
        }
        catch (SecurityException exSecurity)
        {
            aPipelineDictionary.put(DN_ERROR_CODE, ERROR_CODE_SECURITY_EXCEPTION);
            aPipelineDictionary.put(DN_EXCEPTION, exSecurity);
            return PIPELET_ERROR;
        }
        catch (IOException exIO)
        {
            aPipelineDictionary.put(DN_ERROR_CODE, ERROR_CODE_IO_EXCEPTION);
            aPipelineDictionary.put(DN_EXCEPTION, exIO);
            return PIPELET_ERROR;
        }

        return PIPELET_NEXT;

//        if (deleted)
//        {
//            return PIPELET_NEXT;
//        }
//        else
//        {
//            return PIPELET_ERROR;
//        }
    }
}
