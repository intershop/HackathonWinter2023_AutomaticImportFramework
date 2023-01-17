package com.intershop.platform.utils.pipelet.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;
import com.intershop.beehive.core.internal.objectpath.LookupStrategy;
import com.intershop.beehive.core.internal.objectpath.ObjectPathMgr;
import com.intershop.beehive.core.internal.objectpath.ObjectPathMgrImpl;
import com.intershop.beehive.core.internal.objectpath.PositionLookupStrategy;
import com.intershop.beehive.core.internal.paging.PageableIteratorImpl;
import com.intershop.beehive.core.internal.pipeline.PipelineDictionaryImpl;

/**
 * Sorts an iterator by an element contained within the iterator's objects.
 * 
 * @author h.grasser@intershop.de
 * @version 1.0, 05/10/2011
 * @comment Basic functionality.
 * 
 */

public class SortIteratorByAttribute extends Pipelet
{

    /**
     * Constant used to access pipelet configuration with key 'Attribute'
     */
    public static final String CN_ATTRIBUTE = "Attribute";
    /**
     * Member attribute that holds the pipelet configuration value 'Attribute'
     */
    private String cfg_attribute = "";
    /**
     * Constant used to access pipelet configuration with key 'Order'
     */
    public static final String CN_ORDER = "Order";
    /**
     * Member attribute that holds the pipelet configuration value 'Order'
     */
    private String cfg_order = "";
    /**
     * Constant used to access the pipeline dictionary with key 'Iterator'
     */
    public static final String DN_ITERATOR = "Iterator";

    public static final String DN_ATTRIBUTE_NAME = "AttributeName";
    /**
     * Constant used to access the pipeline dictionary with key 'SortedIterator'
     */
    public static final String DN_SORTED_ITERATOR = "SortedIterator";

    private ObjectPathMgr objectPathMgr;

    private LookupStrategy lookupStrategy;

    /**
     * The pipelet's execute method is called whenever the pipelets gets
     * executed in the context of a pipeline and a request. The pipeline
     * dictionary valid for the currently executing thread is provided as a
     * parameter.
     * 
     * @param dict
     *            The pipeline dictionary to be used.
     * @throws PipeletExecutionException
     *             Thrown in case of severe errors that make the pipelet execute
     *             impossible (e.g. missing required input data).
     */
    @SuppressWarnings("rawtypes")
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        // lookup 'Iterator' in pipeline dictionary
        Object elements = dict.get(DN_ITERATOR);
        if (null == elements)
        {
            throw new PipeletExecutionException(
                            "Mandatory input parameter 'Iterator' not available in pipeline dictionary.");

        }

        String attributeName = (String)dict.get(DN_ATTRIBUTE_NAME);
        if (attributeName == null)
        {
            attributeName = cfg_attribute;
        }

        if (attributeName == null)
        {
            throw new PipeletExecutionException(
                            "Mandatory input parameter 'AttributeName' not available in pipeline dictionary.");
        }

        Iterator iterator = null;
        if (elements instanceof Iterator<?>)
        {
            iterator = (Iterator)elements;
        }
        else if (elements instanceof Iterable<?>)
        {
            iterator = ((Iterable)elements).iterator();
        }
        if (null == iterator)
        {
            throw new PipeletExecutionException(
                            "Mandatory input parameter 'Iterator' has incorrect type in pipeline dictionary: "
                                            + elements.getClass().getCanonicalName());
        }

        ArrayList<Object> rc = new ArrayList<Object>();
        while(iterator.hasNext())
        {
            Object element = iterator.next();
            rc.add(element);
        }
        Collections.sort(rc, new ObjectComparator(attributeName));

        // store 'SortedIterator' in pipeline dictionary
        dict.put(DN_SORTED_ITERATOR, new PageableIteratorImpl<Object>(rc.iterator(), rc.size()));
        return PIPELET_NEXT;
    }

    /**
     * The pipelet's initialization method is called whenever the pipeline used
     * to read and process pipelet configuration values that are required during
     * the pipelet execution later on.
     * 
     * @throws PipelineInitializationException
     *             Thrown if some error occured when reading the pipelet
     *             configuration.
     */
    public void init() throws PipelineInitializationException
    {
        // store 'Order' config value in field variable
        cfg_order = (String)getConfiguration().get(CN_ORDER);
        if (null == cfg_order)
        {
            throw new PipelineInitializationException("Mandatory attribute 'Order' not found in pipelet configuration.");
        }

        // store 'Attribute' config value in field variable
        cfg_attribute = (String)getConfiguration().get(CN_ATTRIBUTE);

        objectPathMgr = ObjectPathMgrImpl.getInstance();

        lookupStrategy = new PositionLookupStrategy();
        ((PositionLookupStrategy)lookupStrategy).setBodyStrategy(PipelineDictionaryImpl.pipelineStrategy
                        .getBodyStrategy());
        ((PositionLookupStrategy)lookupStrategy).setTailStrategy(PipelineDictionaryImpl.pipelineStrategy
                        .getTailStrategy());
    }

    private class ObjectComparator implements Comparator<Object>
    {
        private String attributeName;

        public ObjectComparator(String attributeName)
        {
            this.attributeName = attributeName;
        }

        @Override
        public int compare(Object o1, Object o2)
        {
            Object leftValue;
            Object rightValue;
            if (!".".equals(attributeName))
            {
                leftValue = objectPathMgr.lookupObject(o1, lookupStrategy, attributeName);
                rightValue = objectPathMgr.lookupObject(o2, lookupStrategy, attributeName);
            }
            else
            {
                leftValue = o1;
                rightValue = o2;
            }
            return compareValues(leftValue, rightValue);
        }

        @SuppressWarnings("unchecked")
        private int compareValues(Object left, Object right)
        {
            int rc;
            if (left == null && right == null)
            {
                rc = 0;
            }
            else if (left == null)
            {
                rc = -1;
            }
            else if (right == null)
            {
                rc = 1;
            }
            else if ("ASC".equals(cfg_order) || "DESC".equals(cfg_order))
            {
                // non case sensitive string comparison
                rc = left.toString().compareToIgnoreCase(right.toString());
            }
            else
            {
                rc = ((Comparable<Object>)left).compareTo(right);
            }
            if ("Descending".equals(cfg_order) || "DESC".equals(cfg_order))
            {
                rc = -rc;
            }
            return rc;
        }
    }
}