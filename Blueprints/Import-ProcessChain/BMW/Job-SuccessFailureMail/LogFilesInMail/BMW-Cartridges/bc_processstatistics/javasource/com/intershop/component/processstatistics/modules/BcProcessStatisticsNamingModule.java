package com.intershop.component.processstatistics.modules;

import javax.inject.Singleton;

import com.intershop.beehive.core.capi.common.SortingAttributesProvider;
import com.intershop.beehive.core.capi.naming.AbstractNamingModule;
import com.intershop.component.processstatistics.internal.ProcessStatisticsPO;
import com.intershop.component.processstatistics.internal.ProcessStatisticsSortingAttributesProvider;

public class BcProcessStatisticsNamingModule extends AbstractNamingModule
{
    @Override
    protected void configure()
    {
        bindProvider(SortingAttributesProvider.class, ProcessStatisticsPO.class.getName() + "_SortingAttributeProvider", named("ProcessStatisticsPOSortingAttributesProvider"))
                .to(ProcessStatisticsSortingAttributesProvider.class).in(Singleton.class);
    }
}
