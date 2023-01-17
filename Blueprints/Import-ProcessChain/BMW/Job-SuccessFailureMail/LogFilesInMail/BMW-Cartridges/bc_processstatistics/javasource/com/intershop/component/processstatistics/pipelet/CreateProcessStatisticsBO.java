package com.intershop.component.processstatistics.pipelet;

import java.util.Date;

import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBO;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBORepository;

/**
 * The ProcessStatisticsBO business interface is used for statistics information about any kind of processes. It is
 * usually used for collecting information about a replication process or about an import process chain.
 */
public class CreateProcessStatisticsBO extends Pipelet
{
    protected static String DN_ProcessStatisticsBORepository = "ProcessStatisticsBORepository";

    protected static String DN_Domain = "Domain";

    protected static String DN_Type = "Type";

    protected static String DN_Name = "Name";

    protected static String DN_StartDate = "StartDate";

    protected static String DN_ProcessStatisticsBO = "ProcessStatisticsBO";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        ProcessStatisticsBORepository processStatisticsBORepository = aPipelineDictionary.getRequired(DN_ProcessStatisticsBORepository);
        
        Domain domain = aPipelineDictionary.getRequired(DN_Domain);
        
        String type = aPipelineDictionary.getRequired(DN_Type);
        
        String name = aPipelineDictionary.getRequired(DN_Name);
        
        Date startDate = aPipelineDictionary.getRequired(DN_StartDate);
        
        ProcessStatisticsBO processStatisticsBO = processStatisticsBORepository.createProcessStatistics(domain, type, name, startDate);
        
        aPipelineDictionary.put(DN_ProcessStatisticsBO, processStatisticsBO);

        return PIPELET_NEXT;
    }
}
