package com.intershop.component.processstatistics.internal;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.intershop.beehive.businessobject.capi.BusinessObjectContext;
import com.intershop.beehive.core.capi.domain.AbstractExtensibleObjectBO;
import com.intershop.beehive.core.capi.locking.Process;
import com.intershop.component.processstatistics.capi.ProcessStatistics;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBO;

public class ORMProcessStatisticsBOImpl extends AbstractExtensibleObjectBO<ProcessStatistics> implements ProcessStatisticsBO
{
    ORMProcessStatisticsBOImpl(ProcessStatistics delegate, BusinessObjectContext context)
    {
        super(delegate, context);
    }
    
    @Override
    public String getType()
    {
        return getExtensibleObject().getType();
    }

    @Override
    public String getName()
    {
        return getExtensibleObject().getName();
    }

    @Override
    public void setName(String name)
    {
        if (name != null)
        {
            getExtensibleObject().setName(name);
            objectChanged();
        }
        else
        {
            throw new IllegalArgumentException("ProcessStatisticsBO.setName(String name): name is null but JavaDoc reads: 'name cannot be null'");
        }
    }

    @Override
    public Date getStartDate()
    {
        return getExtensibleObject().getStartDate();
    }

    @Override
    public void setStartDate(Date startDate)
    {
        getExtensibleObject().setStartDate(startDate);
        objectChanged();
    }

    @Override
    public Date getEndDate()
    {
        return getExtensibleObject().getEndDate();
    }

    @Override
    public void setEndDate(Date endDate)
    {
        getExtensibleObject().setEndDate(endDate);
        objectChanged();
    }

    @Override
    public String getResult()
    {        
        return getExtensibleObject().getResultNull() ? null : getExtensibleObject().getResult();
    }    
        
    @Override
    public void setResult(String result)
    {
        if (result != null)
        {
            getExtensibleObject().setResult(result);
        }
        else
        {
            getExtensibleObject().setResultNull(true);
        }
        objectChanged();
    }

    @Override
    public Process getProcess()
    {
        return getExtensibleObject().getProcess();
    }

    @Override
    public void setProcess(Process process)
    {
        getExtensibleObject().setProcess(process);
        objectChanged();
    }

    public Long getDurationHours()
    {
        Long result = null;
        Date start = getStartDate();
        Date end = getEndDate();
        if (start != null && end != null)
        {
            long durationMillies = end.getTime() - start.getTime();
            result = Long.valueOf(TimeUnit.MILLISECONDS.toHours(durationMillies));
        }
        return result;
    }

    public Long getDurationMinutes()
    {
        Long result = null;
        Date start = getStartDate();
        Date end = getEndDate();
        if (start != null && end != null)
        {
            long durationMillies = end.getTime() - start.getTime();
            result = Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(durationMillies)) % 60;
        }
        return result;
    }
}
