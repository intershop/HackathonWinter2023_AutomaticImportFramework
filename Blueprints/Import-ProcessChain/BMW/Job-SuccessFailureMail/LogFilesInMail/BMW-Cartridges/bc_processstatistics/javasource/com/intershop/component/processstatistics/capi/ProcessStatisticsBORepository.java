package com.intershop.component.processstatistics.capi;

import java.util.Collection;
import java.util.Date;

import com.intershop.beehive.businessobject.capi.BusinessObjectRepository;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.locking.Process;

public interface ProcessStatisticsBORepository extends BusinessObjectRepository
{
    /**
     * The ID of the created extensions which can be used to get them from the business object later.
     */
    public static final String EXTENSION_ID = "ProcessStatisticsBORepository";

    public ProcessStatisticsBO createProcessStatistics(Domain domain, String type, String name, Date startDate);

    public void deleteProcessStatistics(ProcessStatisticsBO processStatistics);

    public Collection<? extends ProcessStatisticsBO> getProcessStatistics(String type, Date oldestStartDate);

    public ProcessStatisticsBO getProcessStatisticsByProcess(Process process);
}
