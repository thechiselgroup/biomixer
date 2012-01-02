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

import static org.thechiselgroup.choosel.core.client.configuration.ChooselInjectionConstants.AVATAR_FACTORY_ALL_RESOURCES;

import java.util.Collections;

import org.thechiselgroup.biomixer.client.dnd.resources.DraggableResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.DropTargetResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.HighlightingDraggableResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDragController;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDropTargetManager;
import org.thechiselgroup.choosel.core.client.resources.ui.AbstractResourceSetAvatarFactoryProvider;
import org.thechiselgroup.choosel.core.client.resources.ui.DisableIfEmptyResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.FixedLabelResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.choosel.core.client.resources.ui.popup.PopupResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupManagerFactory;
import org.thechiselgroup.choosel.core.client.visualization.ViewAccessor;
import org.thechiselgroup.choosel.core.client.visualization.model.extensions.HighlightingModel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

//TODO refactor see TypeDragAvatarFactoryProvider for example
public class AllResourceSetAvatarFactoryProvider extends
        AbstractResourceSetAvatarFactoryProvider {

    @Inject
    public AllResourceSetAvatarFactoryProvider(
            ResourceSetAvatarDragController dragController,
            HighlightingModel hoverModel,
            @Named(AVATAR_FACTORY_ALL_RESOURCES) ResourceSetAvatarDropTargetManager dropTargetManager,
            ViewAccessor viewAccessor, PopupManagerFactory popupManagerFactory) {

        super(
                new PopupResourceSetAvatarFactory(
                        new HighlightingDraggableResourceSetAvatarFactory(
                                new DropTargetResourceSetAvatarFactory(
                                        new FixedLabelResourceSetAvatarFactory(
                                                new DisableIfEmptyResourceSetAvatarFactory(
                                                        new DraggableResourceSetAvatarFactory(
                                                                "avatar-allResources",
                                                                ResourceSetAvatarType.ALL,
                                                                dragController)),
                                                "All"), dropTargetManager),
                                hoverModel, dragController), viewAccessor,
                        popupManagerFactory, Collections.EMPTY_LIST,
                        "All resources in this view",
                        "<p><b>Drag</b> to add all resources "
                                + "from this view to other views "
                                + "(by dropping on 'All' set), "
                                + "to create filtered views containing "
                                + "all resources from this view "
                                + "(by dropping on view content) " + "or to"
                                + " select all resources from "
                                + "this view (by dropping on selection).</p>",
                        false));
    }
}