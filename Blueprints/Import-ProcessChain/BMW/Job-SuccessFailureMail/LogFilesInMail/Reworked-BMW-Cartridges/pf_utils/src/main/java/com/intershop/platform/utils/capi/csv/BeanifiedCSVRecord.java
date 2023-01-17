package com.intershop.platform.utils.capi.csv;

import org.apache.commons.csv.CSVRecord;

/**
 * This class provides a JavaBean compatible getter to retrieve {@link CSVRecord}
 * values so that it can be accessed on pipeline layer using Enfinity Object Path
 * Expressions.
 * <p />
 * Unfortunately, can't simply extend the {@link CSVRecord} and thus have to use
 * the delegate pattern as Apache people liked the idea of a final class. 
 * 
 * @author JCMeyer
 * @version 1.0, 2015-08-03
 * @since 3.1.0
 */
public class BeanifiedCSVRecord
{
    protected CSVRecord delegate = null;

    protected BeanifiedCSVRecord()
    {
    }

    public BeanifiedCSVRecord(CSVRecord delegate)
    {
        this.delegate = delegate;
    }

    /**
     * See: {@link CSVRecord#get(String)}
     */
    public String getValue(String fieldName)
    {
        return delegate.get(fieldName);
    }

    // feel free to add your getters as needed
}
