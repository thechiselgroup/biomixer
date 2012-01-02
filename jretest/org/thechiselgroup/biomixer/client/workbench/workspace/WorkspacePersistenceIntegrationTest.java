/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.choosel.core.client.resources.ResourceSetTestUtils.createResource;
import static org.thechiselgroup.choosel.core.shared.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.WorkspacePersistenceServiceAsync;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.WorkspaceSharingServiceAsync;
import org.thechiselgroup.choosel.core.client.persistence.PersistableRestorationService;
import org.thechiselgroup.choosel.core.client.resources.DefaultResourceManager;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceManager;
import org.thechiselgroup.choosel.core.client.resources.ResourceSetFactory;
import org.thechiselgroup.choosel.core.client.test.IntegrationTest;
import org.thechiselgroup.choosel.core.client.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.choosel.dnd.client.windows.Desktop;
import org.thechiselgroup.choosel.dnd.client.windows.WindowContentProducer;

import com.google.gwt.user.client.rpc.AsyncCallback;

@IntegrationTest
public class WorkspacePersistenceIntegrationTest {

    private ResourceManager resourceManager;

    private DefaultWorkspacePersistenceManager persistenceManager;

    @SuppressWarnings("unchecked")
    @Test
    public void saveRestoreResourceManagerContent() {
        Resource resource = createResource(1);

        resourceManager.add(resource);
        persistenceManager.saveWorkspace(mock(AsyncCallback.class));
        resourceManager.clear();
        resourceManager.add(createResource(2));

        persistenceManager.loadWorkspace(0l, mock(AsyncCallback.class));

        assertThat(resourceManager, containsExactly(resource));
    }

    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();

        resourceManager = new DefaultResourceManager();

        Workspace workspace = new Workspace();

        WorkspaceManager workspaceManager = mock(WorkspaceManager.class);

        when(workspaceManager.getWorkspace()).thenReturn(workspace);

        Desktop desktop = mock(Desktop.class);
        WindowContentProducer viewFactory = mock(WindowContentProducer.class);
        WorkspacePersistenceServiceAsync persistenceService = new TestWorkspacePersistenceServiceAsync();
        ResourceSetFactory resourceSetFactory = mock(ResourceSetFactory.class);
        WorkspaceSharingServiceAsync sharingService = mock(WorkspaceSharingServiceAsync.class);
        PersistableRestorationService restorationService = mock(PersistableRestorationService.class);

        persistenceManager = new DefaultWorkspacePersistenceManager(
                workspaceManager, desktop, persistenceService, viewFactory,
                resourceManager, resourceSetFactory, sharingService,
                restorationService);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }

}