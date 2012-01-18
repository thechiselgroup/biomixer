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

import org.thechiselgroup.biomixer.client.core.ui.HasEnabledState;
import org.thechiselgroup.biomixer.client.core.util.Disposable;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasName;

public class SaveActionStateController implements Disposable {

    public static final String MESSAGE_SAVING_WORKSPACE = "Saving Workspace";

    public static final String MESSAGE_SAVED_WORKSPACE = "Saved Workspace";

    public static final String MESSAGE_SAVE_WORKSPACE = "Save Workspace";

    private HasEnabledState hasEnabledState;

    private HasName hasName;

    private HandlerRegistration savingStateHandlerRegistration;

    private WorkspaceManager workspaceManager;

    private HandlerRegistration workspaceSwitchedHandler;

    public SaveActionStateController(WorkspaceManager workspaceManager,
            HasName hasName, HasEnabledState hasEnabledState) {

        this.workspaceManager = workspaceManager;
        this.hasName = hasName;
        this.hasEnabledState = hasEnabledState;
    }

    protected void delayedSwitchToNotSaved() {
        // we use timers for now to switch back to not_saved, once
        // everything
        // is done within commands, we can use those to set the state of the
        // button
        new Timer() {
            @Override
            public void run() {
                workspaceManager.getWorkspace().setSavingState(
                        WorkspaceSavingState.NOT_SAVED);
            }
        }.schedule(2000);
    }

    @Override
    public void dispose() {
        workspaceSwitchedHandler.removeHandler();
        savingStateHandlerRegistration.removeHandler();
    }

    public void init() {
        registerWorkspaceSwitchedHandler();
        setWorkspace(workspaceManager.getWorkspace());
    }

    private void registerWorkspaceSavingStateHandler(Workspace workspace) {
        savingStateHandlerRegistration = workspace
                .addWorkspaceSavingStateChangeHandler(new WorkspaceSavingStateChangedEventHandler() {
                    @Override
                    public void onWorkspaceSavingStateChanged(
                            WorkspaceSavingStateChangedEvent event) {
                        update(event.getState());
                    }
                });
    }

    private void registerWorkspaceSwitchedHandler() {
        workspaceSwitchedHandler = workspaceManager
                .addSwitchedWorkspaceEventHandler(new WorkspaceSwitchedEventHandler() {
                    @Override
                    public void onWorkspaceSwitched(WorkspaceSwitchedEvent event) {
                        setWorkspace(event.getWorkspace());
                    }
                });
    }

    private void setWorkspace(Workspace workspace) {
        registerWorkspaceSavingStateHandler(workspace);
        update(workspace.getSavingState());
    }

    private void update(String label, boolean enabled) {
        hasName.setName(label);
        hasEnabledState.setEnabled(enabled);
    }

    private void update(WorkspaceSavingState state) {
        assert state != null;

        switch (state) {
        case NOT_SAVED: {
            update(MESSAGE_SAVE_WORKSPACE, true);
        }
            break;
        case SAVED: {
            update(MESSAGE_SAVED_WORKSPACE, false);
            delayedSwitchToNotSaved();
        }
            break;
        case SAVING: {
            update(MESSAGE_SAVING_WORKSPACE, false);
        }
            break;
        }
    }

}
