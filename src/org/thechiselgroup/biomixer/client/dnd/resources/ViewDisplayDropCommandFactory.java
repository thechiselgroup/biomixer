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

import org.thechiselgroup.biomixer.client.core.command.UndoableCommand;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.command.AddResourceSetToViewCommand;
import org.thechiselgroup.biomixer.client.core.resources.command.AddResourcesToResourceModelCommand;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.biomixer.client.core.visualization.ViewAccessor;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

import com.google.gwt.user.client.ui.Widget;

public class ViewDisplayDropCommandFactory extends
        AbstractResourceSetAvatarDropCommandFactory {

    public ViewDisplayDropCommandFactory(Widget dropTarget,
            ViewAccessor viewAccessor) {

        super(dropTarget, viewAccessor);
    }

    @Override
    public boolean canDrop(ResourceSetAvatar dragAvatar) {
        assert dragAvatar != null;
        return !isDragAvatarFromTargetView(dragAvatar)
                && !isAlreadyContained(dragAvatar);
    }

    @Override
    public UndoableCommand createCommand(ResourceSetAvatar dragAvatar) {
        assert dragAvatar != null;

        ResourceSet addedResources = dragAvatar.getResourceSet();

        assert addedResources != null;

        if (!addedResources.hasLabel()
                && dragAvatar.getType() == ResourceSetAvatarType.SET) {

            return new AddResourcesToResourceModelCommand(getResourceModel(),
                    addedResources);
        }

        // XXX Deactivated preventing the renaming of all/selection sets for now
        // if (dragAvatar.getType() != ResourceSetAvatarType.SET) {
        // addedResources = new UnmodifiableResourceSet(addedResources);
        // }

        return new AddResourceSetToViewCommand(getResourceModel(),
                addedResources);
    }

    private ResourceModel getResourceModel() {
        return getTargetView().getResourceModel();
    }

    private boolean isAlreadyContained(ResourceSetAvatar dragAvatar) {
        ResourceSet resources = dragAvatar.getResourceSet();

        if (!resources.hasLabel()
                && dragAvatar.getType() == ResourceSetAvatarType.SET) {
            return getResourceModel().containsResources(resources);
        }

        return getResourceModel().containsResourceSet(resources);
    }
}