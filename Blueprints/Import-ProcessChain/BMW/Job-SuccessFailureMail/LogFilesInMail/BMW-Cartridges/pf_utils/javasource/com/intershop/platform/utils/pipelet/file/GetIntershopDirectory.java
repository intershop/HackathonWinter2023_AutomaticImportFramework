package com.intershop.platform.utils.pipelet.file;

import java.io.File;

import com.intershop.beehive.core.capi.file.FileUtils;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;

/**
 * Determine a directory in the intershop shared file system.
 *
 * @author t.koerbs@intershop.de
 * @version 1.0, 2015-07-24
 * @since 3.1.0
 *
 *          Currently only the ClusterConfigDirectory ($IS_SHARE/system/config/cluster) is supported.
 *
 * @author t.koerbs@intershop.de
 * @version 1.1, 2016-02-22
 * @since 3.2.1
 *
 *          Added LogSharedDirectory ($IS_SHARE/system/log).
 */
public class GetIntershopDirectory extends Pipelet
{
    /**
     * Constant used to access pipelet configuration with key 'IntershopDirectory'
     * The information field to retrieve.
     */
    private static final String CN_INTERSHOP_DIRECTORY = "IntershopDirectory";

    protected static String DN_DIRECTORY = "Directory";

    enum IntershopDirectories
    {
        ClusterConfigDirectory
        {
            public String getDirectory()
            {
                return FileUtils.getClusterConfigDirectory();
            }
        },
        LogSharedDirectory
        {
            public String getDirectory()
            {
                return FileUtils.getLogSharedDirectoryName();
            }
        },
        SharedDirectory
        {
            public String getDirectory()
            {
                return FileUtils.getSharedDirectory();
            }
        };

        abstract public String getDirectory();
    }
    IntershopDirectories cfg_intershopDirectory;

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        String directoryPath = cfg_intershopDirectory.getDirectory();

        File directory = new File(directoryPath);

        aPipelineDictionary.put(DN_DIRECTORY, directory);

        return PIPELET_NEXT;
    }

    @Override
    public void init() throws PipelineInitializationException
    {
        // store 'IntershopDirectory' config value in field variable
        String cfg_intershopDirectoryConfig = (String) getConfiguration().get(CN_INTERSHOP_DIRECTORY);
        if ("ClusterConfigDirectory".equals(cfg_intershopDirectoryConfig))
        {
            cfg_intershopDirectory = IntershopDirectories.ClusterConfigDirectory;
        }
        else if ("LogSharedDirectory".equals(cfg_intershopDirectoryConfig))
        {
            cfg_intershopDirectory = IntershopDirectories.LogSharedDirectory;
        }
        else if ("SharedDirectory".equals(cfg_intershopDirectoryConfig))
        {
            cfg_intershopDirectory = IntershopDirectories.SharedDirectory;
        }
        else if (cfg_intershopDirectoryConfig == null) {
            throw new PipelineInitializationException("mandatory attribute 'IntershopDirectory' not found in pipelet configuration of GetIntershopDirectory");
        }
        else {
            throw new PipelineInitializationException("attribute \"IntershopDirectory\" in pipelet configuration of GetIntershopDirectory contains invalid value: \"" + cfg_intershopDirectoryConfig + "\"");
        }
    }
}
