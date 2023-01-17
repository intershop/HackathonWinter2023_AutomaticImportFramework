package com.intershop.component.processstatistics.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.intershop.beehive.core.capi.domain.AbstractPersistentObjectBOExtension;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.locking.Process;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.util.ObjectMapper;
import com.intershop.beehive.core.capi.util.ObjectMappingCollection;
import com.intershop.beehive.orm.capi.common.ORMHelper;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBO;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBORepository;
import com.intershop.component.repository.capi.RepositoryBO;

public class ORMProcessStatisticsBORepositoryImpl extends AbstractPersistentObjectBOExtension<RepositoryBO>
                implements ProcessStatisticsBORepository, ObjectMapper<ProcessStatisticsPO, ORMProcessStatisticsBOImpl>
{
    public ORMProcessStatisticsBORepositoryImpl(String extensionID, RepositoryBO extendedObject)
    {
        super(extensionID, extendedObject);
    }

    @Override
    public String getRepositoryID()
    {
        return null;
    }

    @Override
    public ProcessStatisticsBO createProcessStatistics(Domain domain, String type, String name, Date startDate)
    {
        ProcessStatisticsPOFactory processStatisticsPOFactory = (ProcessStatisticsPOFactory)NamingMgr.getInstance()
                        .lookupFactory(ProcessStatisticsPO.class);

        ProcessStatisticsPO processStatisticsPO = processStatisticsPOFactory.create(domain);
        processStatisticsPO.setType(type);
        processStatisticsPO.setName(name);
        processStatisticsPO.setStartDate(startDate);

        ORMProcessStatisticsBOImpl ormProcessStatisticsBOImpl = new ORMProcessStatisticsBOImpl(processStatisticsPO,
                        getContext());
        ormProcessStatisticsBOImpl.objectCreated();

        return ormProcessStatisticsBOImpl;
    }

    @Override
    public void deleteProcessStatistics(ProcessStatisticsBO processStatistics)
    {
        ProcessStatisticsPOFactory processStatisticsPOFactory = (ProcessStatisticsPOFactory)NamingMgr.getInstance()
                        .lookupFactory(ProcessStatisticsPO.class);

        processStatisticsPOFactory.remove(new ProcessStatisticsPOKey(processStatistics.getID()));
    }

    @Override
    public Collection<? extends ProcessStatisticsBO> getProcessStatistics(String type, Date oldestStartDate)
    {
        ProcessStatisticsPOFactory processStatisticsPOFactory = (ProcessStatisticsPOFactory)NamingMgr.getInstance()
                        .lookupFactory(ProcessStatisticsPO.class);

        StringBuilder sqlWhereConditionStringBuilder = new StringBuilder(200);
        ArrayList<Object> sqlArgsArrayList = new ArrayList<>(5);

        sqlWhereConditionStringBuilder.append("Type = ?");
        sqlArgsArrayList.add(type);

        if (oldestStartDate != null)
        {
            sqlWhereConditionStringBuilder.append(" AND StartDate >= ?");
            sqlArgsArrayList.add(oldestStartDate);
        }

        sqlWhereConditionStringBuilder.append(" ORDER BY StartDate DESC");

        String sqlWhereCondition = sqlWhereConditionStringBuilder.toString();
        Object[] sqlArgs = sqlArgsArrayList.toArray();

        @SuppressWarnings("unchecked")
        Collection<ProcessStatisticsPO> processStatisticsPOs = processStatisticsPOFactory
                        .getObjectsBySQLWhere(sqlWhereCondition, sqlArgs);

        return new ObjectMappingCollection<>(processStatisticsPOs, this);
    }

    @Override
    public ProcessStatisticsBO getProcessStatisticsByProcess(Process process)
    {
        ProcessStatisticsBO processStatisticsBO = null;
        
        ProcessStatisticsPOFactory processStatisticsPOFactory = (ProcessStatisticsPOFactory)NamingMgr.getInstance().lookupFactory(ProcessStatisticsPO.class);
        
        String condition = "PROCESSUUID = (SELECT UUID FROM PROCESS WHERE PARENTUUID IS NULL AND ROWNUM = 1 START WITH UUID = ? CONNECT BY PRIOR PARENTUUID = UUID)";
        Object[] args = { process.getUUID() };
        
        Collection<ProcessStatisticsPO> psc = processStatisticsPOFactory.getObjectsBySQLWhere(condition, args);
        if (! psc.isEmpty()) {
            processStatisticsBO = new ORMProcessStatisticsBOImpl(psc.iterator().next(), getContext());
            ORMHelper.closeCollection(psc);
        }
        
        return processStatisticsBO;
    }

    @Override
    public ORMProcessStatisticsBOImpl resolve(ProcessStatisticsPO processStatisticsPO)
    {
        return new ORMProcessStatisticsBOImpl(processStatisticsPO, getContext());
    }
}
