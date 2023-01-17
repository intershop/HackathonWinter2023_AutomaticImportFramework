/**
 * 
 */
package com.intershop.platform.impex.automation.pipelet.domain;

import java.util.ArrayList;
import java.util.List;

import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;
import com.intershop.beehive.core.pipelet.query.ExecuteCountQuery;
import com.intershop.component.mvc.capi.catalog.Repository;
import com.intershop.platform.impex.automation.internal.replication.ThresholdFailure;

/**
 * Checks amount of available products in replication that system with a configured threshold value
 * (Domain_AV:ReplicationProductThreshold).
 */

public class CheckReplicationProductThreshold extends ExecuteCountQuery
{
    /** dictionary key for 'ReplicationDomain' */
    private static final String DN_CHANNEL = "Channel";
    
    /** dictionary key for 'Threshold' */
    private static final String DN_THRESHOLD = "Threshold";

    /** dictionary key for 'ThresholdFailures' */
    private static final String DN_FAILURES = "ThresholdFailures";

    /** dictionary key for 'SourceCount' */
    private static final String DN_SOURCE_CNT = "SourceCount";

    /** dictionary key for 'TargetCount' */
    private static final String DN_TARGET_CNT = "TargetCount";

    /** Preference name for 'ReplicationProductThreshold' */
    private static final String PREF_THRESHOLD = "ReplicationProductThreshold";

    @Override
    public void init() throws PipelineInitializationException
    {
        super.init();
    }

    @Override
    public int execute(PipelineDictionary pd) throws PipeletExecutionException
    {

        Repository channel = pd.getRequired(DN_CHANNEL);
        Double threshold = pd.getOptional(DN_THRESHOLD, 0.0);
        Number src = pd.getRequired(DN_SOURCE_CNT);
        Number target = pd.getRequired(DN_TARGET_CNT);
        long srcCnt = src.longValue();
        long targetCnt = target.longValue();

        List<ThresholdFailure> failures = pd.getOptional(DN_FAILURES);
        if (failures == null)
        {
            failures = new ArrayList<>();
        }

        Domain repositoryDomain = channel.getRepositoryDomain();

        Logger.debug(this, "Checking {}: {} = {}", repositoryDomain.getDomainName(), PREF_THRESHOLD, threshold);

        // only if a threshold is configured
        if (threshold > 0)
        {
            ThresholdFailure failure = ThresholdFailure.getInstance(channel, threshold, srcCnt, targetCnt);
            if (failure != null)
            {
                Logger.debug(this, "Threshold failure in domain {} : source #{} target #{}",
                                repositoryDomain.getDomainName(), srcCnt, targetCnt == 0 ? "<not used>" : targetCnt);
                failures.add(failure);
                // set to PD
                pd.put(DN_FAILURES, failures);
                return PIPELET_ERROR;
            }
        }

        return PIPELET_NEXT;
    }

}
