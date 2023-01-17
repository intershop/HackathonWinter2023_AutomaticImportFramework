package com.intershop.platform.utils.pipelet.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

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
import com.intershop.beehive.foundation.util.ResettableIterator;
import com.intershop.beehive.foundation.util.ResettableIteratorImpl;

/**
 * Filters an iterator by an attribute of the contained objects. Output is a new
 * iterator.
 * 
 * @author h.grasser@intershop.de
 * @version 1.0, 05/10/2011
 * @comment Basic functionality.
 * 
 */

public class FilterIteratorByAttribute extends Pipelet
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
     * Constant used to access pipelet configuration with key
     * 'RegularExpression'
     */
    public static final String CN_REGULAR_EXPRESSION = "RegularExpression";
    /**
     * Member attribute that holds the pipelet configuration value
     * 'RegularExpression'
     */
    private String cfg_regularExpression_text;
    /**
     * Constant used to access pipelet configuration with key 'Include'
     */
    public static final String CN_INCLUDE = "Include";
    /**
     * Member attribute that holds the pipelet configuration value 'Include'
     */
    private String cfg_include = "";
    /**
     * Constant used to access pipelet configuration with key 'Mode'
     */
    public static final String CN_MODE = "Mode";
    /**
     * Member attribute that holds the pipelet configuration value 'Mode'
     */
    private String cfg_mode = "";
    /**
     * Constant used to access the pipeline dictionary with key 'Iterator'
     */
    public static final String DN_ITERATOR = "Iterator";
    /**
     * Constant used to access the pipeline dictionary with key
     * 'FilteredIterator'
     */
    public static final String DN_FILTERED_ITERATOR = "FilteredIterator";

    private ObjectPathMgr objectPathMgr;
    private LookupStrategy lookupStrategy;
    private static final String MODE_EQUALS = "Equals";
    private static final String MODE_MATCH = "Match";
    private static final String MODE_CONTAINS = "Contains";
    private static final String MODE_IN_MAP = "InMap";
    private static final String COLLECTION_CONTAINS = "CollectionContains";
    private static final String INCLUDE_NOT_MATCHING = "Not Matching";

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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        // lookup 'Iterator' in pipeline dictionary
        Object collection = dict.get(DN_ITERATOR);
        if (null == collection)
        {
            throw new PipeletExecutionException(
                            "Mandatory input parameter 'Iterator' not available in pipeline dictionary.");
        }
        Iterator iterator;
        if (collection instanceof Iterator)
        {
            iterator = (Iterator)collection;
        }
        else if (collection instanceof Enumeration)
        {
            iterator = new ResettableIteratorImpl((Enumeration)collection);
        }
        else if (collection instanceof Collection)
        {
            iterator = ((Collection)collection).iterator();
        }
        else
        {
            throw new PipeletExecutionException("Mandatory input parameter 'Iterator' uses class "
                            + collection.getClass().getName());
        }

        String regex = (String)dict.get("RegularExpression");
        if (regex == null)
        {
            regex = cfg_regularExpression_text;
        }
        if (null == regex && !MODE_EQUALS.equals(cfg_mode) && !MODE_IN_MAP.equals(cfg_mode)
                        && !COLLECTION_CONTAINS.equals(cfg_mode))
        {
            throw new PipeletExecutionException(
                            "Mandatory input parameter 'RegularExpression' not available in pipeline dictionary.");

        }

        Object value = dict.get("Value");

        Pattern regularExpression = null;
        if (MODE_IN_MAP.equals(cfg_mode))
        {
            if (value instanceof ResettableIterator)
            {
                ResettableIterator i = ((ResettableIterator)value).cloneIterator();
                HashSet data = new HashSet();
                while(i.hasNext())
                {
                    data.add(i.next());
                }
                value = data;
            }
        }
        else if (!MODE_EQUALS.equals(cfg_mode) && !COLLECTION_CONTAINS.equals(cfg_mode))
        {
            try
            {
                regularExpression = Pattern.compile(regex);
            }
            catch(Exception e)
            {
                throw new PipeletExecutionException("Mandatory attribute 'RegularExpression' is no valid regex.", e);
            }
        }
        ArrayList<Object> rc = new ArrayList<Object>();
        while(iterator.hasNext())
        {
            Object i = iterator.next();
            Object element = objectPathMgr.lookupObject(i, lookupStrategy, cfg_attribute);
            boolean equal = false;
            if (element == null)
            {
                equal = MODE_EQUALS.equals(cfg_mode) && value == null;
            }
            else if (MODE_MATCH.equals(cfg_mode))
            {
                equal = regularExpression.matcher(element.toString()).matches();
            }
            else if (MODE_CONTAINS.equals(cfg_mode))
            {
                equal = regularExpression.matcher(element.toString()).find();
            }
            else if (MODE_EQUALS.equals(cfg_mode))
            {
                equal = value != null && element.equals(value);
            }
            else if (MODE_IN_MAP.equals(cfg_mode))
            {
                if (value instanceof Map)
                {
                    equal = ((Map)value).containsKey(element);
                }
                else if (value instanceof Collection)
                {
                    equal = ((Collection)value).contains(element);
                }
            }
            else if (COLLECTION_CONTAINS.equals(cfg_mode))
            {
                if (element instanceof Map)
                {
                    equal = ((Map)element).containsKey(value);
                }
                else if (element instanceof Collection)
                {
                    equal = ((Collection)element).contains(value);
                }
            }
            equal ^= INCLUDE_NOT_MATCHING.equals(cfg_include);
            if (equal)
            {
                rc.add(i);
            }
        }

        // store 'FilteredIterator' in pipeline dictionary
        dict.put(DN_FILTERED_ITERATOR, new PageableIteratorImpl(rc.iterator(), rc.size()));
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
        // store 'Mode' config value in field variable
        cfg_mode = (String)getConfiguration().get(CN_MODE);

        // store 'Include' config value in field variable
        cfg_include = (String)getConfiguration().get(CN_INCLUDE);
        if (null == cfg_include)
        {
            throw new PipelineInitializationException(
                            "Mandatory attribute 'Include' not found in pipelet configuration.");
        }

        // store 'RegularExpression' config value in field variable
        cfg_regularExpression_text = (String)getConfiguration().get(CN_REGULAR_EXPRESSION);

        // store 'Attribute' config value in field variable
        cfg_attribute = (String)getConfiguration().get(CN_ATTRIBUTE);
        if (null == cfg_attribute)
        {
            throw new PipelineInitializationException(
                            "Mandatory attribute 'Attribute' not found in pipelet configuration.");
        }

        objectPathMgr = ObjectPathMgrImpl.getInstance();

        lookupStrategy = new PositionLookupStrategy();
        ((PositionLookupStrategy)lookupStrategy).setBodyStrategy(PipelineDictionaryImpl.pipelineStrategy
                        .getBodyStrategy());
        ((PositionLookupStrategy)lookupStrategy).setTailStrategy(PipelineDictionaryImpl.pipelineStrategy
                        .getTailStrategy());
    }
}