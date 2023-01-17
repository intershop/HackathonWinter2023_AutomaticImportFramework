package com.zamro.component.internal.searchindex.common;

import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.component.search.capi.service.SearchIndexServiceMgr;

public class SearchIndexReloader
{
    protected String domainName = null;

    public SearchIndexReloader(String domainName)
    {
        this.domainName = domainName;
    }

    public void reload()
    {
        SearchIndexServiceMgr searchIndexServiceMgr = NamingMgr.getManager(SearchIndexServiceMgr.class);
        searchIndexServiceMgr.sendEventReloadAllSearchIndexes(domainName);
    }

}
