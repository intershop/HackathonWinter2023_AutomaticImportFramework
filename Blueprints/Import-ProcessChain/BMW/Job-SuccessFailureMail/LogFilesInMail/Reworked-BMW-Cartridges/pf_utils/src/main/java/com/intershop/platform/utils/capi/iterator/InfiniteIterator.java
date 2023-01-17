package com.intershop.platform.utils.capi.iterator;

import java.util.Iterator;

import com.intershop.beehive.foundation.util.ResettableIterator;

/**
 * This class implements an infinite Iterator meaning method {{@link #hasNext()} will
 * never return <code>false</code>.
 * 
 * @author JCMeyer
 * @version 1.0, 2015-12-11
 * @since 3.2.0
 * 
 * @author t.koerbs@intershop.de
 * @version 1.0, 2017-02-21
 * @since 3.4.0
 *
 *      Use ResettableIterator instead of Iterator to avoid auto-generated ResettableIterators caching all elements.
 *
 */
public class InfiniteIterator implements ResettableIterator<Object>
{
    private static final long serialVersionUID = 1L;
    
    public Object element;

    /**
     * Constructor accepting an object to be later returned through method {@link #next()}.
     * 
     * @param element the one and only iterator element to be returned whenever method {@link #next()} is called
     */
    public InfiniteIterator(Object element)
    {
        this.element = element;
    }

    /**
     * An constructor accepting an object to be later returned through method
     * {@link #next()}.
     * <p>
     * Using this constructor allows to instantiate the iterator using a simple
     * Enfinity Object Path Expression.
     * 
     * @param element the one and only iterator element to be returned whenever method {@link #next()} is called
     */
    public InfiniteIterator(String element)
    {
        this((Object)element);
    }

    @Override
    public boolean hasNext()
    {
        return true;
    }

    @Override
    public Object next()
    {
        return element;
    }

    @Override
    public void remove()
    {
    }

    @Override
    public void close() throws Exception
    {
    }

    @Override
    public void reset()
    {
    }

    @Override
    public ResettableIterator<Object> cloneIterator()
    {
        return new InfiniteIterator(element);
    }

    @Override
    public Iterator<Object> toSequence()
    {
        return this;
    }
}
