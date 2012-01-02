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
import org.thechiselgroup.biomixer.client.core.util.HasDescription;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.SelectionModel;

public class RemoveSelectionSetFromViewCommand extends AbstractUndoableCommand
        implements HasDescription {

    private String description;

    private ResourceSet resourceSet;

    protected SelectionModel selectionModel;

    private boolean wasSelected = false;

    public RemoveSelectionSetFromViewCommand(SelectionModel selectionModel,
            ResourceSet resourceSet) {

        // XXX need better label
        this(selectionModel, resourceSet, "Remove selection set '"
                + resourceSet.getLabel() + "' from view");
    }

    public RemoveSelectionSetFromViewCommand(SelectionModel selectionModel,
            ResourceSet resourceSet, String description) {

        assert selectionModel != null;
        assert resourceSet != null;
        assert resourceSet.hasLabel();
        assert description != null;

        this.description = description;
        this.selectionModel = selectionModel;
        this.resourceSet = resourceSet;
    }

    // TODO add view name / label once available
    @Override
    public String getDescription() {
        return description;
    }

    public ResourceSet getResourceSet() {
        return resourceSet;
    }

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    @Override
    public void performExecute() {
        assert selectionModel.containsSelectionSet(resourceSet);
        if (resourceSet.equals(selectionModel.getSelection())) {
            selectionModel.setSelection(null);
            wasSelected = true;
        }
        selectionModel.removeSelectionSet(resourceSet);
        assert !selectionModel.containsSelectionSet(resourceSet);
    }

    @Override
    public void performUndo() {
        assert !selectionModel.containsSelectionSet(resourceSet);
        selectionModel.addSelectionSet(resourceSet);
        if (wasSelected) {
            selectionModel.setSelection(resourceSet);
        }
        assert selectionModel.containsSelectionSet(resourceSet);
    }

}