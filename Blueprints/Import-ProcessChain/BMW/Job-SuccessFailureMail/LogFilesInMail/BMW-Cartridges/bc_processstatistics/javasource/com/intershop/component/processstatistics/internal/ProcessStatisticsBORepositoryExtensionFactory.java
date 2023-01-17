package com.intershop.component.processstatistics.internal;

import com.intershop.beehive.businessobject.capi.BusinessObject;
import com.intershop.beehive.businessobject.capi.BusinessObjectExtension;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBORepository;
import com.intershop.component.repository.capi.AbstractDomainRepositoryBOExtensionFactory;
import com.intershop.component.repository.capi.RepositoryBO;

public class ProcessStatisticsBORepositoryExtensionFactory extends AbstractDomainRepositoryBOExtensionFactory  // AbstractBusinessObjectExtensionFactory<RepositoryBO>
{
    public ProcessStatisticsBORepositoryExtensionFactory()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getExtensionID()
    {
        return ProcessStatisticsBORepository.EXTENSION_ID;
    }

    @Override
    public boolean isApplicable(BusinessObject object)
    {
        // TODO Auto-generated method stub
        boolean isApplicable = super.isApplicable(object);
        return isApplicable;
    }

    @Override
    public BusinessObjectExtension<RepositoryBO> createExtension(RepositoryBO repository)
    {
        return new ORMProcessStatisticsBORepositoryImpl(ProcessStatisticsBORepository.EXTENSION_ID, repository);
    }

    @Override
    public Class<RepositoryBO> getExtendedType()
    {
        return RepositoryBO.class;
    }
}
