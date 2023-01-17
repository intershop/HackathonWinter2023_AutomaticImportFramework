package com.intershop.component.processstatistics.internal;

import java.util.Collection;
import java.util.Collections;

import com.intershop.beehive.core.capi.common.SortingAttributesProvider;

public class ProcessStatisticsSortingAttributesProvider implements SortingAttributesProvider
{
    @Override
    public Collection<String> getSortingAttributes()
    {
        return Collections.emptyList();
    }
}
