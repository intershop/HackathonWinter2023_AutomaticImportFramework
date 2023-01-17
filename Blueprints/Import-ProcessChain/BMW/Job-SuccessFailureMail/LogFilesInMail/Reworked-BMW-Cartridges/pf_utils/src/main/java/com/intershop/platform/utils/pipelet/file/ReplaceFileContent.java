package com.intershop.platform.utils.pipelet.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.intershop.beehive.core.capi.file.FileUtils;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.foundation.capi.upload.MVCFile;

public class ReplaceFileContent extends Pipelet
{

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        MVCFile file = (MVCFile)aPipelineDictionary.get("File");
        if (file == null) {
            throw new PipeletExecutionException("Input parameter 'File' is not valid.");            
        }
        
        String originalString = (String)aPipelineDictionary.get("OriginalString");
        if (originalString == null) {
            throw new PipeletExecutionException("Input parameter 'OriginalString' is not valid.");            
        } 

        String replaceString = (String)aPipelineDictionary.get("ReplaceString");
        if (replaceString == null) {
            throw new PipeletExecutionException("Input parameter 'ReplaceString' is not valid.");            
        } 

        try
        {        
            // create import file path 
            File impexFile = new File(FileUtils.getUnitImpexDirectory(file.getUnitDomainName()), File.separator + file.getDirectoryPath() + File.separator +  file.getFullName());
            
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(impexFile.toPath()), charset);
            content = content.replaceAll(originalString, replaceString);
            Files.write(impexFile.toPath(), content.getBytes(charset));
        }
        catch(IOException e)
        {
            Logger.error(this, "Error on modifying file", e);
            return PIPELET_ERROR;
        }

        return PIPELET_NEXT;
    }

}
