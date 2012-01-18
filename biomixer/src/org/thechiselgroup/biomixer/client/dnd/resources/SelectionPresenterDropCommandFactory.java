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
package org.thechiselgroup.biomixer.client.dnd.resources;

import org.thechiselgroup.biomixer.client.core.command.AbstractUndoableCommand;
import org.thechiselgroup.biomixer.client.core.command.UndoableCommand;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.util.HasDescription;
import org.thechiselgroup.biomixer.client.core.visualization.ViewAccessor;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.SelectionModel;

import com.google.gwt.user.client.ui.Widget;

public class SelectionPresenterDropCommandFactory extends
        AbstractResourceSetAvatarDropCommandFactory {

    // TODO refactor (replace avatar with resource set)
    private class AddSelectionSetCommand extends AbstractUndoableCommand implements
            HasDescription {
        private final ResourceSetAvatar avatar;

        private ResourceSet oldSelection;

        private AddSelectionSetCommand(ResourceSetAvatar avatar) {
            this.avatar = avatar;
        }

        @Override
        public void performExecute() {
            oldSelection = getSelectionModel().getSelection();
            getSelectionModel().addSelectionSet(avatar.getResourceSet());
            getSelectionModel().setSelection(avatar.getResourceSet());
        }

        @Override
        public String getDescription() {
            return "add resource set as selection to view";
        }

        @Override
        public void performUndo() {
            getSelectionModel().setSelection(oldSelection);
            getSelectionModel().removeSelectionSet(avatar.getResourceSet());
        }
    }

    public SelectionPresenterDropCommandFactory(Widget dropTarget,
            ViewAccessor viewAccessor) {

        super(dropTarget, viewAccessor);
    }

    @Override
    public boolean canDrop(ResourceSetAvatar avatar) {
        assert avatar != null;

        ResourceSet avatarResources = avatar.getResourceSet();

        if (!avatarResources.hasLabel()
                || getSelectionModel().containsSelectionSet(avatarResources)) {
            return false;
        }

        ResourceSet viewResources = getTargetView().getModel()
                .getContentResourceSet();

        assert viewResources != null;

        return !viewResources.getIntersection(avatarResources).isEmpty();
    }

    @Override
    public UndoableCommand createCommand(final ResourceSetAvatar avatar) {
        assert avatar != null;

        return new AddSelectionSetCommand(avatar);
    }

    private SelectionModel getSelectionModel() {
        return getTargetView().getSelectionModel();
    }

}