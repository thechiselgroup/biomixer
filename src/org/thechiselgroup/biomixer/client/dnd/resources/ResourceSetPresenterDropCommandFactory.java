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

import org.thechiselgroup.choosel.core.client.command.UndoableCommand;
import org.thechiselgroup.choosel.core.client.resources.command.AddResourceSetToResourceSetCommand;
import org.thechiselgroup.choosel.core.client.resources.command.MergeResourceSetsCommand;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.choosel.core.client.visualization.ViewAccessor;

public class ResourceSetPresenterDropCommandFactory extends
        AbstractResourceSetAvatarDropCommandFactory {

    private ResourceSetAvatar targetDragAvatar;

    public ResourceSetPresenterDropCommandFactory(
            ResourceSetAvatar targetDragAvatar, ViewAccessor viewAccessor) {

        super(targetDragAvatar, viewAccessor);

        this.targetDragAvatar = targetDragAvatar;
    }

    private boolean areResourcesDifferentFromTarget(ResourceSetAvatar dragAvatar) {
        return dragAvatar.getResourceSet() != targetDragAvatar.getResourceSet();
    }

    @Override
    public boolean canDrop(ResourceSetAvatar dragAvatar) {
        assert dragAvatar != null;
        return isTargetModifiable()
                && areResourcesDifferentFromTarget(dragAvatar)
                && !targetContainsAllResources(dragAvatar);
    }

    @Override
    public UndoableCommand createCommand(ResourceSetAvatar dragAvatar) {
        assert dragAvatar != null;

        if (isDragAvatarFromTargetView(dragAvatar)
                && (dragAvatar.getType() == ResourceSetAvatarType.SET)) {

            return new MergeResourceSetsCommand(dragAvatar.getResourceSet(),
                    targetDragAvatar.getResourceSet(), findView(dragAvatar)
                            .getResourceModel());
        }

        return new AddResourceSetToResourceSetCommand(
                dragAvatar.getResourceSet(), targetDragAvatar.getResourceSet());
    }

    private boolean isTargetModifiable() {
        return targetDragAvatar.getResourceSet().isModifiable();
    }

    private boolean targetContainsAllResources(ResourceSetAvatar dragAvatar) {
        return targetDragAvatar.getResourceSet().containsAll(
                dragAvatar.getResourceSet());
    }
}