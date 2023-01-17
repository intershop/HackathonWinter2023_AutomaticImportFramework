package com.intershop.platform.utils.tax.pipelet;

import java.math.BigDecimal;

import com.intershop.beehive.bts.capi.tax.TaxClass;
import com.intershop.beehive.bts.capi.tax.TaxCodeRef;
import com.intershop.beehive.bts.capi.tax.TaxCodeTaxRateMapping;
import com.intershop.beehive.bts.capi.tax.TaxMgr;
import com.intershop.beehive.bts.capi.tax.TaxRateProvider;
import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;
import com.intershop.beehive.core.capi.user.User;

public class GetTaxJurisdictionAndTaxRate extends Pipelet
{
    public static final String DN_USER = "User";
    public static final String DN_DOMAIN_NAME = "Domain";
    public static final String DN_TAXCLASS = "TaxClass";
    public static final String DN_TAXRATE = "TaxRate";
    public static final String DN_TAXJURISDICTIONCODE = "TaxJurisdictionCode";
    public static final String DN_ERROR = "TaxError";
    public static final String DN_ERROR_VALUE_TAX_RATE_NOT_FOUND = "TaxRateNotFoundForTaxClass";
    public static final String DN_ERROR_VALUE_NO_TAXJURISDICTION = "NoTaxJurisdictionForDomain";
    private TaxMgr taxMgr = null;

    @Override
    public void init() throws PipelineInitializationException, SystemException
    {
        taxMgr = ((TaxMgr)NamingMgr.getInstance().lookupManager("TaxMgr"));

        if (taxMgr == null)
        {
            throw new PipelineInitializationException("TaxMgr not found.");
        }
    }

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        Domain domain = aPipelineDictionary.getRequired(DN_DOMAIN_NAME);
        String domainName = domain.getDomainName();
        //Domain aRespDomain = taxMgr.getTaxClassProvidingDomain(domain);

        User user = aPipelineDictionary.getRequired(DN_USER);
        
        TaxClass taxClass = aPipelineDictionary.getOptional(DN_TAXCLASS);

        TaxRateProvider taxRateProvider = taxMgr.getTaxRateProvider(domain);
        if (taxRateProvider == null)
        {
          throw new PipeletExecutionException("TaxRateProvider not found for domain: " + domainName);
        }
        
        String defaultTaxJurisdictionCodeForDomain = taxRateProvider.getDefaultTaxJurisdictionCode(domain);
        if (defaultTaxJurisdictionCodeForDomain != null)
        {
            aPipelineDictionary.put(DN_TAXJURISDICTIONCODE, defaultTaxJurisdictionCodeForDomain);
            
            if (taxClass != null)
            {
                String taxCode = taxClass.getTaxCode();
                TaxCodeRef taxCodeRef = new TaxCodeRef(taxCode, domainName);
                TaxCodeTaxRateMapping mapping = new TaxCodeTaxRateMapping();
                mapping.createEntry(taxCodeRef);

                mapping = taxRateProvider.lookupTaxRates(user, domain, mapping, defaultTaxJurisdictionCodeForDomain);

                BigDecimal taxRate = mapping.getTaxRate(taxCodeRef);
                if (taxRate != null)
                {
                    aPipelineDictionary.put(DN_TAXRATE, taxRate);
                }
                else
                {
                    aPipelineDictionary.put(DN_ERROR, DN_ERROR_VALUE_TAX_RATE_NOT_FOUND);
                    return PIPELET_ERROR;
                }
            }
        }
        else
        {
            aPipelineDictionary.put(DN_ERROR, DN_ERROR_VALUE_NO_TAXJURISDICTION);
            return PIPELET_ERROR;
        }

        return PIPELET_NEXT;
    }
}
