package com.intershop.platform.impex.automation.pipelet;

import java.io.File;

import com.intershop.beehive.core.capi.file.FileUtils;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

public class GetSitesDirectory extends Pipelet
{

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        final File sitesDir = new File(FileUtils.getSiteShareDirectory());
        dict.put("SitesDirectory", sitesDir);
        return PIPELET_NEXT;
    }

}
