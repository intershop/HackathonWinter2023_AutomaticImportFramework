package com.intershop.platform.utils.pipelet.iterator;

import java.util.Iterator;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.platform.utils.capi.iterator.InfiniteIterator;

/**
 * Creates an unlimited, endless, infinite Iterator where hasNext always returns true.
 */
public class CreateInfiniteIterator extends Pipelet
{
    public static Object dummyElement = new Object()
    {
        @Override
        public String toString()
        {
            return "This is just a static dummy object used by InfiniteIterator: " + super.toString();
        }
    };

    /**
     * Constant used to access the pipeline dictionary with key 'Element'.
     */
    protected static String DN_ELEMENT = "Element";

    /**
     * Constant used to access the pipeline dictionary with key 'Iterator'.
     */
    protected static String DN_ITERATOR = "Iterator";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        Object element = aPipelineDictionary.getOptional(DN_ELEMENT, dummyElement);
        
        Iterator<Object> iterator = new InfiniteIterator(element);
        
        aPipelineDictionary.put(DN_ITERATOR, iterator);

        return PIPELET_NEXT;
    }

}
