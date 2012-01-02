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
package org.thechiselgroup.biomixer.client.core.resources.command;

import org.thechiselgroup.biomixer.client.core.command.AbstractUndoableCommand;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.SelectionModel;

/**
 * Command for adding/removing resources from a current selection.
 * 
 * @author Del
 * 
 */
public class SwitchSelectionCommand extends AbstractUndoableCommand {

    private SelectionModel selectionModel;

    private ResourceSet resources;

    public SwitchSelectionCommand(ResourceSet resources,
            SelectionModel selectionModel) {
        this.selectionModel = selectionModel;
        this.resources = resources;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.choosel.core.client.command.AbstractUndoableCommand
     * #performExecute()
     */
    @Override
    public void performExecute() {
        // switch selection is a toggle, so undo/redo should be the same.
        selectionModel.switchSelection(resources);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.choosel.core.client.command.AbstractUndoableCommand
     * #performUndo()
     */
    @Override
    public void performUndo() {
        // switch selection is a toggle, so undo/redo should be the same.
        selectionModel.switchSelection(resources);
    }

}
