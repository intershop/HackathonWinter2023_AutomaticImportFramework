package com.intershop.platform.utils.pipelet.file;

import java.io.File;
import java.io.IOException;

import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * This pipelet is used to create an empty file based on the given file name.
 * 
 * @author JCMeyer
 * @version 1.0, 2015-09-14
 * @since 3.2.0
 */
public class CreateFile extends Pipelet
{
    /**
     * Constant used to access the pipeline dictionary with key 'FileName'.
     */
    protected static String DN_FILE_NAME = "FileName";

    /**
     * Constant used to access the pipeline dictionary with key 'CreateAsDirectory'.
     */
    protected static String DN_CREATE_AS_DIRECTORY = "CreateAsDirectory";

    /**
     * Constant used to access the pipeline dictionary with key 'CreateParentDirectories'.
     */
    protected static String DN_CREATE_PARENT_DIRECTORIES = "CreateParentDirectories";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        String fileName = dict.getRequired(DN_FILE_NAME);

        Boolean createAsDirectory = dict.getOptional(DN_CREATE_AS_DIRECTORY, Boolean.FALSE);

        Boolean createParentDirectories = dict.getOptional(DN_CREATE_PARENT_DIRECTORIES, Boolean.FALSE);

        File file = new File(fileName);
        try
        {
            if (createParentDirectories)
            {
                file.getParentFile().mkdirs();
            }
            
            if (createAsDirectory)
            {
                file.mkdir();
            }
            else
            {
                file.createNewFile();
            }
            
            dict.put("File", file);
        }
        catch (IOException ex)
        {
            Logger.error(CreateZipFile.class, "Error creating file " + fileName);
            throw new PipeletExecutionException(ex);
        }

        return PIPELET_NEXT;
    }
}
