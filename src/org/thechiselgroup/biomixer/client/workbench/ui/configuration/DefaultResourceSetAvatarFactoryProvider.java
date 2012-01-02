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
package org.thechiselgroup.biomixer.client.workbench.ui.configuration;

import org.thechiselgroup.biomixer.client.core.resources.ui.AbstractResourceSetAvatarFactoryProvider;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.biomixer.client.core.resources.ui.UpdateResourceSetAvatarLabelFactory;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;
import org.thechiselgroup.biomixer.client.dnd.resources.DraggableResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.DropTargetResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.HighlightingDraggableResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDragController;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDropTargetManager;

import com.google.inject.Inject;

public class DefaultResourceSetAvatarFactoryProvider extends
        AbstractResourceSetAvatarFactoryProvider {

    @Inject
    public DefaultResourceSetAvatarFactoryProvider(
            ResourceSetAvatarDragController dragController,
            HighlightingModel hoverModel,
            ResourceSetAvatarDropTargetManager dropTargetManager) {

        super(new HighlightingDraggableResourceSetAvatarFactory(
                new DropTargetResourceSetAvatarFactory(
                        new UpdateResourceSetAvatarLabelFactory(
                                new DraggableResourceSetAvatarFactory(
                                        "avatar-resourceSet",
                                        ResourceSetAvatarType.SET,
                                        dragController)), dropTargetManager),
                hoverModel, dragController));
    }
}