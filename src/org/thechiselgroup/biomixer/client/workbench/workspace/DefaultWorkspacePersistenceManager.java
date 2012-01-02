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
package org.thechiselgroup.biomixer.client.workbench.workspace;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.workbench.services.AsyncCallbackVoidDelegate;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ResourceSetDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WindowDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WorkspaceDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WorkspacePreviewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.WorkspacePersistenceServiceAsync;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.WorkspaceSharingServiceAsync;
import org.thechiselgroup.choosel.core.client.persistence.Persistable;
import org.thechiselgroup.choosel.core.client.persistence.PersistableRestorationService;
import org.thechiselgroup.choosel.core.client.resources.DefaultResourceSet;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceManager;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.ResourceSetFactory;
import org.thechiselgroup.choosel.core.client.resources.UnmodifiableResourceSet;
import org.thechiselgroup.choosel.core.client.resources.persistence.DefaultResourceSetCollector;
import org.thechiselgroup.choosel.core.client.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.choosel.dnd.client.windows.Desktop;
import org.thechiselgroup.choosel.dnd.client.windows.WindowContent;
import org.thechiselgroup.choosel.dnd.client.windows.WindowContentProducer;
import org.thechiselgroup.choosel.dnd.client.windows.WindowPanel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DefaultWorkspacePersistenceManager implements
        WorkspacePersistenceManager {

    private Desktop desktop;

    private ResourceManager resourceManager;

    private ResourceSetFactory resourceSetFactory;

    private WorkspacePersistenceServiceAsync service;

    private WorkspaceSharingServiceAsync sharingService;

    private WindowContentProducer viewFactory;

    private WorkspaceManager workspaceManager;

    private PersistableRestorationService restorationService;

    @Inject
    public DefaultWorkspacePersistenceManager(
            WorkspaceManager workspaceManager, Desktop desktop,
            WorkspacePersistenceServiceAsync service,
            WindowContentProducer viewFactory, ResourceManager resourceManager,
            ResourceSetFactory resourceSetFactory,
            WorkspaceSharingServiceAsync sharingService,
            PersistableRestorationService restorationService) {

        assert resourceManager != null;
        assert workspaceManager != null;
        assert desktop != null;
        assert service != null;
        assert viewFactory != null;
        assert resourceSetFactory != null;
        assert sharingService != null;
        assert restorationService != null;

        this.sharingService = sharingService;
        this.resourceSetFactory = resourceSetFactory;
        this.resourceManager = resourceManager;
        this.viewFactory = viewFactory;
        this.desktop = desktop;
        this.workspaceManager = workspaceManager;
        this.service = service;
        this.restorationService = restorationService;
    }

    private WindowDTO createWindowDTO(
            DefaultResourceSetCollector persistanceManager, WindowPanel window,
            WindowContent windowContent) {

        assert windowContent instanceof Persistable;

        // TODO change to WindowModel, keep in workspace, update
        // automatically
        WindowDTO windowDTO = new WindowDTO();
        windowDTO.setX(window.getAbsoluteX());
        windowDTO.setY(window.getAbsoluteY());

        windowDTO.setHeight(window.getOffsetHeight());
        windowDTO.setWidth(window.getOffsetWidth());

        // TODO store x-zindex

        // TODO use view content label instead of window title
        windowDTO.setTitle(window.getWindowTitle());

        windowDTO.setContentType(windowContent.getContentType());

        Persistable persistable = (Persistable) windowContent;
        windowDTO.setViewState(persistable.save(persistanceManager));

        return windowDTO;
    }

    private List<WindowDTO> createWindowDTOs(
            DefaultResourceSetCollector persistanceManager) {

        List<WindowPanel> windows = desktop.getWindows();
        List<WindowDTO> windowDTOs = new ArrayList<WindowDTO>();
        for (int i = 0; i < windows.size(); i++) {
            WindowPanel window = windows.get(i);
            WindowContent windowContent = window.getViewContent();

            if (!(windowContent instanceof Persistable)) {
                continue;
            }

            windowDTOs.add(createWindowDTO(persistanceManager, window,
                    windowContent));
        }
        return windowDTOs;
    }

    private WorkspaceDTO createWorkspaceDTO(Workspace workspace) {
        WorkspaceDTO workspaceDTO = new WorkspaceDTO();

        workspaceDTO.setId(workspace.getId());
        workspaceDTO.setName(workspace.getName());

        // collects resource sets accross all windows
        DefaultResourceSetCollector resourceSetCollector = new DefaultResourceSetCollector();
        List<WindowDTO> windowDTOs = createWindowDTOs(resourceSetCollector);
        workspaceDTO.setWindows(windowDTOs.toArray(new WindowDTO[windowDTOs
                .size()]));

        // Resource set DTOs
        // 1. resolved unmodified sets --> changes list size
        List<ResourceSet> resourceSets = new ArrayList<ResourceSet>(
                resourceSetCollector.getResourceSets());
        for (ResourceSet resourceSet : resourceSets) {
            if (resourceSet instanceof UnmodifiableResourceSet) {
                resourceSetCollector
                        .storeResourceSet(((UnmodifiableResourceSet) resourceSet)
                                .getDelegate());
            }
        }

        // 2. store sets
        ResourceSet resourceCollector = new DefaultResourceSet();
        ResourceSetDTO[] resourceSetDTOs = new ResourceSetDTO[resourceSetCollector
                .getResourceSets().size()];
        for (int i = 0; i < resourceSetCollector.getResourceSets().size(); i++) {
            ResourceSetDTO dto = new ResourceSetDTO();
            ResourceSet resourceSet = resourceSetCollector.getResourceSets()
                    .get(i);

            if (resourceSet.hasLabel()) {
                dto.setLabel(resourceSet.getLabel());
            }

            dto.setId(i);

            if (resourceSet instanceof UnmodifiableResourceSet) {
                ResourceSet sourceSet = ((UnmodifiableResourceSet) resourceSet)
                        .getDelegate();

                dto.setDelegateSetId(resourceSetCollector
                        .storeResourceSet(sourceSet));
            } else {
                List<String> resourceIds = new ArrayList<String>();
                for (Resource resource : resourceSet) {
                    resourceCollector.add(resource);
                    resourceIds.add(resource.getUri());
                }
                dto.setResourceIds(resourceIds);
            }

            resourceSetDTOs[i] = dto;
        }
        workspaceDTO.setResourceSets(resourceSetDTOs);

        List<Resource> allResources = resourceManager.getAllResources();
        Resource[] resources = allResources.toArray(new Resource[allResources
                .size()]);

        workspaceDTO.setResources(resources);

        return workspaceDTO;
    }

    @Override
    public void loadWorkspace(Long workspaceID,
            final AsyncCallback<Workspace> callback) {

        assert workspaceID != null;
        assert callback != null;

        service.loadWorkspace(workspaceID, new AsyncCallback<WorkspaceDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(WorkspaceDTO dto) {
                try {
                    Workspace workspace = loadWorkspace(dto);
                    callback.onSuccess(workspace);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        });

    }

    // protected for tests
    protected Workspace loadWorkspace(WorkspaceDTO dto) {
        Workspace workspace = new Workspace();
        workspace.setName(dto.getName());
        workspace.setId(dto.getId());
        workspace.setSavingState(WorkspaceSavingState.NOT_SAVED); // use saved
        // once we have everything in commands
        workspaceManager.setWorkspace(workspace);

        restoreResources(dto);

        ResourceSetDTO[] resourceSetDTOs = dto.getResourceSets();
        final ResourceSet[] resourceSets = new ResourceSet[resourceSetDTOs.length];
        // 1. restore primary resource sets
        for (ResourceSetDTO resourceSetDTO : resourceSetDTOs) {
            if (!resourceSetDTO.isUnmodifiable()) {
                ResourceSet resourceSet = resourceSetFactory
                        .createResourceSet();
                resourceSet.setLabel(resourceSetDTO.getLabel());
                for (String uri : resourceSetDTO.getResourceIds()) {
                    resourceSet.add(resourceManager.getByUri(uri));
                }
                resourceSets[resourceSetDTO.getId()] = resourceSet;
            }
        }
        // 2. restore unmodifiable resource sets
        for (ResourceSetDTO resourceSetDTO : resourceSetDTOs) {
            if (resourceSetDTO.isUnmodifiable()) {
                int delegateId = resourceSetDTO.getDelegateSetId();
                ResourceSet resourceSet = new UnmodifiableResourceSet(
                        resourceSets[delegateId]);
                resourceSets[resourceSetDTO.getId()] = resourceSet;
            }
        }

        ResourceSetAccessor accessor = new ResourceSetAccessor() {
            @Override
            public ResourceSet getResourceSet(int id) {
                assert id >= 0;
                return resourceSets[id];
            }
        };

        // add new windows
        desktop.clearWindows();
        WindowDTO[] windows = dto.getWindows();
        for (WindowDTO wDTO : windows) {
            String contentType = wDTO.getContentType();

            int width = wDTO.getWidth();
            int height = wDTO.getHeight();
            String title = wDTO.getTitle();
            int x = wDTO.getX();
            int y = wDTO.getY();

            WindowContent content = viewFactory
                    .createWindowContent(contentType);

            content.setLabel(title);

            desktop.createWindow(content, x, y, width, height);

            /*
             * important: we restore the content after the window was created,
             * because different view content objects such as the timeline
             * require the view to be attached to the DOM.
             */
            if (content instanceof Persistable) {
                ((Persistable) content).restore(wDTO.getViewState(),
                        restorationService, accessor);
            }
        }

        return workspace;
    }

    @Override
    public void loadWorkspacePreviews(
            final AsyncCallback<List<WorkspacePreview>> callback) {

        assert callback != null;

        service.loadWorkspacePreviews(new AsyncCallback<List<WorkspacePreviewDTO>>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(List<WorkspacePreviewDTO> result) {
                List<WorkspacePreview> previews = new ArrayList<WorkspacePreview>();
                for (WorkspacePreviewDTO dto : result) {
                    previews.add(new WorkspacePreview(dto.getId(), dto
                            .getName(), dto.getId().equals(
                            workspaceManager.getWorkspace().getId())));
                }

                callback.onSuccess(previews);
            }
        });
    }

    private void restoreResources(WorkspaceDTO dto) {
        resourceManager.clear();
        Resource[] resources = dto.getResources();
        for (Resource resource : resources) {
            resourceManager.add(resource);
            // TODO need to allocate once allocation / removal is redone?
        }
    }

    @Override
    public void saveWorkspace(final AsyncCallback<Void> callback) {
        assert callback != null;

        final Workspace workspace = workspaceManager.getWorkspace();
        workspace.setSavingState(WorkspaceSavingState.SAVING);

        WorkspaceDTO workspaceDTO = createWorkspaceDTO(workspace);

        service.saveWorkspace(workspaceDTO,
                new AsyncCallbackVoidDelegate<Long>(callback) {
                    @Override
                    public void onFailure(Throwable caught) {
                        workspace
                                .setSavingState(WorkspaceSavingState.NOT_SAVED);
                        super.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(Long result) {
                        workspace.setId(result);
                        workspace.setSavingState(WorkspaceSavingState.SAVED);
                        super.onSuccess(result);
                    }
                });
    }

    @Override
    public void shareWorkspace(final String emailAddress,
            final AsyncCallback<Void> callback) {

        assert callback != null;
        assert emailAddress != null;

        final Workspace workspace = workspaceManager.getWorkspace();

        /*
         * If workspace is new, save it first and then share it.
         */
        if (workspace.isNew()) {
            saveWorkspace(new AsyncCallbackVoidDelegate<Void>(callback) {
                @Override
                public void onSuccess(Void result) {
                    WorkspaceDTO workspaceDTO = createWorkspaceDTO(workspace);
                    sharingService.shareWorkspace(workspaceDTO, emailAddress,
                            callback);
                }
            });
        } else {
            WorkspaceDTO workspaceDTO = createWorkspaceDTO(workspace);
            sharingService.shareWorkspace(workspaceDTO, emailAddress, callback);
        }
    }
}
