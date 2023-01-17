package com.intershop.platform.utils.internal.file;

import java.io.File;
import java.io.IOException;

import com.intershop.component.foundation.capi.upload.Directory;
import com.intershop.component.foundation.internal.upload.GenericDirectoryScanner;

/**
 * Class for scanning the static content of unit for files and directories.
 *
 * @author Andreas Diel
 */
public class NoneDomainDirectoryScanner implements GenericDirectoryScanner
{
   private boolean recursive = false;
   private File startDir = null;
   private File baseDirectory = null;

   /**
    * The constructor.
    * @param recursive Specifies if the directory should be scanned recursive. - *
    * The constructor.
    * @param baseDirectory Identifier for impex or static content directory of
    * the domain.
    * @param dirPath The directory path relative to the unit directory.
    * @param localeID
    * @param overrideUnitDirectory Override last directory from choosen unit directory. E.g. the last directory (before the locale) of SECURE_STATIC_DIR is "attachments" and could be *renamed* to given string. Only for SECURE_STATIC_DIR.
    */
   public NoneDomainDirectoryScanner(File baseDirectory, String dirPath, boolean recursive)
   {
        this.baseDirectory = baseDirectory;
        this.recursive = recursive;
        
        String filePath = dirPath.replace('\\', File.separatorChar).replace('/', File.separatorChar);
        this.startDir = new File(baseDirectory, filePath);

        if (!this.startDir.exists())
        {
            throw new IllegalArgumentException(this.startDir.getAbsolutePath() + " does not exist");
        }

        if (!this.startDir.isDirectory())
        {
            throw new IllegalArgumentException(this.startDir.getAbsolutePath() + " is not a directory");
        }
   }

   /**
    * The constructor.
    * @param recursive Specifies if the directory should be scanned recursive. - *
    * The constructor.
    * @param baseDirectory Identifier for impex or static content directory of
    * the domain.
    * @param localeID
    * @param overrideUnitDirectory Override last directory from choosen unit directory. E.g. the last directory (before the locale) of SECURE_STATIC_DIR is "attachments" and could be *renamed* to given string. Only for SECURE_STATIC_DIR.
    */
   public NoneDomainDirectoryScanner(File baseDirectory, boolean recursive)
   {
       this(baseDirectory, "", recursive);
   }

   /**
    * Starts the scan process. The start directory is
    * .../<unit_name>/static/<language>.
    * @return A <code>Directory</code> object containing entries
    * for all sub directories and files of the start directory.
    * @throws IOException in case of I/O errors
    */
   @Override
   public Directory scan() throws IOException
   {
        NoneDomainDirectoryImpl dir =
            new NoneDomainDirectoryImpl(startDir.getAbsolutePath(), "", null);
        scanDir(startDir, dir);
        return dir;
   }

   /**
    * Starts the scan process. The start directory is
    * .../<unit_name>/static/<language>/<path>.
    * @param path Specifies a sub directory of the static content directory.
    * @return A <code>Directory</code> object containing entries
    * for all sub directories and files of the start directory.
    * @throws IOException in case of I/O errors
    */
   public Directory scan(String path) throws IOException
   {
        if (path == null)
        {
            throw new IllegalArgumentException("path must not be null");
        }

        File dir = new File(startDir, path + File.separator);

        if (!dir.isDirectory())
        {
            throw new IllegalStateException("specified path is not a directory");
        }

        NoneDomainDirectoryImpl directory = new NoneDomainDirectoryImpl(startDir.getAbsolutePath(), path);
        scanDir(dir, directory);
        return directory;
   }

   /**
    * @param dir
    * @param directory
    * @throws IOException in case of I/O errors
    */
   private void scanDir(File dir, NoneDomainDirectoryImpl directory) throws IOException
   {
        String[] entries = dir.list();

        // the result of list() operation may be null because of
        // incorrect access privileges
        // bug #12244
        if (entries == null)
        {
            // throw new IOException due to
            // in the API of the java.io.File class
            throw new IOException(
                    "Unknown I/O error occured reading directory: "
                    + dir.getAbsolutePath()
                    + " Does the directory exist? Are there incorrect file access privileges?");
        }

        for (int i = 0; i < entries.length; ++i)
        {
            File file = new File(dir, entries[i] + File.separator);

            if (file.isDirectory())
            {
                NoneDomainDirectoryImpl newDirectory =
                    new NoneDomainDirectoryImpl(startDir.getAbsolutePath(), file.getName(), null);
                directory.addDirectory(newDirectory);

                if (recursive)
                {
                    scanDir(file, newDirectory);
                }
            }
        }
   }
}
