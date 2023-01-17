package com.intershop.platform.utils.capi.properties;

import java.util.Properties;

import com.google.common.base.Predicate;
import com.intershop.beehive.core.capi.domain.DomainMgr;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.naming.NamingMgr;

/**
 * A {@link Predicate} based filter which removes chunks of properties - so called configuration records - referring to 
 * domain names of non-existing domains. The checked domain name is taken from any key containing the string
 * {@code DomainName}.
 * <p>
 * Optionally, a flag can be provided during filter instantiation which controls whether a warn log entry is written
 * whenever a configuration record is filtered out or in other words whenever method {@link #apply(Properties)} returns
 * {@code false}. The default behavior omits all logging.
 * 
 * @author JCMeyer
 *
 */
public class ExistingDomainPropertiesFilter implements Predicate<Properties>
{
    protected static final String KEY_PART_DOMAIN_NAME = "DomainName";
    
    protected static final DomainMgr DOMAIN_MGR = NamingMgr.getManager(DomainMgr.class);
    
    protected boolean logWarnings;
    
    public ExistingDomainPropertiesFilter()
    {
        this(false);
    }
    
    public ExistingDomainPropertiesFilter(boolean logWarnings)
    {
        this.logWarnings = logWarnings;
    }
    
    @Override
    public boolean apply(Properties configurationRecord)
    {
        for (String key : configurationRecord.stringPropertyNames())
        {
            if (key.contains(KEY_PART_DOMAIN_NAME))
            {
                String domainName = configurationRecord.getProperty(key);
                if (DOMAIN_MGR.getDomainByName(domainName) == null)
                {
                    if (logWarnings)
                    {
                        Logger.warn(this, "Filtering out properties targeting non-existing or to-be-deleted domain '{}'.", domainName);
                    }
                    return false;
                }
                
            }
        }
        return true;
    }
}
