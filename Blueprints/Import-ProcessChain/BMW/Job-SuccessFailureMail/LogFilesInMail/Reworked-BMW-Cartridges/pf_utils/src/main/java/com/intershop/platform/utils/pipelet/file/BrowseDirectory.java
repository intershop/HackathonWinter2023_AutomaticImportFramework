package com.intershop.platform.utils.pipelet.file;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;
import com.intershop.component.foundation.capi.upload.Directory;
import com.intershop.component.foundation.capi.upload.FileNameVerifier;
import com.intershop.component.foundation.internal.upload.GenericDirectoryScanner;
import com.intershop.platform.utils.internal.file.NoneDomainDirectoryScanner;

/**
 * Scans a directory in the Intershop shared file system for its content recursively.
 */
public  class BrowseDirectory
    extends Pipelet
{
    public static final String IO_BASEDIRECTORY_NAME = "BaseDirectory";
    public static final String IO_DIRECTORYPATH_NAME = "DirectoryPath";
    public static final String IO_DIRECTORY_NAME = "Directory";

    /**
     * Member attribute for holding the pipelet configuration value 'DefaultDirectoryPath'.
     */
    private String cfg_defaultDirectoryPath;

	/**
	 * Constant used to access pipelet configuration with key 'Recursive'
	 */
	public static final String CN_RECURSIVE = "Recursive";

	/**
	 * Member attribute that holds the pipelet configuration value 'Recursive'
	 */
	private boolean cfg_recursive = false;

    private FileNameVerifier dirPathVerifier = null;

    private boolean includeParentDirectories = true;

    @Override
    public void init()
        throws PipelineInitializationException, SystemException
    {
        if(getConfiguration().get(CN_RECURSIVE) != null)
        {
            cfg_recursive = Boolean.parseBoolean(getConfiguration().get(CN_RECURSIVE).toString());
        }
        else
        {
            cfg_recursive = true;
        }

        if (getConfiguration().get("IncludeParentDirectories") != null)
        {
            includeParentDirectories = Boolean.valueOf(
                getConfiguration().get("IncludeParentDirectories").toString()).booleanValue();
        }

        cfg_defaultDirectoryPath = (String)getConfiguration().get("DefaultDirectoryPath");
        dirPathVerifier = new FileNameVerifier(true);
    }

    private void addDirPathToExpandedMap(Map<String, String> map, String dir)
    {
        String[] paths = dir.split(File.pathSeparator);

        String subpath = paths[0];

        map.put(subpath, subpath);

        for (int i = 1; i < paths.length; i++)
        {
            subpath += paths[i];
            map.put(subpath, subpath);
        }
    }

    @Override
    public int execute(PipelineDictionary dict)
        throws PipeletExecutionException, SystemException
    {
        File baseDirectory = (File)dict.getRequired(IO_BASEDIRECTORY_NAME);

        String directoryPath = (String)dict.get(IO_DIRECTORYPATH_NAME);

        if (directoryPath != null && !directoryPath.trim().equals(""))
        {
            if (dirPathVerifier.verify(directoryPath) != FileNameVerifier.Status.ISVALIDNAME)
            {
                throw new PipeletExecutionException("directory path is not valid");
            }
        }
        else if (cfg_defaultDirectoryPath != null && !cfg_defaultDirectoryPath.trim().equals(""))
        {
            directoryPath = cfg_defaultDirectoryPath;
        }
        else
        {
            directoryPath = "";
        }

        try
        {
            GenericDirectoryScanner ds = null;

            Directory dir = null;

            if (includeParentDirectories)
            {
                ds = new NoneDomainDirectoryScanner(baseDirectory, cfg_recursive);
            }
            else
            {
                ds = new NoneDomainDirectoryScanner(baseDirectory, directoryPath, cfg_recursive);
            }

            dir = ds.scan();

            if (includeParentDirectories && directoryPath.length() > 0)
            {
                dir = dir.getDirectory(directoryPath);
            }

            if (dir == null)
            {
                Logger.debug(this, "cannot find directory for the specified parameters");
                return PIPELET_ERROR;
            }

            dict.put(IO_DIRECTORY_NAME, dir);
        }
        catch (IllegalArgumentException ex)
        {
            Logger.debug(this, ex.getMessage());
            return PIPELET_ERROR;
        }
        catch (IOException ex)
        {
            Logger.error(this, ex.getMessage(), ex);
            return PIPELET_ERROR;
        }

        return PIPELET_NEXT;
    }
}
