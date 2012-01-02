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

import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ResourceSetDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ViewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ViewPreviewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.ViewPersistenceServiceAsync;
import org.thechiselgroup.choosel.core.client.persistence.Persistable;
import org.thechiselgroup.choosel.core.client.persistence.PersistableRestorationService;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceManager;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.ResourceSetFactory;
import org.thechiselgroup.choosel.core.client.resources.UnmodifiableResourceSet;
import org.thechiselgroup.choosel.core.client.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.choosel.core.client.util.callbacks.TransformingAsyncCallback;
import org.thechiselgroup.choosel.core.client.util.transform.Transformer;
import org.thechiselgroup.choosel.core.client.visualization.View;
import org.thechiselgroup.choosel.dnd.client.windows.Desktop;
import org.thechiselgroup.choosel.dnd.client.windows.ViewWindowContent;
import org.thechiselgroup.choosel.dnd.client.windows.WindowContent;
import org.thechiselgroup.choosel.dnd.client.windows.WindowContentProducer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DefaultViewLoadManager implements ViewLoadManager {

    public static interface ViewInitializer {

        void init(WindowContent content);

    }

    private ViewPersistenceServiceAsync service;

    private ResourceSetFactory resourceSetFactory;

    private ResourceManager resourceManager;

    private final WindowContentProducer windowContentProducer;

    private final WorkspaceManager workspaceManager;

    private final Desktop desktop;

    private PersistableRestorationService restorationService;

    @Inject
    public DefaultViewLoadManager(ViewPersistenceServiceAsync service,
            ResourceManager resourceManager,
            ResourceSetFactory resourceSetFactory,
            WindowContentProducer windowContentProducer,
            WorkspaceManager workspaceManager, Desktop desktop,
            PersistableRestorationService restorationService) {

        assert resourceManager != null;
        assert service != null;
        assert resourceSetFactory != null;
        assert restorationService != null;

        this.desktop = desktop;
        this.workspaceManager = workspaceManager;
        this.windowContentProducer = windowContentProducer;
        this.resourceSetFactory = resourceSetFactory;
        this.resourceManager = resourceManager;
        this.service = service;
        this.restorationService = restorationService;
    }

    @Override
    public void deleteView(Long id, final AsyncCallback<Long> callback) {
        assert callback != null;
        service.deleteView(id, callback);
    }

    private WindowContent loadResourcesAndView(ViewDTO dto,
            ViewInitializer viewInitializer) {

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

        final WindowContent content = windowContentProducer
                .createWindowContent(dto.getContentType());

        content.setLabel(dto.getTitle());

        viewInitializer.init(content);

        /*
         * important: we restore the content after the window was created,
         * because different view content objects such as the timeline require
         * the view to be attached to the DOM.
         */
        if (content instanceof Persistable) {
            ((Persistable) content).restore(dto.getViewState(),
                    restorationService, accessor);
        }

        return content;
    }

    @Override
    public void loadView(Long id, AsyncCallback<View> callback) {
        service.loadView(id, TransformingAsyncCallback.create(callback,
                new Transformer<ViewDTO, View>() {
                    @Override
                    public View transform(ViewDTO dto) throws Exception {
                        return loadView(dto);
                    }
                }));
    }

    protected View loadView(ViewDTO dto) {
        return ((ViewWindowContent) loadResourcesAndView(dto,
                new ViewInitializer() {
                    @Override
                    public void init(WindowContent content) {
                        content.init();
                    }
                })).getView();
    }

    @Override
    public void loadViewAsWindow(Long id, AsyncCallback<Workspace> callback) {
        service.loadView(id, TransformingAsyncCallback.create(callback,
                new Transformer<ViewDTO, Workspace>() {
                    @Override
                    public Workspace transform(ViewDTO dto) throws Exception {
                        return loadWindow(dto);
                    }
                }));
    }

    @Override
    public void loadViewAsWorkspace(Long id, AsyncCallback<Workspace> callback) {
        service.loadView(id, TransformingAsyncCallback.create(callback,
                new Transformer<ViewDTO, Workspace>() {
                    @Override
                    public Workspace transform(ViewDTO dto) {
                        return loadWorkspace(dto);
                    }
                }));
    }

    @Override
    public void loadViewPreviews(final AsyncCallback<List<ViewPreview>> callback) {
        service.loadViewPreviews(TransformingAsyncCallback.create(callback,
                new Transformer<List<ViewPreviewDTO>, List<ViewPreview>>() {
                    @Override
                    public List<ViewPreview> transform(
                            List<ViewPreviewDTO> result) {

                        List<ViewPreview> previews = new ArrayList<ViewPreview>();
                        for (ViewPreviewDTO dto : result) {
                            previews.add(new ViewPreview(dto.getId(), dto
                                    .getTitle(), dto.getType(), dto
                                    .getCreated()));
                        }
                        return previews;
                    }
                }));
    }

    protected Workspace loadWindow(ViewDTO dto) {
        Workspace workspace = workspaceManager.getWorkspace();

        loadResourcesAndView(dto, new ViewInitializer() {
            @Override
            public void init(WindowContent content) {
                desktop.createWindow(content);
            }
        });

        return workspace;
    }

    protected Workspace loadWorkspace(ViewDTO dto) {
        desktop.clearWindows();
        workspaceManager.createNewWorkspace();

        return loadWindow(dto);
    }

    private void restoreResources(ViewDTO dto) {
        resourceManager.clear();
        Resource[] resources = dto.getResources();
        for (Resource resource : resources) {
            resourceManager.add(resource);
            // TODO need to allocate once allocation / removal is redone?
        }
    }

}
