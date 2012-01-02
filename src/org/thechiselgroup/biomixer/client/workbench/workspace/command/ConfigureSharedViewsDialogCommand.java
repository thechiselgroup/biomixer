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

import org.thechiselgroup.biomixer.client.workbench.services.AsyncCallbackVoidDelegate;
import org.thechiselgroup.biomixer.client.workbench.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewLoadManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewLoader;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewPreview;
import org.thechiselgroup.choosel.core.client.command.AsyncCommand;
import org.thechiselgroup.choosel.core.client.command.AsyncCommandExecutor;
import org.thechiselgroup.choosel.core.client.util.HasDescription;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

// TODO refactor (use AsyncCommand, split into two classes)
public class ConfigureSharedViewsDialogCommand implements AsyncCommand,
        HasDescription {

    // TODO clean up & refactor dialog code
    public static class DefaultDetailsDisplay implements DetailsDisplay {

        private AsyncCommandExecutor asyncCommandExecutor;

        private DialogManager dialogManager;

        private ViewLoader persistenceManager;

        @Inject
        public DefaultDetailsDisplay(ViewLoader persistenceManager,
                DialogManager dialogManager,
                AsyncCommandExecutor asyncCommandExecutor) {

            this.persistenceManager = persistenceManager;
            this.dialogManager = dialogManager;
            this.asyncCommandExecutor = asyncCommandExecutor;
        }

        @Override
        public void show(final List<ViewPreview> views) {
            dialogManager.show(new ConfigureSharedViewsDialog(views,
                    asyncCommandExecutor, persistenceManager));
        }
    }

    public interface DetailsDisplay {

        void show(List<ViewPreview> workspacePreviews);

    }

    public static class LoadViewPreviewsCallback extends
            AsyncCallbackVoidDelegate<List<ViewPreview>> {

        private final DetailsDisplay detailsDisplay;

        public LoadViewPreviewsCallback(DetailsDisplay detailsDisplay,
                AsyncCallback<Void> callback) {

            super(callback);
            this.detailsDisplay = detailsDisplay;
        }

        @Override
        public void onSuccess(List<ViewPreview> workspaces) {
            detailsDisplay.show(workspaces);
            super.onSuccess(workspaces);
        }

    }

    private DetailsDisplay detailsDisplay;

    private ViewLoadManager persistenceManager;

    @Inject
    public ConfigureSharedViewsDialogCommand(DetailsDisplay detailsDisplay,
            ViewLoadManager persistenceManager) {
        this.detailsDisplay = detailsDisplay;
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void execute(AsyncCallback<Void> callback) {
        persistenceManager.loadViewPreviews(new LoadViewPreviewsCallback(
                detailsDisplay, callback));
    }

    @Override
    public String getDescription() {
        return "Loading Views...";
    }

}