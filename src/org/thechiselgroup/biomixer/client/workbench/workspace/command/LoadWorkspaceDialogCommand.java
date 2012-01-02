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
package org.thechiselgroup.biomixer.client.workbench.workspace.command;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.command.AsyncCommand;
import org.thechiselgroup.biomixer.client.core.command.AsyncCommandExecutor;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.core.util.HasDescription;
import org.thechiselgroup.biomixer.client.workbench.services.AsyncCallbackVoidDelegate;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePersistenceManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePreview;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

// TODO refactor (use AsyncCommand, split into two classes)
public class LoadWorkspaceDialogCommand implements AsyncCommand, HasDescription {

    // TODO clean up & refactor dialog code
    public static class DefaultDetailsDisplay implements DetailsDisplay {

        private AsyncCommandExecutor asyncCommandExecutor;

        private DialogManager dialogManager;

        private WorkspacePersistenceManager persistenceManager;

        @Inject
        public DefaultDetailsDisplay(
                WorkspacePersistenceManager persistenceManager,
                DialogManager dialogManager,
                AsyncCommandExecutor asyncCommandExecutor) {

            this.persistenceManager = persistenceManager;
            this.dialogManager = dialogManager;
            this.asyncCommandExecutor = asyncCommandExecutor;
        }

        @Override
        public void show(final List<WorkspacePreview> workspaces) {
            dialogManager.show(new LoadWorkspaceDialog(workspaces,
                    asyncCommandExecutor, persistenceManager));
        }
    }

    public interface DetailsDisplay {

        void show(List<WorkspacePreview> workspacePreviews);

    }

    public static class LoadWorkspacePreviewsCallback extends
            AsyncCallbackVoidDelegate<List<WorkspacePreview>> {

        private final DetailsDisplay detailsDisplay;

        public LoadWorkspacePreviewsCallback(DetailsDisplay detailsDisplay,
                AsyncCallback<Void> callback) {

            super(callback);
            this.detailsDisplay = detailsDisplay;
        }

        @Override
        public void onSuccess(List<WorkspacePreview> workspaces) {
            detailsDisplay.show(workspaces);
            super.onSuccess(workspaces);
        }

    }

    private DetailsDisplay detailsDisplay;

    private WorkspacePersistenceManager persistenceManager;

    @Inject
    public LoadWorkspaceDialogCommand(DetailsDisplay detailsDisplay,
            WorkspacePersistenceManager persistenceManager) {
        this.detailsDisplay = detailsDisplay;
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void execute(AsyncCallback<Void> callback) {
        persistenceManager
                .loadWorkspacePreviews(new LoadWorkspacePreviewsCallback(
                        detailsDisplay, callback));
    }

    @Override
    public String getDescription() {
        return "Loading workspaces...";
    }

}