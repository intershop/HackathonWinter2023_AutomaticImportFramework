package com.intershop.platform.utils.pipelet.file;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.platform.utils.capi.csv.BeanifiedCSVRecordIterator;

/**
 * This pipelet is used to read lines of CSV values from a given text file. The
 * file needs to provide a header line naming the columns.
 * <p />
 * The error connector is used in case read or parsing operation fails.
 * 
 * @author JCMeyer
 * @version 1.0, 2015-08-03
 * @since 3.1.0
 */
public class ReadCSVFile extends Pipelet
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
     * An iterator of CSV line records as read from the input file.
     */
    public static final String DN_RECORDS = "Records";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        File file = dict.getRequired(DN_FILE);

        CSVFormat csvFormat = CSVFormat.EXCEL.withDelimiter(';').withHeader();
        Iterable<CSVRecord> records = null;
        try
        {
            records = csvFormat.parse(new FileReader(file));
        }
        catch (IOException ex)
        {
            Logger.error(this, "Error parsing CSV file.", ex);
            return PIPELET_ERROR;
        }

        dict.put(DN_RECORDS, new BeanifiedCSVRecordIterator(records.iterator()));

        return PIPELET_NEXT;
    }

}
