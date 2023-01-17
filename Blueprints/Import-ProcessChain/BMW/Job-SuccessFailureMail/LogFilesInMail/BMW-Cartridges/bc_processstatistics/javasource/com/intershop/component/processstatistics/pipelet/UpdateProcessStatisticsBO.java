package com.intershop.component.processstatistics.pipelet;

import java.util.Date;

import com.intershop.beehive.core.capi.locking.Process;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.processstatistics.capi.ProcessStatisticsBO;

/**
 * The domain and type of a ProcessStatisticsBO cannot be changed anymore.
 */
public class UpdateProcessStatisticsBO extends Pipelet
{
    protected static String DN_ProcessStatisticsBO = "ProcessStatisticsBO";

    protected static String DN_DoUpdateName = "DoUpdateName";
    protected static String DN_Name = "Name";

    protected static String DN_DoUpdateStartDate = "DoUpdateStartDate";
    protected static String DN_StartDate = "StartDate";

    protected static String DN_DoUpdateProcess = "DoUpdateProcess";
    protected static String DN_Process = "Process";

    protected static String DN_DoUpdateEndDate = "DoUpdateEndDate";
    protected static String DN_EndDate = "EndDate";

    protected static String DN_DoUpdateResult = "DoUpdateResult";
    protected static String DN_Result = "Result";

    /**
     * Constant that indicates, that the result is either success, 
     * or a process is given and it is to check if the process did fail.
     * If the process did fail, the result will be set to FAILURE, otherwise to SUCCESS.
     */
    public static final String CHECK_SUCCESS = "CHECK_SUCCESS";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        ProcessStatisticsBO processStatisticsBO = aPipelineDictionary.getRequired(DN_ProcessStatisticsBO);
        
        Boolean doUpdateName = aPipelineDictionary.getRequired(DN_DoUpdateName);
        if (doUpdateName)
        {
            String name = aPipelineDictionary.getRequired(DN_Name);  // Required if DoUpdateName is true.
            processStatisticsBO.setName(name);
        }
        
        Boolean doUpdateStartDate = aPipelineDictionary.getRequired(DN_DoUpdateStartDate);
        if (doUpdateStartDate)
        {
            Date startDate = aPipelineDictionary.getRequired(DN_StartDate);  // Required if DoUpdateStartDate is true.
            processStatisticsBO.setStartDate(startDate);
        }
        
        Boolean doUpdateProcess = aPipelineDictionary.getRequired(DN_DoUpdateProcess);
        if (doUpdateProcess)
        {
            Process process = aPipelineDictionary.getOptional(DN_Process);  // Optional, can be null.
            processStatisticsBO.setProcess(process);
        }
        
        Boolean doUpdateEndDate = aPipelineDictionary.getRequired(DN_DoUpdateEndDate);
        if (doUpdateEndDate)
        {
            Date endDate = aPipelineDictionary.getOptional(DN_EndDate);  // Optional, can be null.
            processStatisticsBO.setEndDate(endDate);
        }
        
        Boolean doUpdateResult = aPipelineDictionary.getRequired(DN_DoUpdateResult);
        if (doUpdateResult)
        {
            String result = aPipelineDictionary.getOptional(DN_Result); // Optional, can be null.
            if (CHECK_SUCCESS.equals(result))
            {
                Process process = processStatisticsBO.getProcess();
                if (process != null && checkIsFailure(process))
                {
                    processStatisticsBO.setResult("FAILURE");
                }
                else
                {
                    processStatisticsBO.setResult("SUCCESS");
                }
            }
            else
            {
                processStatisticsBO.setResult(result);
            }
        }

        return PIPELET_NEXT;
    }
    
    private boolean checkIsFailure(final Process process)
    {
        boolean result = false;
        if (process != null)
        {
            result = "failed".equals(process.getState());
            if (!result)
            {
                for (Object obj : process.getChilds())
                {
                    result = checkIsFailure((Process)obj);
                    if (result)
                    {
                        break;
                    }
                }
            }
        }
        return result;
    }
}
