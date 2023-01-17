package com.intershop.platform.utils.pipelet.job;

import java.util.List;

import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.job.JobConfiguration;
import com.intershop.beehive.core.capi.localization.LocaleInformation;
import com.intershop.beehive.core.capi.localization.LocaleMgr;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;
import com.intershop.beehive.core.capi.process.ProcessChain;
import com.intershop.beehive.core.capi.process.ProcessChainConcurrent;
import com.intershop.beehive.core.capi.process.ProcessChainMgr;
import com.intershop.beehive.core.capi.process.ProcessChainSequence;
import com.intershop.beehive.core.capi.process.ProcessChainTask;
import com.intershop.beehive.core.capi.process.ProcessConstants;

public class CreateJobChain extends Pipelet
{

    /**
     * Constant used to access the pipeline dictionary with key 'JobConfigurations'
     *
     * These jobs will be added to a process chain.
     */
    public static final String DN_JOB_CONFIGURATIONS = "JobConfigurations";
    /**
     * Constant used to access the pipeline dictionary with key 'jobChain'
     *
     * The chain consisting of the given jobs.
     */
    public static final String DN_JOB_CHAIN = "JobChain";
    /**
     * Constant used to access pipelet configuration with key 'ExecutionMode'
     *
     * Defines if the jobs within the chain should be prepared for concurrent or sequential execution.
     */
    public static final String CN_EXECUTION_MODE = "ExecutionMode";
    /**
     * Member attribute that holds the pipelet configuration value 'ExecutionMode'
     *
     * Defines if the jobs within the chain should be prepared for concurrent or sequential execution.
     */
    private String cfg_executionMode = "";


    private ProcessChainMgr pcMgr = null;
    LocaleMgr localeMgr = null;

    /**
     * Constant used to access the pipeline dictionary with key 'ExecutionDomain'
     *
     * The domain for which the chain is created.
     */
    public static final String DN_EXECUTION_DOMAIN = "ExecutionDomain";
    /**
     * Constant used to access the pipeline dictionary with key 'Name'
     *
     * Name of the process chain.
     */
    public static final String DN_NAME = "Name";
    /**
     * Constant used to access the pipeline dictionary with key 'Description'
     *
     * Description of the process chain.
     */
    public static final String DN_DESCRIPTION = "Description";
    /**
     * Constant used to access the pipeline dictionary with key 'Locale'
     *
     * Locale of the process chain description.
     */
    public static final String DN_LOCALE = "Locale";

    /**
     * The pipelet's execute method is called whenever the pipelets gets
     * executed in the context of a pipeline and a request. The pipeline
     * dictionary valid for the currently executing thread is provided as a
     * parameter.
     *
     * @param dict
     *            The pipeline dictionary to be used.
     * @throws PipeletExecutionException
     *             Thrown in case of severe errors that make the pipelet execute
     *             impossible (e.g. missing required input data).
     */
    public int execute(PipelineDictionary dict)
    throws PipeletExecutionException {
        // lookup 'Locale' in pipeline dictionary
        LocaleInformation locale = (LocaleInformation)dict.get(DN_LOCALE);
        if(locale == null)
            locale = localeMgr.getLeadLocale();

        // lookup 'Description' in pipeline dictionary
        String description = (String)dict.get(DN_DESCRIPTION);

        // lookup 'Name' in pipeline dictionary
        String name = (String)dict.get(DN_NAME);
        if(name == null)
            name = "Job Chain";

        // lookup 'ExecutionDomain' in pipeline dictionary
        Domain executionDomain = (Domain)dict.get(DN_EXECUTION_DOMAIN);
        if (null == executionDomain)
        {
            throw new PipeletExecutionException(
                            "Mandatory input parameter 'ExecutionDomain' not available in pipeline dictionary.");
        }

        // lookup 'JobConfigurations' in pipeline dictionary
        List<JobConfiguration> jobConfigurations = (List<JobConfiguration>)dict.get(DN_JOB_CONFIGURATIONS);
        if (null == jobConfigurations)
        {
            throw new PipeletExecutionException(
                            "Mandatory input parameter 'JobConfigurations' not available in pipeline dictionary.");

        }

        ProcessChain jobChain = pcMgr.createChain(executionDomain);
        jobChain.setName(name);

        ProcessChainTask structuredTask;
        ProcessChainSequence   sequence   = null;
        ProcessChainConcurrent concurrent = null;
        
        if(cfg_executionMode.equals("Sequential"))
        {
            sequence = pcMgr.createSequence(jobChain);
            sequence.setDescription("Set of sequential job tasks.", locale);
            
            structuredTask = sequence;
        }
        else if (cfg_executionMode.equals("Concurrent"))
        {
            concurrent = pcMgr.createConcurrent(jobChain);
            concurrent.setDescription("Set of concurrent job tasks.", locale);

            structuredTask = concurrent;
        }
        else
        {
            throw new PipeletExecutionException("Unknown " + CN_EXECUTION_MODE + " \"" + cfg_executionMode + "\"");
        }

        if (description != null)
        {
            jobChain.setDescription(description, locale);
        }
        
        jobChain.setTask(structuredTask);

        // add job tasks
        for(JobConfiguration jobConfiguration : jobConfigurations)
        {
            Domain jobDomain = jobConfiguration.getDomain();
            
            ProcessChainTask task = pcMgr.createJobTask(structuredTask, jobConfiguration);
            task.setName(task.getProcessName() + " for " + jobConfiguration.getName() + " (" + (jobDomain != null ? jobDomain.getDomainName() : "null") + ")");
            task.addIgnoredStatus(ProcessConstants.STATUS_ERROR_NAME);
            task.addIgnoredStatus(ProcessConstants.STATUS_FAILURE_NAME);
            if (sequence != null)
            {
                sequence.addToTasks(task);
            }
            if (concurrent != null)
            {
                concurrent.addToTasks(task);
            }
        }

        // store 'JobChain' in pipeline dictionary
        dict.put(DN_JOB_CHAIN, jobChain);

        return PIPELET_NEXT;
    }

    /**
     * The pipelet's initialization method is called whenever the pipeline
     * used to read and process pipelet configuration values that are required
     * during the pipelet execution later on.
     *
     * @throws  PipelineInitializationException
     *          Thrown if some error occured when reading the pipelet configuration.
     */
    public void init()
    throws PipelineInitializationException {
        // store 'ExecutionMode' config value in field variable
        cfg_executionMode = (String)getConfiguration().get(CN_EXECUTION_MODE);
        if (null == cfg_executionMode)
        {
            throw new PipelineInitializationException("Mandatory attribute 'ExecutionMode' not found in pipelet configuration.");
        }

        pcMgr = NamingMgr.getManager(ProcessChainMgr.class);
        localeMgr = NamingMgr.getManager(LocaleMgr.class);
    }
}