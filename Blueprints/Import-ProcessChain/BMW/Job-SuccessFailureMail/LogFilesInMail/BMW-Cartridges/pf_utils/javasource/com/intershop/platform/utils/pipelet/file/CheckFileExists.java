package com.intershop.platform.utils.pipelet.file;

import java.io.File;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * Checks if the given file exists. If it exists the "next" connector is used,
 * if it does not exist then the "error" connector is used. The pipelet does
 * not distinguish between a file and a directory.
 *
 * @author t.koerbs@intershop.de
 * @version 1.0, 2015-07-24
 * @since 3.1.0
 *
 *          First basic functionality.
 */
public class CheckFileExists extends Pipelet
{
    protected static String DN_FILE = "File";

    protected static String DN_FILE_NAME = "FileName";

    protected static String DN_DIRECTORY = "Directory";

    protected static String DN_DIRECTORY_NAME = "DirectoryName";

    protected static String DN_EXISTING_FILE = "ExistingFile";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        //
        // Read in dictionary params and construct an according File instance
        //
        File file = aPipelineDictionary.getOptional(DN_FILE);
        if (file == null)
        {
            String fileName = aPipelineDictionary.getRequired(DN_FILE_NAME);

            File directory = aPipelineDictionary.getOptional(DN_DIRECTORY);
            if (directory != null)
            {
                file = new File(directory, fileName);
            }
            else
            {
                String directoryName = aPipelineDictionary.getOptional(DN_DIRECTORY_NAME);
                if (directoryName != null)
                {
                    file = new File(directoryName, fileName);
                }
                else
                {
                    file = new File(fileName);
                }
            }
        }

        if (file.exists())
        {
            aPipelineDictionary.put(DN_EXISTING_FILE, file);
            return PIPELET_NEXT;
        }
        else
        {
            return PIPELET_ERROR;
        }
    }
}
