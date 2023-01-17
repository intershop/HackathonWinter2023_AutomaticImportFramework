package com.intershop.platform.utils.pipelet.orm;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.orm.capi.cache.CacheManager;
import com.intershop.beehive.orm.capi.common.ORMObjectKey;

public class RemoveObjectFromORMCache extends Pipelet
{
    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        ORMObjectKey objectKey = dict.getRequired("ORMObjectKey");
        CacheManager cacheManager = dict.getRequired("CacheManager");
        
        cacheManager.removeObject(objectKey);
        
        return PIPELET_NEXT;
    }
}
