package com.intershop.platform.utils.pipelet.security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.security.AuthorizationObject;
import com.intershop.beehive.core.capi.security.PermissionIdentifier;
import com.intershop.beehive.core.capi.security.Role;
import com.intershop.beehive.core.capi.security.RoleAssignment;
import com.intershop.beehive.core.capi.security.RoleMgr;
import com.intershop.beehive.core.capi.user.User;
import com.intershop.beehive.core.capi.user.UserGroup;
import com.intershop.beehive.core.capi.user.UserGroupMgr;
import com.intershop.beehive.core.pipelet.PipelineConstants;
import com.intershop.beehive.foundation.util.FilterCondition;
import com.intershop.beehive.foundation.util.FilterIterator;
import com.intershop.beehive.foundation.util.Iterators;
import com.intershop.beehive.foundation.util.ResettableIterator;

public  class GetRoleAssignments
    extends Pipelet
{
    @Inject
    private RoleMgr roleMgr;
    
    @Inject
    private UserGroupMgr userGroupMgr;
    

    /*------------------------------------------------------------------------
                               I/O constant section
    ------------------------------------------------------------------------*/

    public static final String IO_ROLE_ASSIGNMENTS = "RoleAssignments";
    public static final String IO_ROLE_ASSIGNMENT_MAP = "RoleAssignmentMap";
    public static final String IO_AUTHORIZATIONOBJECT = "AuthorizationObject";

    /*------------------------------------------------------------------------
                            pipelet attribute section
    ------------------------------------------------------------------------*/

    /*------------------------------------------------------------------------
                            pipelet init method implementation
    ------------------------------------------------------------------------*/

    /*------------------------------------------------------------------------
                            pipelet execute method implementation
    ------------------------------------------------------------------------*/

    /**
     * The pipelet's execute method is called whenever the pipelets gets
     * executed in the context of a pipeline and a request. The pipeline
     * dictionary valid for the currently executing thread is provided as
     * a parameter.
     *
     * @param   dict The pipeline dictionary to be used.
     * @throws  PipeletExecutionException
     *          Thrown in case of severe errors that make the pipelet execute
     *          impossible (e.g. missing required input data).
     */
    public int execute(PipelineDictionary dict)
        throws PipeletExecutionException, SystemException
    {
        // lookup CurrentUser in pipeline dictionary
        User currentUser = (User)dict.getOptional( PipelineConstants.DN_CURRENT_USER );

        // lookup AuthorizationObject in pipeline dictionary
        AuthorizationObject authorizationObject = (AuthorizationObject)dict.getRequired(IO_AUTHORIZATIONOBJECT);

        Iterator roleAssignments = null;
        Map<String /* Role ID */, RoleAssignment> roleAssignmentMap = new HashMap<String, RoleAssignment>(50);
                        
        //fallback to permissionID
        if (currentUser == null)
        {
            roleAssignments = authorizationObject.createRoleAssignmentsIterator();
        }
        else
        {
            roleAssignments = new FilterIterator(authorizationObject.createRoleAssignmentsIterator(), new UserPermissionCondition(null, currentUser));
        }
        
        if (!(roleAssignments instanceof ResettableIterator))
        {
            roleAssignments = Iterators.createResettableIterator(roleAssignments);
        }
        
        ((ResettableIterator)roleAssignments).reset();
        while (roleAssignments.hasNext())
        {
            RoleAssignment roleAssignment = (RoleAssignment)roleAssignments.next();
            roleAssignmentMap.put(roleAssignment.getRoleID(), roleAssignment);
            
        }
        ((ResettableIterator)roleAssignments).reset();

        dict.put(IO_ROLE_ASSIGNMENTS, roleAssignments);
        dict.put(IO_ROLE_ASSIGNMENT_MAP, roleAssignmentMap);

        return PIPELET_NEXT;
    }

    class UserPermissionCondition extends FilterCondition
    {
        /**
         * The permission.
         */

        private PermissionIdentifier filterPermission;

        /**
         * The user.
         */

        private User filterUser;

        /**
         * The constructor. Initializes the filter.
         *
         * @param filterPermission  The permission needed to be set at the parent object
         * @param filterUser  The user who has to have the given permission
         *
         * @throws com.intershop.beehive.core.capi.common.SystemException If the manager lookup failed due to a remote error
         */

        public UserPermissionCondition(PermissionIdentifier filterPermission, User filterUser) throws SystemException
        {
            // assign values
            this.filterPermission = filterPermission;
            this.filterUser = filterUser;
        }


        /**
         * Checks if an object is a valid return value for the enumeration.
         *
         * @param       anObject        the object to be checked
         *
         * @return      true, if the object fulfills the condition for the
                        enumeration
         * REWORK Bug-2 Throws an exception, if the user group for the passed role
         *          assignment doesn't exist anymore. 
         * REWORK Perform-2 Use of isUserInUserGroup is expensive - alternative available?
         */

        @Override
        public boolean isValid(Object anObject)
        {
            if (anObject instanceof RoleAssignment)
            {
                try
                {
                    // get the role
                    RoleAssignment ra = (RoleAssignment)anObject;
                    Role role = ra.getRole();

                    // check if permission is null (ignore permission) or in role; this is a PK access
                    if ((filterPermission == null) || (roleMgr.isPermissionInRole(filterPermission, role)))
                    {
                        // the object defines the permission, check user group (null ignores user check)
                        if (filterUser != null)
                        {
                            // get params
                            String userGroupID = ra.getUserGroupID();
                            String userGroupDomainID = ra.getUserGroupDomainID();

                            // lookup group (PK access)
                            UserGroup userGroup = userGroupMgr.getUserGroup(userGroupID, userGroupDomainID);

                            // check group membership (this results in iterating a cached iterator, bad solution)
                            // check usergroup null: see SCR1619
                            if (userGroup == null || !userGroupMgr.isUserInUserGroup(filterUser, userGroup))
                            {
                                // no, the user is not in the group too, no success
                                return false;
                            }
                        }

                        // the user is null or the user was found in the group, success
                        return true;
                    }
                }
                catch (SystemException ex)
                {
                    // do only logging and return false
                    Logger.error(this, "core.Exception", ex);
                }
            }

            // no success
            return false;
        }
    }
}
