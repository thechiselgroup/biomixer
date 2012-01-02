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

import java.util.List;

import org.thechiselgroup.biomixer.client.core.util.ServiceException;
import org.thechiselgroup.biomixer.client.core.util.task.Task;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WorkspaceDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WorkspacePreviewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.WorkspacePersistenceService;
import org.thechiselgroup.biomixer.server.workbench.server.ChooselServiceServlet;
import org.thechiselgroup.biomixer.server.workbench.server.PMF;

import com.google.appengine.api.users.UserServiceFactory;

// TODO check if there is an aspect-oriented solution to this logging
public class WorkspacePersistenceServiceServlet extends ChooselServiceServlet
        implements WorkspacePersistenceService {

    private WorkspacePersistenceService service = null;

    private WorkspacePersistenceService getServiceDelegate() {
        if (service == null) {
            service = new WorkspacePersistenceServiceImplementation(PMF.get(),
                    new WorkspaceSecurityManager(
                            UserServiceFactory.getUserService()));
        }

        return service;
    }

    @Override
    public WorkspaceDTO loadWorkspace(final Long id) throws ServiceException {
        return execute(new Task<WorkspaceDTO>() {
            @Override
            public WorkspaceDTO execute() throws ServiceException {
                return getServiceDelegate().loadWorkspace(id);
            }
        });
    }

    @Override
    public List<WorkspacePreviewDTO> loadWorkspacePreviews()
            throws ServiceException {
        return execute(new Task<List<WorkspacePreviewDTO>>() {
            @Override
            public List<WorkspacePreviewDTO> execute() throws ServiceException {
                return getServiceDelegate().loadWorkspacePreviews();
            }
        });
    }

    @Override
    public Long saveWorkspace(final WorkspaceDTO workspace)
            throws ServiceException {

        return execute(new Task<Long>() {
            @Override
            public Long execute() throws ServiceException {
                return getServiceDelegate().saveWorkspace(workspace);
            }
        });
    }
}