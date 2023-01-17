package com.intershop.component.processstatistics.pipelet;

import java.util.Map;

import javax.validation.constraints.NotNull;

import com.intershop.beehive.core.capi.environment.ORMMgr;
import com.intershop.beehive.core.capi.locking.Process;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.orm.capi.common.ORMException;
import com.intershop.beehive.orm.capi.transaction.Transaction;
import com.intershop.beehive.orm.capi.transaction.TransactionManager;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBO;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBORepository;
import com.intershop.component.processstatistics.capi.extension.ProcessStatisticsBOImportMonitoringExtension;
import com.intershop.component.processstatistics.internal.FileImportStatistics;
import com.intershop.component.processstatistics.internal.ProductImportStatistics;

public class UpdateImportProcessStatistics extends Pipelet
{
    /** declaration of keys in PD input 'ImportStatisticsMap' */
    private enum StatisticMapEntryKeys
    {
        CountProducts, CountProductsOnline,
        CountParts, CountPartsOnline,
        CountVehicleTypes, CountVehicleTypesOnline,
        CountProdCats, CountProdWithoutCats
    }

    /** declaration of keys in PD input 'FileStatisticsMap' */
    private enum FileStatisticMapEntryKeys
    {
        DomainName, SelectedFile, ImportMode,
    }

    /** key to access dictionary with 'ProcessStatisticsBO' */
    private static final String DN_PROCESS_STATISTICS_BO = "ProcessStatisticsBO";

    /** key to access dictionary with 'Process' */
    private static final String DN_PROCESS_STATISTICS_BO_REPOSITORY = "ProcessStatisticsBORepository";

    /** key to access dictionary with 'Process' */
    private static final String DN_PROCESS = "Process";

    /** key to access dictionary with 'ImportStatisticsMap' */
    private static final String DN_IMPORT_STATISTICS_MAP = "ImportStatisticsMap";

    /** key to access dictionary with 'FileStatisticsMap' */
    private static final String DN_FILE_STATISTICS_MAP = "FileStatisticsMap";

    /** key to access dictionary with 'ElementCount' */
    private static final String DN_ELEMENT_COUNT = "ElementCount";

    /** key to access configuration with 'Phase' */
    private static final String CFG_PHASE = "Phase";

    /** time in milliseconds before transaction retry */
    private static final int RETRY_TIMEOUT = 100;

    /** number of retries before final transaction fail */
    private static final int RETRY_NUMBER = 10;

    /** import phase */
    protected ProcessStatisticsBOImportMonitoringExtension.Phase phase;

    /** fetch our good old fellow - the transaction manager  */
    protected TransactionManager transactionMgr = NamingMgr.getManager(ORMMgr.class).getORMEngine().getTransactionManager();

    @Override
    public void init()
    {
        phase = ProcessStatisticsBOImportMonitoringExtension.Phase.valueOf((String) getConfiguration().get(CFG_PHASE));
    }

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        // fetch input bindings
        ProcessStatisticsBO processStatisticsBO = dict.getOptional(DN_PROCESS_STATISTICS_BO);
        
        Process process = dict.getOptional(DN_PROCESS);
        ProcessStatisticsBORepository processStatisticsBORepository = dict.getOptional(DN_PROCESS_STATISTICS_BO_REPOSITORY);
        
        if (processStatisticsBO == null && (processStatisticsBORepository == null || process == null))
        {
            throw new PipeletExecutionException("Either 'ProcessStatisticsBO' or 'ProcessStatisticsBORepository' and 'Process' must be provided.");
        }
        
        Map<String, Long> productImportStatisticsMap = dict.getRequired(DN_IMPORT_STATISTICS_MAP);
        Map<String, String> fileImportStatisticsMap = dict.getOptional(DN_FILE_STATISTICS_MAP);
        
        long elements = dict.getOptional(DN_ELEMENT_COUNT, 0);
        
        // explicit transaction handling to address optimistic locking exceptions
        boolean success = false;
        Transaction transaction = null;
        for (int retries = RETRY_NUMBER; retries > 0; retries--)
        {
            try {
                
                transaction = transactionMgr.getCurrentTransaction();
                transaction.begin();
                
                // fetch import monitoring extension for statistics business object
                ProcessStatisticsBOImportMonitoringExtension<ProcessStatisticsBO> importMonitoringExtension;
                if (processStatisticsBO == null) {
                    ProcessStatisticsBO tmpProcessStatisticsBO = processStatisticsBORepository.getProcessStatisticsByProcess(process);
                    importMonitoringExtension = tmpProcessStatisticsBO.getExtension(ProcessStatisticsBOImportMonitoringExtension.class);
                }
                else
                {
                    // currently there is no re-read of processStatisticsBO when provided as input binding (before and after chain execution)
                    importMonitoringExtension = processStatisticsBO.getExtension(ProcessStatisticsBOImportMonitoringExtension.class);
                }
                
                // build transient statistics object from map and store afterwards via monitoring extension
                buildAndStoreImportStatistics(importMonitoringExtension, productImportStatisticsMap, fileImportStatisticsMap, elements);
                
                transaction.commit();
                
                // transaction has been committed
                success = true;
                break;
            }
            catch (ORMException ex)
            { 
                Logger.debug(this, "Could not update statistics for process '" + process.getName() + "'. Will retry in " + RETRY_TIMEOUT + " millseconds...", ex);
                if (transaction != null && transaction.isActive())
                {
                    try
                    {
                        transaction.rollback();
                    }
                    catch (Exception rollbackEx)
                    {
                        Logger.error(this, "Could not rollback transaction.", rollbackEx);
                    }
                }
                try
                {
                    Thread.sleep(RETRY_TIMEOUT);
                }
                catch(InterruptedException e)
                {
                }
            }
        }
        
        if (! success)
        {
            Logger.error(this, "Could not update statistics for process '{}'.",  process.getName());
        }
        
        return PIPELET_NEXT;
    }

    protected void buildAndStoreImportStatistics(ProcessStatisticsBOImportMonitoringExtension<ProcessStatisticsBO> importMonitoringExtension,
                    Map<String, Long> productImportStatisticsMap, Map<String, String> fileImportStatisticsMap, long elements)
    {
        // build transient product import statistics from map
        ProductImportStatistics productImportStatistics = buildProductImportStatistics(productImportStatisticsMap);
        
        // build transient file import statistics from map when available
        FileImportStatistics fileImportStatistics = null;
        if (fileImportStatisticsMap != null) 
        {
            fileImportStatistics = buildFileImportStatistics(fileImportStatisticsMap);
        }
        
        switch (phase)
        {
            case BeforeProcess:
                importMonitoringExtension.storeBeforeProcess(productImportStatistics);
                break;
            
            case AfterProcess:
                importMonitoringExtension.storeAfterProcess(productImportStatistics);
                break;
            
            case BeforeFileImport:
            case AfterFileImport:
                if (fileImportStatistics != null) 
                {
                    importMonitoringExtension.createOrUpdateFileImportStatistics(fileImportStatistics, elements, productImportStatistics);
                }
                break;
        }

        // store updated file import statistics
        importMonitoringExtension.storeFileImports(importMonitoringExtension.getFileImports());
    }

    protected ProductImportStatistics buildProductImportStatistics(@NotNull final Map<String, Long> statisticsMap)
    {
        ProductImportStatistics pis = new ProductImportStatistics();
        
        pis.setProducts                 (getLongValueByKey(statisticsMap, StatisticMapEntryKeys.CountProducts.name()));
        pis.setProductsOnline           (getLongValueByKey(statisticsMap, StatisticMapEntryKeys.CountProductsOnline.name()));
        pis.setParts                    (getLongValueByKey(statisticsMap, StatisticMapEntryKeys.CountParts.name()));
        pis.setPartsOnline              (getLongValueByKey(statisticsMap, StatisticMapEntryKeys.CountPartsOnline.name()));
        pis.setTypes                    (getLongValueByKey(statisticsMap, StatisticMapEntryKeys.CountVehicleTypes.name()));
        pis.setTypesOnline              (getLongValueByKey(statisticsMap, StatisticMapEntryKeys.CountVehicleTypesOnline.name()));
        pis.setProductsWithCategories   (getLongValueByKey(statisticsMap, StatisticMapEntryKeys.CountProdCats.name()));
        pis.setProductsWithoutCategories(getLongValueByKey(statisticsMap, StatisticMapEntryKeys.CountProdWithoutCats.name()));
        
        return pis;
    }

    protected FileImportStatistics buildFileImportStatistics(@NotNull final Map<String, String> statisticsMap)
    {
        FileImportStatistics fis = new FileImportStatistics();
        String filename = statisticsMap.get(FileStatisticMapEntryKeys.SelectedFile.name());
        
        fis.setFilename                 (filename);
        fis.setObjectTypeFromFilename   (filename);
        fis.setDomain                   (statisticsMap.get(FileStatisticMapEntryKeys.DomainName.name()));
        fis.setImportMode               (FileStatisticMapEntryKeys.ImportMode.name());
        
        return fis;
    }

    protected long getLongValueByKey(final Map<String, Long> importStatistics, final String key)
    {
        long staticticsValue = 0L;
        
        if (importStatistics.containsKey(key))
        {
            Number tmp = importStatistics.get(key);
            staticticsValue = tmp.longValue();
        }
        
        return staticticsValue;
    }

}
