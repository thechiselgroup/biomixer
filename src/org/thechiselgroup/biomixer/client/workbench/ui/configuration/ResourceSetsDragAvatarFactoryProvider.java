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

import static org.thechiselgroup.choosel.core.client.configuration.ChooselInjectionConstants.AVATAR_FACTORY_SET;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.dnd.resources.DraggableResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.DropTargetResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.HighlightingDraggableResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDragController;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDropTargetManager;
import org.thechiselgroup.choosel.core.client.command.CommandManager;
import org.thechiselgroup.choosel.core.client.resources.action.RemoveSetAction;
import org.thechiselgroup.choosel.core.client.resources.ui.HighlightingResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarFactoryProvider;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.choosel.core.client.resources.ui.UpdateResourceSetAvatarLabelFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.popup.PopupResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.popup.PopupResourceSetAvatarFactory.Action;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupManagerFactory;
import org.thechiselgroup.choosel.core.client.visualization.ViewAccessor;
import org.thechiselgroup.choosel.core.client.visualization.model.extensions.HighlightingModel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ResourceSetsDragAvatarFactoryProvider implements
        ResourceSetAvatarFactoryProvider {

    private CommandManager commandManager;

    private final ResourceSetAvatarDragController dragController;

    private final ResourceSetAvatarDropTargetManager dropTargetManager;

    private final HighlightingModel hoverModel;

    private final PopupManagerFactory popupManagerFactory;

    private final ViewAccessor viewAccessor;

    @Inject
    public ResourceSetsDragAvatarFactoryProvider(
            ResourceSetAvatarDragController dragController,
            HighlightingModel hoverModel,
            @Named(AVATAR_FACTORY_SET) ResourceSetAvatarDropTargetManager dropTargetManager,
            ViewAccessor viewAccessor, PopupManagerFactory popupManagerFactory,
            CommandManager commandManager) {

        this.dragController = dragController;
        this.hoverModel = hoverModel;
        this.dropTargetManager = dropTargetManager;
        this.viewAccessor = viewAccessor;
        this.popupManagerFactory = popupManagerFactory;
        this.commandManager = commandManager;

    }

    @Override
    public ResourceSetAvatarFactory get() {
        ResourceSetAvatarFactory defaultFactory = new DraggableResourceSetAvatarFactory(
                "avatar-resourceSet", ResourceSetAvatarType.SET, dragController);

        ResourceSetAvatarFactory updateFactory = new UpdateResourceSetAvatarLabelFactory(
                defaultFactory);

        ResourceSetAvatarFactory dropTargetFactory = new DropTargetResourceSetAvatarFactory(
                updateFactory, dropTargetManager);

        HighlightingResourceSetAvatarFactory highlightingFactory = new HighlightingDraggableResourceSetAvatarFactory(
                dropTargetFactory, hoverModel, dragController);

        List<Action> actions = new ArrayList<Action>();
        actions.add(new RemoveSetAction(commandManager));

        return new PopupResourceSetAvatarFactory(highlightingFactory,
                viewAccessor, popupManagerFactory, actions, "User-defined set",
                "<p><b>Rename</b> by clicking on the label "
                        + "at the top of this popup and "
                        + "changing the text.</p>"
                        + "<p><b>Drag</b> to add to other views as a set "
                        + "(by dropping on view content) "
                        + "or as single elements " + "(by dropping on 'All'), "
                        + "to merge with other user-defined sets "
                        + "(by dropping on these sets)"
                        + " or to select resources "
                        + "(by dropping on a selection).</p>", true);
    }
}