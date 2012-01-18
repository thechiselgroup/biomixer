/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.server.workbench.workspace;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationException;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthorizationException;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;

public class WorkspaceSecurityManager {

    private UserService userService;

    public WorkspaceSecurityManager(UserService userService) {
        assert userService != null;
        this.userService = userService;
    }

    public void checkAuthenticated() throws AuthenticationException {
        if (!userService.isUserLoggedIn()) {
            throw new AuthenticationException(
                    "Authentication failed: User not signed in.");
        }
    }

    public void checkAuthorization(PersistentWorkspace workspace,
            PersistenceManager manager) throws AuthorizationException {

        Collection<PersistentWorkspacePermission> permissions = getWorkspacePermissionsForCurrentUser(
                workspace, manager);

        if (permissions.isEmpty()) {
            throw new AuthorizationException(
                    "User not authorized to access workspace");
        }
    }

    private void createWorkspacePermission(PersistentWorkspace workspace,
            String userId, String userName, String userEmail,
            PersistenceManager manager) {

        assert workspace != null;
        assert manager != null;
        assert userId != null;

        PersistentWorkspacePermission permission = new PersistentWorkspacePermission();
        permission.setWorkspace(workspace);
        permission.setUserId(userId);
        permission.setUserEmail(userEmail);
        permission.setUserName(userName);
        manager.makePersistent(permission);
    }

    public void createWorkspacePermissionForCurrentUser(
            PersistentWorkspace workspace, PersistenceManager manager) {

        createWorkspacePermission(workspace, getCurrentUser().getUserId(),
                getCurrentUser().getNickname(), getCurrentUser().getEmail(),
                manager);
    }

    private User getCurrentUser() {
        return userService.getCurrentUser();
    }

    public Collection<PersistentWorkspacePermission> getWorkspacePermissionsForCurrentUser(
            PersistenceManager manager) {

        Query userIdQuery = manager.newQuery(
                PersistentWorkspacePermission.class, "userId == userIdParam");
        userIdQuery.declareParameters("String userIdParam");

        return (Collection<PersistentWorkspacePermission>) userIdQuery
                .execute(getCurrentUser().getUserId());
    }

    private Collection<PersistentWorkspacePermission> getWorkspacePermissionsForCurrentUser(
            PersistentWorkspace workspace, PersistenceManager manager) {

        Query userIdQuery = manager.newQuery(
                PersistentWorkspacePermission.class,
                "workspace == workspaceParam" + " && "
                        + "userId == userIdParam");
        userIdQuery.declareParameters("PersistentWorkspace workspaceParam"
                + ", " + "String userIdParam");

        return (Collection<PersistentWorkspacePermission>) userIdQuery.execute(
                workspace, getCurrentUser().getUserId());
    }

}
