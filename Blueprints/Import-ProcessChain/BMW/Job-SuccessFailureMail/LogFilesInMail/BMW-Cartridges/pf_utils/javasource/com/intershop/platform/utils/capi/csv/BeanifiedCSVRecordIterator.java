package com.intershop.platform.utils.capi.csv;

import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * This iterator wraps the {@link CSVRecord} iterator as returned by {@link CSVFormat#parse(java.io.Reader)}.
 * It returns {@link BeanifiedCSVRecord BeanifiedCSVRecords}.
 * 
 * @author JCMeyer
 * @version 1.0, 2015-08-03
 * @since 3.1.0
 */
public class BeanifiedCSVRecordIterator implements Iterator<BeanifiedCSVRecord>
{
    protected Iterator<CSVRecord> delegate = null;

    protected BeanifiedCSVRecordIterator()
    {
    }

    public BeanifiedCSVRecordIterator(Iterator<CSVRecord> delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext()
    {
        return delegate.hasNext();
    }

    @Override
    public BeanifiedCSVRecord next()
    {
        return new BeanifiedCSVRecord(delegate.next());
    }

    @Override
    public void remove()
    {
        delegate.remove();
    }
}
