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

import org.thechiselgroup.biomixer.client.workbench.workspace.ViewLoader;
import org.thechiselgroup.biomixer.client.workbench.workspace.Workspace;
import org.thechiselgroup.choosel.core.client.command.AsyncCommand;
import org.thechiselgroup.choosel.core.client.util.HasDescription;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoadViewAsWindowCommand implements AsyncCommand, HasDescription {

    private ViewLoader persistenceManager;

    private Long viewId;

    public LoadViewAsWindowCommand(Long workspaceId,
            ViewLoader persistenceManager) {

        assert workspaceId != null;
        assert persistenceManager != null;

        viewId = workspaceId;
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void execute(final AsyncCallback<Void> callback) {
        persistenceManager.loadViewAsWindow(viewId,
                new AsyncCallback<Workspace>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(Workspace workspace) {
                        callback.onSuccess(null);
                    }
                });
    }

    @Override
    public String getDescription() {
        String result = "Loading View in Workspace";

        return result;
    }
}