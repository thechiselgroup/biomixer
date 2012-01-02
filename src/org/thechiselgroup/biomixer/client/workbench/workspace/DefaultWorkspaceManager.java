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

import org.thechiselgroup.choosel.core.client.command.CommandManager;
import org.thechiselgroup.choosel.dnd.client.windows.Desktop;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

/**
 * Responsibility: link current workspace to system resources (desktop, etc)
 */
public class DefaultWorkspaceManager implements WorkspaceManager {

    private final CommandManager commandManager;

    private final Desktop desktop;

    private HandlerManager handlerManager = new HandlerManager(this);

    private Workspace workspace;

    @Inject
    public DefaultWorkspaceManager(Desktop desktop,
            CommandManager commandManager) {

        assert desktop != null;
        assert commandManager != null;

        this.commandManager = commandManager;
        this.desktop = desktop;
    }

    @Override
    public HandlerRegistration addSwitchedWorkspaceEventHandler(
            WorkspaceSwitchedEventHandler h) {

        return handlerManager.addHandler(WorkspaceSwitchedEvent.TYPE, h);
    }

    @Override
    public void createNewWorkspace() {
        Workspace workspace = new Workspace();
        workspace.setName("Untitled Workspace");
        workspace.setSavingState(WorkspaceSavingState.NOT_SAVED);

        // TODO is this the right way?? workspace should have window handles...
        // and restore/clear should take place in set workspace
        desktop.clearWindows();

        setWorkspace(workspace);
    }

    @Override
    public Workspace getWorkspace() {
        if (workspace == null) {
            createNewWorkspace();
        }

        return workspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        assert workspace != null;

        if ((workspace.getId() != null)
                && (workspace.getId().equals(this.workspace.getId()))) {
            return;
        }

        this.workspace = workspace;

        commandManager.clear();

        handlerManager.fireEvent(new WorkspaceSwitchedEvent(workspace));
    }

}
