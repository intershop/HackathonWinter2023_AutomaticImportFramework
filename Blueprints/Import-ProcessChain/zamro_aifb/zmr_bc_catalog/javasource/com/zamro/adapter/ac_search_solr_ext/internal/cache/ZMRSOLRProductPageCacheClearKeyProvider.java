/**
 * 
 */
package com.zamro.adapter.ac_search_solr_ext.internal.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.intershop.beehive.cache.capi.common.ClassCacheClearKey;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.pagecache.PageCacheClearKey;
import com.intershop.beehive.core.capi.pagecache.PageCacheClearKeyProvider;
import com.intershop.beehive.core.capi.pagecache.PageCacheKeywordsProvider;
import com.zamro.adapter.ac_search_solr_ext.internal.ZMRSOLRProduct;

/**
 * @author developer
 *
 */
public class ZMRSOLRProductPageCacheClearKeyProvider implements PageCacheClearKeyProvider, PageCacheKeywordsProvider
{

    @Override
    public boolean canProvideCacheClearKey(Object obj)
    {
     // We can provide a cache clear key for promotions only
        return (obj instanceof ZMRSOLRProduct);
    }

    @Override
    public PageCacheClearKey provideCacheClearKey(Object obj)
    {
        ZMRSOLRProduct prod = (ZMRSOLRProduct)obj;
        Collection<String> keywords = new HashSet<String>(1);
        // We will use the UUID as keyword to identify the promotion
        keywords.add(prod.getUUID());
        Collection<Domain> affectedSites = new HashSet<Domain>(1);
        // The promotion is not shared, so we only have one affected site
        affectedSites.add(prod.getDomain().getSite());
        return new PageCacheClearKey(keywords, affectedSites);
    }

    @Override
    public Collection<String> getKeywords(Object obj)
    {
        List<String> keywords = new ArrayList<String>();
        if (obj instanceof ZMRSOLRProduct)
        {
            // Called by the PageCacheMgr at the time a page is rendered and cached to determine the keywords,
            // we have to provide a keyword to identify the promotion (this is the UUID) as well
            // as a keyword to be able to invalidate a page if invalidation is requested for all promotions (this
            // is the fully qualified class name of the promotion class)
            ZMRSOLRProduct prod = (ZMRSOLRProduct)obj;
            keywords = (List<String>)prod.getCacheKeys();
        }
        else if (obj instanceof ClassCacheClearKey)
        {
            // Called by the PageCache instance at the time cache clear is requested to determine the
            // keyword to remove all pages which display promotions
            Class<?> selectiveClass = ((ClassCacheClearKey)obj).getIdentifiedClass();
            if (ZMRSOLRProduct.class.isAssignableFrom(selectiveClass))
            {
                keywords.add(ZMRSOLRProduct.class.getName());
            }
        }
        return keywords;
    }

    @Override
    public boolean canProvideKeywords(Object obj)
    {
        return (obj instanceof ZMRSOLRProduct) || (obj instanceof ClassCacheClearKey);
    }


    

}
