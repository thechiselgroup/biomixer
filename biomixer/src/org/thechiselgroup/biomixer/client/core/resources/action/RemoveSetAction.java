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
package org.thechiselgroup.biomixer.client.core.resources.action;

import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.command.UndoableCommand;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.command.RemoveResourceSetFromResourceModelCommand;
import org.thechiselgroup.biomixer.client.core.resources.ui.popup.PopupResourceSetAvatarFactory.Action;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

public class RemoveSetAction implements Action {

    private CommandManager commandManager;

    public RemoveSetAction(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    protected UndoableCommand createCommand(ResourceSet resources,
            ResourceModel resourceModel) {

        return new RemoveResourceSetFromResourceModelCommand(resourceModel,
                resources, "Remove set '" + resources.getLabel()
                        + "' from selection");
    }

    @Override
    public void execute(ResourceSet resources, View view) {
        commandManager
                .execute(createCommand(resources, view.getResourceModel()));
    }

    @Override
    public String getLabel() {
        return "Remove set from view";
    }
}