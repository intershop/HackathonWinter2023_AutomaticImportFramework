package com.intershop.platform.impex.automation.pipelet.domain;

import java.util.ArrayList;
import java.util.Iterator;

import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.environment.ORMMgr;
import com.intershop.beehive.core.capi.locking.Process;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.orm.capi.common.ORMException;
import com.intershop.beehive.orm.capi.transaction.Transaction;

/**
 * Pipelet appends log information (import log file name, job domain name, job name, job description) to the given domain object
 *
 * @author t.hofbeck@intershop.de
 * @version 1.0, 2016-01-29
 * @since 3.2.1
 *
 *          First basic functionality.
 */
public class AddLogInformationToDomain extends Pipelet
{

    protected static String DN_FILE_NAME = "FileName";

    protected static String DN_PROCESS = "Process";

    protected static String DN_TARGET_DOMAIN = "TargetDomain";
    
    private static String LOG_FILE_NAME = "Import_LogFileName"; 
    private static String LOG_JOB_NAME = "Import_LogJobName";
    private static String LOG_JOB_DESCRIPTION = "Import_LogJobDescription";
    private static String LOG_JOB_DOMAIN_NAME = "Import_LogJobDomainName";
    

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        String fileName = aPipelineDictionary.getRequired(DN_FILE_NAME);
        if (null == fileName){
            throw new PipeletExecutionException("Mandatory input parameter 'FileName' not available in pipeline dictionary.");
        }
            
        Process process = aPipelineDictionary.getRequired(DN_PROCESS);
        if (null == process){
            throw new PipeletExecutionException("Mandatory input parameter 'Process' not available in pipeline dictionary.");
        }

        Domain targetDomain = aPipelineDictionary.getRequired(DN_TARGET_DOMAIN);
        if (null == targetDomain){
            throw new PipeletExecutionException("Mandatory input parameter 'TargetDomain' not available in pipeline dictionary.");
        }

        //
        // The optimistic locking approach the ORM layer uses may lead to ORMExceptions here.
        // But using pessimistic locking is not an option due to performance reasons.
        // We implement the following solution:
        //   Use optimistic locking.
        //   In case it fails, try again.
        //   Do this 3 times at most. If not successful after 3 retries then fail.
        //
        Transaction transaction = null;
        boolean isOurOwnTransaction = false;
        boolean isCompletedSuccessfully = false;
        int cntTries = 0;
        final int MAX_RETRIES = 3;  // number of times a failed transaction should be re-tried
        final int INITIAL_WAIT_TIME_UNTIL_RETRY_IN_MILLISECONS = 200;
        final int WAIT_TIME_PROLONGATION_FACTOR_PER_RETRY = 4;  // wait time is extended by this factor after each retry, wait time = INITIAL_WAIT_TIME_UNTIL_RETRY_IN_MILLISECONS * (WAIT_TIME_PROLONGATION_FACTOR_PER_RETRY to the power of cntTries)
        int waitTimeUntilNextRetryInMilliseconds = INITIAL_WAIT_TIME_UNTIL_RETRY_IN_MILLISECONS;

        do
        {
            cntTries++;

            try
            {
                transaction = NamingMgr.getManager(ORMMgr.class).getORMEngine().getTransactionManager()
                                .getCurrentTransaction();

                isOurOwnTransaction = !transaction.isActive();

                if (isOurOwnTransaction)
                {
                    transaction.begin();
                }

                //
                // This is the actual task.
                //
                updateMultipleCA(targetDomain, LOG_FILE_NAME, fileName);
                updateMultipleCA(targetDomain, LOG_JOB_NAME, process.getName());
                updateMultipleCA(targetDomain, LOG_JOB_DESCRIPTION, process.getDescription());
                updateMultipleCA(targetDomain, LOG_JOB_DOMAIN_NAME, process.getDomain().getDomainName());
                // log warning to be able to reproduce and analyze errors (e.g. ORMException)
                Logger.warn(this, "Pipelet AddLogInformationToDomain: INFO: Just updated Doamin AV name={}, stringValue={}, and 3 more, isOurOwnTransaction: {}, STORE/COMMIT still outstanding.", LOG_FILE_NAME, fileName, isOurOwnTransaction);

                if (isOurOwnTransaction)
                {
                    // our own transaction needs to be committed
                    transaction.commit();
                    transaction = null;
                }
                else
                {
                    // A transaction opened at a higher level cannot be committed here.
                    // What we could do is: Write the changes into the database
                    // to get a ORMException in case of a race condition.
                    // Unfortunately we cannot:
                    //     (1) retry our changes plus
                    //     (2) keep the changes done at a higher level
                    //transaction.store();
                }

                isCompletedSuccessfully = true;
            }
            catch(ORMException exORM)
            {
                if (isOurOwnTransaction)
                {
                    if (transaction != null && transaction.isActive())
                    {
                        try
                        {
                            // a transaction opened at a higher level cannot be committed here,
                            // but we need to write the changes into the database to get a ORMException in case of a race condition
                            transaction.rollback();
                        }
                        catch(Exception rollbackEx)
                        {
                            Logger.error(this, "Could not rollback transaction.", rollbackEx);
                        }

                        transaction = null;
                    }

                    if (cntTries <= MAX_RETRIES)
                    {
                        // log warning and try again after INITIAL_WAIT_TIME_UNTIL_RETRY_IN_MILLISECONS * (WAIT_TIME_PROLONGATION_FACTOR_PER_RETRY to the power of cntTries)
                        Logger.warn(this, "{} {}: {} after {} tries, will try again", LOG_FILE_NAME, fileName, exORM.toString(), cntTries);
                        try
                        {
                            Thread.sleep(waitTimeUntilNextRetryInMilliseconds);
                        }
                        catch(InterruptedException e)
                        {
                        }

                        waitTimeUntilNextRetryInMilliseconds *= WAIT_TIME_PROLONGATION_FACTOR_PER_RETRY;
                    }
                    else
                    {
                        Logger.error(this, "{} {}: {} after {} tries, stopping here", LOG_FILE_NAME, fileName, exORM.toString(), cntTries);
                        throw exORM;
                    }
                }
                else
                {
                    // For a transaction opened at a higher level we cannot:
                    //     (1) retry our changes plus
                    //     (2) keep the changes done at a higher level
                    //transaction.rollback();
                    //transaction.begin();
                    // That's why we cannot retry in this case.
                    // We simply throw the ORMException here.
                    Logger.error(this, "{} {}: Not our own transaction, cannot retry", LOG_FILE_NAME, fileName);
                    throw exORM;
                }
            }
            finally
            {
                if (isOurOwnTransaction)
                {
                    if (transaction != null && transaction.isActive())
                    {
                        try
                        {
                            transaction.rollback();
                        }
                        catch(Exception rollbackEx)
                        {
                            Logger.error(this, "Could not rollback transaction.", rollbackEx);
                        }

                        transaction = null;
                    }

                    NamingMgr.getManager(ORMMgr.class).closeCurrentResources();
                }
            }
        }
        while (!isCompletedSuccessfully && cntTries <= MAX_RETRIES);
        
        return PIPELET_NEXT;
    }
    
    private void updateMultipleCA (Domain targetDomain, String attributeName, String newValue) {

        Iterator<String> valuesIterator =  targetDomain.getMultipleAttributes(attributeName);
        ArrayList<String> valuesList = new ArrayList<String>();
    
        while (valuesIterator.hasNext()) {
            valuesList.add(valuesIterator.next());
        }
        
        valuesList.add(newValue);

        targetDomain.putMultipleStrings(attributeName, valuesList.iterator());

    }
    
}
