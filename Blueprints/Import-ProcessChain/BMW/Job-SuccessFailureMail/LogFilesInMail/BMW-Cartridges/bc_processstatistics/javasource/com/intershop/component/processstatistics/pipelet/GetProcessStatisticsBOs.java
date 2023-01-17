package com.intershop.component.processstatistics.pipelet;

import java.util.Collection;
import java.util.Date;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBO;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBORepository;

/**
 * Get a collection of ProcessStatisticsBO objects. Type is reqired, that means one can only retrieve
 * ProcessStatisticsBOs of a single type at a time. The returned collection is sorted by start date, newest first.
 */
public class GetProcessStatisticsBOs extends Pipelet
{
    protected static String DN_ProcessStatisticsBORepository = "ProcessStatisticsBORepository";

    protected static String DN_Type = "Type";

    protected static String DN_OldestStartDate = "OldestStartDate";

    protected static String DN_ProcessStatisticsBOs = "ProcessStatisticsBOs";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        ProcessStatisticsBORepository processStatisticsBORepository = aPipelineDictionary.getRequired(DN_ProcessStatisticsBORepository);
        
        String type = aPipelineDictionary.getRequired(DN_Type);
        
        Date oldestStartDate = aPipelineDictionary.getRequired(DN_OldestStartDate);
        
        Collection<? extends ProcessStatisticsBO> processStatisticsBOs = processStatisticsBORepository.getProcessStatistics(type, oldestStartDate);
        
        aPipelineDictionary.put(DN_ProcessStatisticsBOs, processStatisticsBOs);

        return PIPELET_NEXT;
    }
}
