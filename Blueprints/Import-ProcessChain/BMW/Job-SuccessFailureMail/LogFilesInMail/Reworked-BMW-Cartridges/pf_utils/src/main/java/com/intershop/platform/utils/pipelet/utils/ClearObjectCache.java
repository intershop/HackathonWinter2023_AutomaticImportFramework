package com.intershop.platform.utils.pipelet.utils;

import com.intershop.beehive.cache.capi.engine.CacheEngine;
import com.intershop.beehive.core.capi.component.ComponentMgr;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.foundation.util.LRUCache;
import com.intershop.beehive.foundation.util.LRUHashMap;

/**
 * This pipelet clears the object cache with the given CacheID
 * 
 * @author aseitz@intershop.de
 * @version 1.0, 10.11.2015
 *
 */
public class ClearObjectCache extends Pipelet
{

    public static final String DN_CACHE_ID = "CacheId";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        String cacheId = (String)dict.getRequired(DN_CACHE_ID);
        
        CacheEngine cacheEngine = NamingMgr.getManager(ComponentMgr.class).getGlobalComponentInstance("CacheEngine");
        LRUCache<String, Object> productPartCache = (LRUHashMap<String, Object>) cacheEngine.getCache(cacheId);

        if (productPartCache != null) {
            productPartCache.clear();
        }
        return PIPELET_NEXT;
    }

}
