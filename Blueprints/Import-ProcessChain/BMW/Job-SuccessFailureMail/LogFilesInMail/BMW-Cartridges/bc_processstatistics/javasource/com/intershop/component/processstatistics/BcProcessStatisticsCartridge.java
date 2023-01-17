package com.intershop.component.processstatistics;

import com.intershop.beehive.core.capi.cartridge.Cartridge;
import com.intershop.beehive.core.capi.domain.XMLCustomAttributeMgr;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.component.processstatistics.internal.ProcessStatisticsPO;

public class BcProcessStatisticsCartridge extends Cartridge
{
    @Override
    public void initDynamicAttributeClasses()
    {
        XMLCustomAttributeMgr mgr = (XMLCustomAttributeMgr)NamingMgr.getInstance().lookupManager(XMLCustomAttributeMgr.REGISTRY_NAME);
        mgr.initXMLCustomAttributes(ProcessStatisticsPO.class);

        super.initDynamicAttributeClasses();
    }
}
