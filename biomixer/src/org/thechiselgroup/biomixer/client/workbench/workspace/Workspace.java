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

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class Workspace {

    /*
     * Model of this workspace. Should have references to all elements that are
     * should be stored / loaded.
     */

    private HandlerManager handlerManager = new HandlerManager(this);

    private Long id;

    private String name;

    private WorkspaceSavingState savingState;

    public HandlerRegistration addWorkspaceSavingStateChangeHandler(
            WorkspaceSavingStateChangedEventHandler handler) {
        return this.handlerManager.addHandler(
                WorkspaceSavingStateChangedEvent.TYPE, handler);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public WorkspaceSavingState getSavingState() {
        return savingState;
    }

    public boolean isNew() {
        return id == null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSavingState(WorkspaceSavingState newSavingState) {
        assert newSavingState != null;

        if (newSavingState == this.savingState) {
            return;
        }

        this.savingState = newSavingState;

        this.handlerManager.fireEvent(new WorkspaceSavingStateChangedEvent(
                this, savingState));
    }

}