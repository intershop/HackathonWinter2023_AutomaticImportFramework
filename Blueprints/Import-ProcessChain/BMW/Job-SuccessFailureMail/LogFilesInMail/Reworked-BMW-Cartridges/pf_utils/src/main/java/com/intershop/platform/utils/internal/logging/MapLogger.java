package com.intershop.platform.utils.internal.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.intershop.beehive.core.internal.pipeline.PipelineDictionaryImpl;
import com.intershop.beehive.core.internal.request.StorefrontSession;
import com.intershop.beehive.core.pipelet.common.Convert;

/**
 * Verbose logging of pipeline dictionaries, store front sessions, generic maps. Can be instantiated via {@link Convert}.
 * <p />
 * Simply call its custom {@link #toString()} method to get logged the content of the provided input. 
 * 
 * @author JCMeyer
 * @version 1.0, 2018-08-23
 * @since 3.7.0
 * 
 */
public class MapLogger
{
    protected Map<String, Object> map = null;
    
    /**
     * Instantiates a pipeline dictionary logger. Intentionally makes use of internal {@link PipelineDictionaryImpl} in
     * order to allow simple instantiation via {@link Convert}.
     * 
     * @param dict a pipeline dictionary to log the contents of
     */
    public MapLogger(PipelineDictionaryImpl dict)
    {
         this.map = dict;
    }
    
    /**
     * Instantiates a store front session logger. Converts the session object into a map using {@link BeanUtils} and
     * works similar to the generic map logger from there.
     * 
     * @param session a store front session to log the contents of
     */
    public MapLogger(StorefrontSession session)
    {
        this.map = new HashMap<>();
        Iterator<String> sessionKeysIterator = session.createDictionaryKeyIterator();
        while (sessionKeysIterator.hasNext())
        {
            String key = sessionKeysIterator.next();
            this.map.put(key, session.getObject(key));
        }
    }
    
    /**
     * Instantiates a generic map logger.
     * 
     * @param map the map to log the contents of
     */
    public MapLogger(Map<String, Object> map)
    {
         this.map = map;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        List<Entry<String, Object>> entryList = new ArrayList<>(map.entrySet());
        Collections.sort(entryList, new DictionaryKeyComparator());
        
        for (Entry<String, Object> entry : entryList)
        {
            if (equals(entry.getValue()))
            {
                continue;
            }
            
            builder.append(entry.getKey());
            builder.append(" = [");
            builder.append(entry.getValue());
            builder.append("]\n");
        }
        
        return map.getClass().getName() + "@" + map.hashCode() + "\n" + builder.toString();
    }
    
    protected class DictionaryKeyComparator implements Comparator<Entry<String, Object>>
    {
        @Override
        public int compare(Entry<String, Object> entryAlpha, Entry<String, Object> entryBeta)
        {
            return entryAlpha.getKey().compareTo(entryBeta.getKey());
        }
    }
    
}
