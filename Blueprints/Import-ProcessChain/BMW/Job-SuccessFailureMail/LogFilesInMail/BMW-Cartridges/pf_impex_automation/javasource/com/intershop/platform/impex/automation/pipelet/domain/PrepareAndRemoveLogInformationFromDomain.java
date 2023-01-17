package com.intershop.platform.impex.automation.pipelet.domain;

import java.util.ArrayList;
import java.util.Iterator;

import com.intershop.beehive.core.capi.locking.Process;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * Pipelet to prepare and remove log information (import log file name, job domain name, job name, job description) from the given domain object
 *
 * @author t.hofbeck@intershop.de
 * @version 1.0, 2016-01-29
 * @since 3.2.1
 *
 *          First basic functionality.
 */
public class PrepareAndRemoveLogInformationFromDomain extends Pipelet
{ 

    protected static String DN_TARGET_DOMAIN = "TargetDomain";
    
    private static String LOG_FILE_NAME = "Import_LogFileName"; 
    private static String LOG_JOB_NAME = "Import_LogJobName";
    private static String LOG_JOB_DESCRIPTION = "Import_LogJobDescription";
    private static String LOG_JOB_DOMAIN_NAME = "Import_LogJobDomainName";
    private static String DELIMITER = "|"; 
    
    private static String RESULT_LOG_INFORMATION = "LogInformation";
    

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {

        Domain targetDomain = aPipelineDictionary.getRequired(DN_TARGET_DOMAIN);
        if (null == targetDomain){
            throw new PipeletExecutionException("Mandatory input parameter 'TargetDomain' not available in pipeline dictionary.");
        }

        ArrayList<String> logFileNames = this.getAndRemoveMultipleCA(targetDomain, LOG_FILE_NAME);
        ArrayList<String> logJobNames = this.getAndRemoveMultipleCA(targetDomain, LOG_JOB_NAME);
        ArrayList<String> logJobDescriptions = this.getAndRemoveMultipleCA(targetDomain, LOG_JOB_DESCRIPTION);
        ArrayList<String> logJobDomainNames = this.getAndRemoveMultipleCA(targetDomain, LOG_JOB_DOMAIN_NAME);
        
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < logFileNames.size(); i++){
            result.add(logFileNames.get(i) + DELIMITER + logJobNames.get(i) + DELIMITER + logJobDescriptions.get(i) + DELIMITER + logJobDomainNames.get(i));
        } 
        
        aPipelineDictionary.put(RESULT_LOG_INFORMATION, result.iterator());
        
        return PIPELET_NEXT;
    }
    
    private ArrayList<String> getAndRemoveMultipleCA (Domain targetDomain, String attributeName) {

        Iterator<String> valuesIterator =  targetDomain.getMultipleAttributes(attributeName);
        ArrayList<String> valuesList = new ArrayList<String>();
    
        while (valuesIterator.hasNext()) {
            valuesList.add(valuesIterator.next());
        }
        
        targetDomain.removeAttribute(attributeName);
        
        return valuesList;

    }
    
}
