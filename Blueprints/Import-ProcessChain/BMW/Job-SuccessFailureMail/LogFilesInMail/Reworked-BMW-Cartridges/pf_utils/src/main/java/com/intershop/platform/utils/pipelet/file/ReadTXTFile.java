package com.intershop.platform.utils.pipelet.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * This pipelet is used to read lines of TXT values from a given text file. The
 * file needs to provide a header line naming the columns.
 * <p />
 * The error connector is used in case read or parsing operation fails.
 * 
 * @authorhmordt
 * @version 1.0, 2019-06-21
 * @since 3.1.0
 */
public class ReadTXTFile extends Pipelet
{
    /**
     * Constant used to access the pipeline dictionary with key 'File'
     * 
     * The file object to read the values from.
     */
    public static final String DN_FILE = "File";

    /**
     * Constant used to access the pipeline dictionary with key 'Records'
     * 
     * An iterator of TXT line records as read from the input file.
     */
    public static final String DN_LINES = "Lines";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        File file = dict.getRequired(DN_FILE);
        FileReader reader;
        ArrayList<String> lines = new ArrayList<>();
        try
        {
            reader = new FileReader (file);
            BufferedReader fileDRader = new BufferedReader( reader );
            
            String line = "";

            while((line = fileDRader.readLine()) != null) 
            {
                lines.add( line );
            }
        }
        catch(FileNotFoundException e)
        {
            Logger.error(this, "File {} not found!", file.getName() );
            return PIPELET_ERROR;
        }
        catch(IOException e)
        {
            Logger.error(this, "I/O Exception {}!", file.getName() );
            return PIPELET_ERROR;
        }
        
        dict.put(DN_LINES, lines.iterator());

        return PIPELET_NEXT;
    }

}
