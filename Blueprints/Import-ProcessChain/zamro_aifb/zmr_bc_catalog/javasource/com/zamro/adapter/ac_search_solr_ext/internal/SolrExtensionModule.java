package com.zamro.adapter.ac_search_solr_ext.internal;

import com.google.inject.AbstractModule;
import com.intershop.adapter.search_solr.internal.SolrIndexExtension;
import com.intershop.adapter.search_solr.internal.SolrInstanceFactory;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class SolrExtensionModule extends AbstractModule
{

    @Override
    protected void configure()
    {
        install(new FactoryModuleBuilder().implement(SolrIndexExtension.class, ZMRSolrIndexExtension.class)
                        .build(SolrInstanceFactory.class));

    }

}
