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
package org.thechiselgroup.biomixer.client.workbench.command;

import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePersistenceManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePreview;
import org.thechiselgroup.biomixer.client.workbench.workspace.command.LoadWorkspaceDialogCommand;
import org.thechiselgroup.biomixer.client.workbench.workspace.command.LoadWorkspaceDialogCommand.DetailsDisplay;
import org.thechiselgroup.biomixer.client.workbench.workspace.command.LoadWorkspaceDialogCommand.LoadWorkspacePreviewsCallback;
import org.thechiselgroup.choosel.core.client.util.callbacks.NullAsyncCallback;

public class LoadWorkspaceDialogCommandTest {

    private static <T> T isNotNull(Class<T> clazz) {
        return clazz.cast(and(any(clazz), Matchers.isNotNull()));
    }

    @Mock
    private DetailsDisplay detailsDisplay;

    @Mock
    private WorkspacePersistenceManager persistenceManager;

    private LoadWorkspaceDialogCommand presenter;

    @Test
    public void callGetAllWorkspacesOnExecute() {
        presenter.execute(new NullAsyncCallback<Void>());

        verify(persistenceManager).loadWorkspacePreviews(
                isNotNull(LoadWorkspacePreviewsCallback.class));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        presenter = new LoadWorkspaceDialogCommand(detailsDisplay,
                persistenceManager);
    }

    @Test
    public void showDetailsDisplayOnSuccess() {
        LoadWorkspacePreviewsCallback callback = new LoadWorkspacePreviewsCallback(
                detailsDisplay, new NullAsyncCallback<Void>());

        List<WorkspacePreview> workspaces = new ArrayList<WorkspacePreview>();
        callback.onSuccess(workspaces);

        verify(detailsDisplay).show(workspaces);
    }
}
