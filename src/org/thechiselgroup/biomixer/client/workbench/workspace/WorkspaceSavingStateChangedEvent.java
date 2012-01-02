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

import com.google.gwt.event.shared.GwtEvent;

public class WorkspaceSavingStateChangedEvent extends
        GwtEvent<WorkspaceSavingStateChangedEventHandler> {

    public static final Type<WorkspaceSavingStateChangedEventHandler> TYPE = new Type<WorkspaceSavingStateChangedEventHandler>();

    private final WorkspaceSavingState state;

    private final Workspace workspace;

    public WorkspaceSavingStateChangedEvent(Workspace workspace,
            WorkspaceSavingState state) {

        this.workspace = workspace;
        this.state = state;
    }

    @Override
    protected void dispatch(WorkspaceSavingStateChangedEventHandler handler) {
        handler.onWorkspaceSavingStateChanged(this);
    }

    @Override
    public Type<WorkspaceSavingStateChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public WorkspaceSavingState getState() {
        return state;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

}