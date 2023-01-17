package com.intershop.platform.utils.pipelet.rebate;

import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.marketing.internal.rebate.RebateFilterGroupAssignmentPO;
import com.intershop.component.marketing.internal.rebate.RebateFilterGroupAssignmentPOFactory;
import com.intershop.component.marketing.internal.rebate.RebateFilterGroupAssignmentPOKey;

/**
 * Get a RebateFilterGroupAssignment by UUID. If not found the error connector is used.
 */
public class GetRebateFilterGroupAssignmentByUUID extends Pipelet
{
    /**
     * Constant used to access the pipeline dictionary with key 'RebateFilterGroupAssignmentUUID'.
     */
    protected static String DN_REBATE_FILTER_GROUP_ASSIGNMENT_UUID = "RebateFilterGroupAssignmentUUID";

    /**
     * Constant used to access the pipeline dictionary with key 'RebateFilterGroupAssignment'.
     */
    protected static String DN_REBATE_FILTER_GROUP_ASSIGNMENT = "RebateFilterGroupAssignment";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        String rebateFilterGroupAssignmentUUID = aPipelineDictionary.getRequired(DN_REBATE_FILTER_GROUP_ASSIGNMENT_UUID);
        
        RebateFilterGroupAssignmentPOFactory filterGroupAssignFactory = (RebateFilterGroupAssignmentPOFactory)NamingMgr.getInstance().lookupFactory(RebateFilterGroupAssignmentPO.class);
        RebateFilterGroupAssignmentPO rebateFilterGroupAssignment = filterGroupAssignFactory.getObjectByPrimaryKey(new RebateFilterGroupAssignmentPOKey(rebateFilterGroupAssignmentUUID));
        
        if (rebateFilterGroupAssignment != null)
        {
            aPipelineDictionary.put(DN_REBATE_FILTER_GROUP_ASSIGNMENT, rebateFilterGroupAssignment);
            return PIPELET_NEXT;
        }
        else
        {
            return PIPELET_ERROR;
        }
    }
}
