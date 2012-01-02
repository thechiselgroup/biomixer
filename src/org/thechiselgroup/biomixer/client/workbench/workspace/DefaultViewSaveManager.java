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

import org.thechiselgroup.biomixer.client.workbench.services.AsyncCallbackDelegate;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ResourceSetDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ViewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.ViewPersistenceServiceAsync;
import org.thechiselgroup.choosel.core.client.error_handling.LoggingErrorHandler;
import org.thechiselgroup.choosel.core.client.persistence.Persistable;
import org.thechiselgroup.choosel.core.client.resources.DefaultResourceSet;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.UnmodifiableResourceSet;
import org.thechiselgroup.choosel.core.client.resources.persistence.DefaultResourceSetCollector;
import org.thechiselgroup.choosel.core.client.visualization.View;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DefaultViewSaveManager implements ViewSaveManager {

    private ViewPersistenceServiceAsync service;

    private final LoggingErrorHandler loggingErrorHandler;

    @Inject
    public DefaultViewSaveManager(ViewPersistenceServiceAsync service,
            LoggingErrorHandler loggingErrorHandler) {

        assert service != null;

        this.loggingErrorHandler = loggingErrorHandler;
        this.service = service;
    }

    private ViewDTO createViewDTO(View view) {
        assert view instanceof Persistable;

        ViewDTO viewDTO = new ViewDTO();

        DefaultResourceSetCollector persistanceManager = new DefaultResourceSetCollector();

        // TODO use view content label instead of window title
        // viewDTO.setTitle(window.getWindowTitle()); We will eventually store
        // the title

        viewDTO.setContentType(view.getContentType());

        Persistable persistable = view;
        viewDTO.setViewState(persistable.save(persistanceManager));

        // Resource set DTOs
        // 1. resolved unmodified sets --> changes list size
        List<ResourceSet> resourceSets = new ArrayList<ResourceSet>(
                persistanceManager.getResourceSets());
        for (ResourceSet resourceSet : resourceSets) {
            if (resourceSet instanceof UnmodifiableResourceSet) {
                persistanceManager
                        .storeResourceSet(((UnmodifiableResourceSet) resourceSet)
                                .getDelegate());
            }
        }

        // 2. store sets
        ResourceSet resourceCollector = new DefaultResourceSet();
        ResourceSetDTO[] resourceSetDTOs = new ResourceSetDTO[persistanceManager
                .getResourceSets().size()];
        for (int i = 0; i < persistanceManager.getResourceSets().size(); i++) {
            ResourceSetDTO dto = new ResourceSetDTO();
            ResourceSet resourceSet = persistanceManager.getResourceSets().get(
                    i);

            if (resourceSet.hasLabel()) {
                dto.setLabel(resourceSet.getLabel());
            }

            dto.setId(i);

            if (resourceSet instanceof UnmodifiableResourceSet) {
                ResourceSet sourceSet = ((UnmodifiableResourceSet) resourceSet)
                        .getDelegate();

                dto.setDelegateSetId(persistanceManager
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
        viewDTO.setResourceSets(resourceSetDTOs);

        Resource[] resources = new Resource[resourceCollector.size()];
        int count = 0;
        for (Resource resource : resourceCollector) {
            resources[count++] = resource;
        }

        viewDTO.setResources(resources);

        viewDTO.setTitle(view.getLabel());

        return viewDTO;
    }

    // XXX should be called with view, not with shareConfiguration
    @Override
    public void saveView(final DefaultShareConfiguration shareConfiguration,
            final AsyncCallback<Long> callback) {
        assert callback != null;

        ViewDTO viewDTO = createViewDTO(shareConfiguration.getView());

        service.saveView(viewDTO, new AsyncCallbackDelegate<Long>(callback) {
            @Override
            public void onFailure(Throwable caught) {
                loggingErrorHandler.handleError(caught);
                shareConfiguration.notLoggedIn();
            }

            @Override
            public void onSuccess(Long result) {
                shareConfiguration.updateSharePanel(result);
                super.onSuccess(result);
            }
        });

    }

}
