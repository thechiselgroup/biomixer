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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationException;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthorizationException;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WorkspaceDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WorkspacePreviewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.WorkspacePersistenceService;

import com.google.inject.Inject;

/**
 * Design rationale: everything that will not be used for querying is just
 * serialized and not mapped to persistable objects (reason: better performance
 * & better development performance)
 * 
 * For more information on App Engine persistence, see
 * 
 * {@linkplain http://code.google.com/appengine/docs/java/datastore/}
 * 
 * {@linkplain http
 * ://code.google.com/events/io/2009/sessions/SofterSideofSchemas.html}
 */
public class WorkspacePersistenceServiceImplementation implements
        WorkspacePersistenceService {

    private WorkspaceSecurityManager permissionManager;

    private final PersistenceManagerFactory persistenceManagerFactory;

    @Inject
    public WorkspacePersistenceServiceImplementation(
            PersistenceManagerFactory pmf,
            WorkspaceSecurityManager securityManager) {

        assert securityManager != null;
        assert pmf != null;

        permissionManager = securityManager;
        this.persistenceManagerFactory = pmf;
    }

    private PersistenceManager createPersistanceManager() {
        return persistenceManagerFactory.getPersistenceManager();
    }

    private PersistentWorkspace createPersistentWorkspace(PersistenceManager pm) {
        PersistentWorkspace workspace = new PersistentWorkspace();
        workspace = pm.makePersistent(workspace);

        permissionManager
                .createWorkspacePermissionForCurrentUser(workspace, pm);

        return workspace;
    }

    private PersistentWorkspace getPersistentWorkspace(Long workspaceId,
            PersistenceManager manager) throws AuthorizationException {

        PersistentWorkspace pWorkspace = manager.getObjectById(
                PersistentWorkspace.class, workspaceId);

        permissionManager.checkAuthorization(pWorkspace, manager);

        return pWorkspace;
    }

    private PersistentWorkspace getPersistentWorkspace(WorkspaceDTO dto,
            PersistenceManager manager) throws AuthorizationException {
        return getPersistentWorkspace(dto.getId(), manager);
    }

    private Collection<PersistentWorkspace> getPersistentWorkspacesForUser(
            PersistenceManager manager) {

        Collection<PersistentWorkspacePermission> result = permissionManager
                .getWorkspacePermissionsForCurrentUser(manager);
        Collection<PersistentWorkspace> workspaces = new ArrayList<PersistentWorkspace>();
        for (PersistentWorkspacePermission permission : result) {
            workspaces.add(permission.getWorkspace());
        }

        return workspaces;
    }

    @Override
    public WorkspaceDTO loadWorkspace(Long id) throws AuthenticationException,
            AuthorizationException {

        permissionManager.checkAuthenticated();

        PersistenceManager manager = createPersistanceManager();
        try {
            return loadWorkspace(id, manager);
        } finally {
            manager.close();
        }
    }

    private WorkspaceDTO loadWorkspace(Long id, PersistenceManager manager)
            throws AuthorizationException {

        return toWorkspaceDTO(getPersistentWorkspace(id, manager));
    }

    @Override
    public List<WorkspacePreviewDTO> loadWorkspacePreviews()
            throws AuthenticationException {

        permissionManager.checkAuthenticated();

        PersistenceManager manager = createPersistanceManager();
        try {
            return loadWorkspacePreviews(manager);
        } finally {
            manager.close();
        }
    }

    private List<WorkspacePreviewDTO> loadWorkspacePreviews(
            PersistenceManager manager) {

        Collection<PersistentWorkspace> workspaces = getPersistentWorkspacesForUser(manager);
        List<WorkspacePreviewDTO> result = new ArrayList<WorkspacePreviewDTO>();
        for (PersistentWorkspace workspace : workspaces) {
            result.add(toWorkspaceSDTO(workspace));
        }
        return result;
    }

    // TODO should be done in a transaction?
    @Override
    public Long saveWorkspace(WorkspaceDTO dto) throws AuthenticationException,
            AuthorizationException {

        permissionManager.checkAuthenticated();

        PersistenceManager pm = createPersistanceManager();

        try {
            PersistentWorkspace workspace = workspaceExists(dto) ? getPersistentWorkspace(
                    dto, pm) : createPersistentWorkspace(pm);

            updateWorkspaceWithDTO(workspace, dto);

            return workspace.getId();
        } finally {
            pm.close();
        }
    }

    private WorkspaceDTO toWorkspaceDTO(PersistentWorkspace pWorkspace) {
        WorkspaceDTO dto = new WorkspaceDTO();

        dto.setId(pWorkspace.getId());
        dto.setName(pWorkspace.getName());
        dto.setResources(pWorkspace.getResources());
        dto.setResourceSets(pWorkspace.getResourceSets());
        dto.setWindows(pWorkspace.getWindows());

        return dto;
    }

    private WorkspacePreviewDTO toWorkspaceSDTO(PersistentWorkspace workspace) {
        return new WorkspacePreviewDTO(workspace.getId(), workspace.getName());
    }

    private void updateWorkspaceWithDTO(PersistentWorkspace workspace,
            WorkspaceDTO dto) {

        workspace.setName(dto.getName());
        workspace.setResources(dto.getResources());
        workspace.setResourceSets(dto.getResourceSets());
        workspace.setWindows(dto.getWindows());
    }

    private boolean workspaceExists(WorkspaceDTO dto) {
        return dto.getId() != null;
    }
}
