package com.intershop.platform.utils.pipelet.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.intershop.beehive.core.capi.file.FileUtils;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.foundation.util.ResettableIterator;

/**
 * Copy one or more files into ZIP file.
 * 
 * If something failed while copying one ore more files to the outpunt stream, the error connector is used and the
 * dictionary out property "ErrorCode" will be used to indicate which error happened. If a temporary zip file was
 * created, this will be also returned and the original file name either. These are the returned error codes:
 * ERROR_CODE_ZIP_CREATION - the zip file could not be created ERROR_CODE_ZIP_FAULTY - the zip file contains faulty
 * files
 * 
 */
public class CreateZipFile extends Pipelet
{
    private static final String ERROR_CODE_ZIP_CREATION = "ERROR_CODE_ZIP_CREATION";
    private static final String ERROR_CODE_ZIP_FAULTY = "ERROR_CODE_ZIP_FAULTY";

    private static final String STATUS_CODE_OK = "OK";
    private static final String STATUS_CODE_ERROR = "SCE";

    /**
     * default buffer size
     */
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024; // 8KB.

    /**
     * Constant used to access the pipeline dictionary with key
     * 'Files'
     */
    public static final String DN_FILES = "Files";

    /**
     * Constant used to access the pipeline dictionary with key 'ZipFile'
     */
    public static final String DN_ZIP_FILE = "ZipFile";

    /**
     * Constant used to access the pipeline dictionary with key 'BaseDirectory'
     */
    public static final String DN_BASE_DIRECTORY = "BaseDirectory";

    /**
     * Constant used to access the pipeline dictionary with key 'ErrorCode'
     *
     * If there was an error while copying the files to the output stream or something similar, the error connector of the pipelet is used. To indicate what happened, an error code will be put into the pipeline dictionary.
     */
    public static final String DN_ERROR_CODE = "ErrorCode";

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
    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        // lookup 'ZipFile' in pipeline dictionary
        File zipFile = dict.getRequired(DN_ZIP_FILE);

        // lookup 'Files' in pipeline dictionary
        ResettableIterator<File> files = (ResettableIterator<File>)dict.getRequired(DN_FILES);
        if (!files.hasNext())
        {
            throw new PipeletExecutionException("Input parameter 'Files' is an empty iterator.");
        }

        // lookup 'BaseDirectory' in pipeline dictionary
        File baseDirectory = dict.getRequired(DN_BASE_DIRECTORY);

        try
        {
            boolean success = writeFilesToZipOutputStream(files, zipFile, baseDirectory);

            if (!success)
            {
                Logger.error(CreateZipFile.class, "ZIP file " + zipFile.getName() + "contains faulty file.");
                dict.put(DN_ERROR_CODE, ERROR_CODE_ZIP_FAULTY);
                return PIPELET_ERROR;
            }
            else
            {
                return PIPELET_NEXT;
            }
        }
        catch(IOException e)
        {
            Logger.error(CreateZipFile.class, "Error while creating zip file " + zipFile.getName(), e);
            dict.put(DN_ERROR_CODE, ERROR_CODE_ZIP_CREATION);
            return PIPELET_ERROR;
        }
    }

    /**
     * Write file to output stream with zip compression.
     *
     * @param fileList
     * @param parent
     * @param tempDir the temporary directory to store the zip file in
     * @return <code>true</code> if case of success, <code>false</code> if the process had errors
     * @throws IOException
     */
    public static boolean writeFilesToZipOutputStream(ResettableIterator<File> fileList, File zipFile, File baseDirectory) throws IOException
    {
        // the stream, that writes into the temp file
        FileOutputStream zipFileOutputStream = null;
        // Create a zipstream and write it to the temp file
        ZipOutputStream zipstream = null;

        // was there a write error?
        boolean errorWhileWritingToZip = false;

        if (baseDirectory == null)
        {
            // try to find a common parent of all fileList files
            fileList.reset();
            while(fileList.hasNext())
            {
                File file = fileList.next();

                if (baseDirectory == null)
                {
                    baseDirectory = file.getParentFile();
                }
                else
                {
                    baseDirectory = determineCommonParent(baseDirectory, file);
                }
            }
        }

        try
        {
            zipFileOutputStream = new FileOutputStream( zipFile );
            zipstream = new ZipOutputStream( zipFileOutputStream );

            // copy file(s) to temporary output stream
            fileList.reset();
            while (fileList.hasNext())
            {
               File file = fileList.next();
               if (!putFileToZipOutputStream(zipstream, file, baseDirectory).equals(STATUS_CODE_OK))
               {
                   errorWhileWritingToZip = true;
               }
            }
        }
        finally
        {
            try
            {
                if ( zipstream != null )
                {
                    zipstream.close();
                }
                if ( zipFileOutputStream != null )
                {
                    zipFileOutputStream.close();
                }
            }
            catch(IOException ignore)
            {}
        }

        return !errorWhileWritingToZip;
    }

    /**
     * Put file in zip compressed output stream, if file is directory, add all
     * filtered subfiles to zip compressed output stream.
     *
     * @param output
     * @param file
     * @param parent
     * @return A status code if the operation was successful
     * @throws IOException
     */
    public static String putFileToZipOutputStream(ZipOutputStream output, File file, File baseDirectory) throws IOException
    {
        if (file.isDirectory())
        {
            // root is not a really dir, it's only a overview for the shared directories
            if (!file.equals(baseDirectory))
            {
                // add dir to zip entry
                output.putNextEntry(new ZipEntry(getRelativeName(baseDirectory, file) + "/"));
                output.closeEntry();
            }
            // add all files of dir to zipentry
            File[] subFiles = file.listFiles(new FileFilter() {@Override
            public boolean accept(File f) {return !f.isDirectory();}} );
            String statusCode = STATUS_CODE_OK;
            for (File subFile : subFiles)
            {
                String currentStatusCode = putFileToZipOutputStream(output, subFile, baseDirectory);
                // save the error status code, do not override it
                if (!STATUS_CODE_ERROR.equals(statusCode))
                {
                    statusCode = currentStatusCode;
                }
            }
            return statusCode;
        }
        else
        {
            InputStream input = null;
            byte[] buffer = new byte[1024 * 8];
            String relativeName = null;
            boolean exceptionWhileReadingFile = false;
            FileInputStream fis = null;
            try
            {
                relativeName = getRelativeName(baseDirectory, file);
                fis = new FileInputStream(file);

                // trying to read the file, if this fails, catch block will be called
                input = new BufferedInputStream(fis, DEFAULT_BUFFER_SIZE);

                output.putNextEntry(new ZipEntry(relativeName));
                for(int length = 0; (length = input.read(buffer)) > 0;)
                {
                    output.write(buffer, 0, length);
                }
                output.closeEntry();
            }
            catch (IOException exception)
            {
                if (fis != null)
                {
                    FileLock fileLock = fis.getChannel().tryLock();
                    if(fileLock != null)
                    {
                        Logger.error(CreateZipFile.class, "Error reading file '{}'", file.getAbsolutePath(), exception );
                        exceptionWhileReadingFile = true;
                    }
                    else
                    {
                        // do nothing it's okay, ignore fileLock
                        Logger.warn(CreateZipFile.class, "File '{}' is locked, cannot add to ZIP", file.getAbsolutePath(), null);
                    }
                }
                else
                {
                    Logger.error(CreateZipFile.class, "Error creating Zip file '{}", file.getAbsolutePath(), exception);
                }
            }
            finally
            {
                FileUtils.close(input);
            }

            return exceptionWhileReadingFile ? STATUS_CODE_ERROR : STATUS_CODE_OK;
        }
    }

    /**
     * Find the first common parent folder of the given files.
     * Return null if one file is null or no common folder is found.
     *
     * @param file1
     * @param file2
     * @return file as parent
     */
    public static File determineCommonParent(File file1, File file2)
    {
        if (file1 == null || file2 == null)
        {
            return null;
        }

        if (file1.equals(file2))
        {
            return file1;
        }

        File parent = file1.getParentFile();

        while (parent != null)
        {
            if (parent.equals(file2))
            {
                return parent;
            }
            parent = parent.getParentFile();
        }

        return determineCommonParent(file1, file2.getParentFile());
    }

    /**
     * Get the relative name of the given file relative to the parent.
     *
     * @param parent
     * @param file
     * @return the relative name
     * @throws IOException
     */
    public static String getRelativeName(File parent, File file)
    {
        if (null != parent && !parent.equals(file))
        {
            String[] filelist = file.getAbsolutePath().split(Pattern.quote(File.separator));
            String[] parentlist = parent.getAbsolutePath().split(Pattern.quote(File.separator));

            int pos = parentlist.length;

            for (int i = 0; i < parentlist.length; i++)
            {
                if (!filelist[i].equals(parentlist[i]))
                {
                    pos = i;
                    break;
                }
            }
            StringBuilder name = new StringBuilder();
            for(int i = pos; i < filelist.length; i++)
            {
                if(i > pos)
                {
                    name.append(File.separator);
                }
                name.append(filelist[i]);
            }

            return name.toString();
        }
        return file.getName();
    }
}
