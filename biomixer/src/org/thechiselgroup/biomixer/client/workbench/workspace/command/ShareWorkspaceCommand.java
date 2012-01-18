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

import org.thechiselgroup.biomixer.client.core.command.AsyncCommand;
import org.thechiselgroup.biomixer.client.workbench.services.AsyncCallbackDelegate;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePersistenceManager;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ShareWorkspaceCommand implements AsyncCommand {

    private WorkspacePersistenceManager persistenceManager;

    @Inject
    public ShareWorkspaceCommand(WorkspacePersistenceManager persistenceManager) {
        assert persistenceManager != null;
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void execute(AsyncCallback<Void> callback) {
        String emailAddress = Window.prompt(
                // "Enter email address of person who "
                // + "should be invited to work on this workspace",
                "Please enter the email address of the collaborator:",
                "example@example.org");

        if (emailAddress == null) {
            // cancel was pressed
            callback.onSuccess(null);
            return;
        }

        // TODO message
        persistenceManager.shareWorkspace(emailAddress,
                new AsyncCallbackDelegate<Void>(callback));
    }
}