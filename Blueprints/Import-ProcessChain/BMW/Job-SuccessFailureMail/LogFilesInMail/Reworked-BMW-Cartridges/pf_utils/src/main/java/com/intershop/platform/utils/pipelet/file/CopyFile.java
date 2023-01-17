package com.intershop.platform.utils.pipelet.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * Copy a file.
 * Target file does not need to exist. If it does not exists it is created, if it exists it is overwritten.
 *
 * @author t.koerbs@intershop.de
 * @version 1.0, 2015-07-24
 * @since 3.1.0
 *
 *          First basic functionality.
 */
public class CopyFile extends Pipelet
{
    protected static String DN_SOURCE_FILE = "SourceFile";

    protected static String DN_SOURCE_FILE_NAME = "SourceFileName";

    protected static String DN_SOURCE_DIRECTORY = "SourceDirectory";

    protected static String DN_SOURCE_DIRECTORY_NAME = "SourceDirectoryName";

    protected static String DN_TARGET_FILE = "TargetFile";

    protected static String DN_TARGET_FILE_NAME = "TargetFileName";

    protected static String DN_TARGET_DIRECTORY = "TargetDirectory";

    protected static String DN_TARGET_DIRECTORY_NAME = "TargetDirectoryName";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        //
        // Read in source file dictionary params and construct an according File instance
        //

        File sourceFile = aPipelineDictionary.getOptional(DN_SOURCE_FILE);
        if (sourceFile == null)
        {
            String sourceFileName = aPipelineDictionary.getRequired(DN_SOURCE_FILE_NAME);

            File sourceDirectory = aPipelineDictionary.getOptional(DN_SOURCE_DIRECTORY);
            if (sourceDirectory != null)
            {
                sourceFile = new File(sourceDirectory, sourceFileName);
            }
            else
            {
                String sourceDirectoryName = aPipelineDictionary.getOptional(DN_SOURCE_DIRECTORY_NAME);
                if (sourceDirectoryName != null)
                {
                    sourceFile = new File(sourceDirectoryName, sourceFileName);
                }
                else
                {
                    sourceFile = new File(sourceFileName);
                }
            }
        }

        //
        // Read in target file dictionary params and construct an according File instance
        //

        File targetFile = aPipelineDictionary.getOptional(DN_TARGET_FILE);
        if (targetFile == null)
        {
            String targetFileName = aPipelineDictionary.getRequired(DN_TARGET_FILE_NAME);

            File targetDirectory = aPipelineDictionary.getOptional(DN_TARGET_DIRECTORY);
            if (targetDirectory != null)
            {
                targetFile = new File(targetDirectory, targetFileName);
            }
            else
            {
                String targetDirectoryName = aPipelineDictionary.getOptional(DN_TARGET_DIRECTORY_NAME);
                if (targetDirectoryName != null)
                {
                    targetFile = new File(targetDirectoryName, targetFileName);
                }
                else
                {
                    targetFile = new File(targetFileName);
                }
            }
        }

        try
        {
            copyFile(sourceFile, targetFile);
        }
        catch(IOException exIO)
        {
            Logger.error(this, "unable to copy \"{}\" to  \"{}\": {}", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath(), exIO.toString());
            return PIPELET_ERROR;
        }

        aPipelineDictionary.put(DN_TARGET_FILE, targetFile);

        return PIPELET_NEXT;
    }

    /**
     * @param sourceFile must exist
     * @param targetFile does not need to exist; if it does not exists it is created, if it exists it is overwritten
     * @throws IOException
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException
    {
        InputStream in = new FileInputStream(sourceFile);
        OutputStream out = new FileOutputStream(targetFile);

        // Copy the bytes from InputStream to OutputStream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }

        out.close();
        in.close();
    }
}
