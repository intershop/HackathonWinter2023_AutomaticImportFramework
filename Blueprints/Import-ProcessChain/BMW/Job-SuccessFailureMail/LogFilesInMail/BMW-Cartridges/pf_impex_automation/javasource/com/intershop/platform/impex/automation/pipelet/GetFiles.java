package com.intershop.platform.impex.automation.pipelet;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * Gets the files in the given directory which match the given regular expression.
 */
public class GetFiles extends Pipelet
{

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        final File dir = dict.getRequired("Directory");
        final String patternString = dict.getRequired("Regex");
        final Pattern pattern = Pattern.compile(patternString);
        final FileFilter filter = new FileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                boolean matches = pattern.matcher(file.getName()).matches();
                return matches && !file.isDirectory();
            }
        }; 
        List<File> files = Arrays.asList(dir.listFiles(filter));
        dict.put("Files", files);
        return PIPELET_NEXT;
    }

}
