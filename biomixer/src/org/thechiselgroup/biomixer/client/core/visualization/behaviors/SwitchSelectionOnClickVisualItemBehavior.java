/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.visualization.behaviors;

import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.resources.command.SwitchSelectionCommand;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.SelectionModel;

/**
 * Manages {@link VisualItem} highlighting in a single view.
 * 
 * @author Lars Grammel
 * @author Del Myers
 */
public class SwitchSelectionOnClickVisualItemBehavior implements
        VisualItemBehavior {

    private SelectionModel selectionModel;

    private CommandManager commandManager;

    public SwitchSelectionOnClickVisualItemBehavior(
            SelectionModel selectionModel, CommandManager commandManager) {

        assert selectionModel != null;
        assert commandManager != null : "CommandManager must not be null";

        this.selectionModel = selectionModel;
        this.commandManager = commandManager;
    }

    protected SelectionModel getSelectionModel() {
        return selectionModel;
    }

    @Override
    public void onInteraction(VisualItem visualItem,
            VisualItemInteraction interaction) {
        assert visualItem != null;
        assert interaction != null;

        switch (interaction.getEventType()) {
        case CLICK:
            switchSelection(visualItem);
            break;
        }
    }

    @Override
    public void onVisualItemContainerChanged(VisualItemContainerChangeEvent event) {
    }

    protected void switchSelection(VisualItem visualItem) {
        commandManager.execute(new SwitchSelectionCommand(visualItem
                .getResources(), getSelectionModel()));
    }

}