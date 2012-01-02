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

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.choosel.core.client.command.CommandManager;
import org.thechiselgroup.choosel.core.client.resources.action.RemoveSelectionSetAction;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarFactoryProvider;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.choosel.core.client.resources.ui.UpdateResourceSetAvatarLabelFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.popup.PopupResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.popup.PopupResourceSetAvatarFactory.Action;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupManagerFactory;
import org.thechiselgroup.choosel.core.client.visualization.ViewAccessor;
import org.thechiselgroup.choosel.core.client.visualization.model.extensions.HighlightingModel;
import org.thechiselgroup.choosel.dnd.client.resources.DraggableResourceSetAvatarFactory;
import org.thechiselgroup.choosel.dnd.client.resources.HighlightingDraggableResourceSetAvatarFactory;
import org.thechiselgroup.choosel.dnd.client.resources.ResourceSetAvatarDragController;
import org.thechiselgroup.choosel.dnd.client.resources.SelectionResourceSetAvatarFactory;

import com.google.inject.Inject;

public class SelectionDragAvatarFactoryProvider implements
        ResourceSetAvatarFactoryProvider {

    private final CommandManager commandManager;

    private final ResourceSetAvatarDragController dragController;

    private final HighlightingModel hoverModel;

    private final PopupManagerFactory popupManagerFactory;

    private final ViewAccessor viewAccessor;

    @Inject
    public SelectionDragAvatarFactoryProvider(
            ResourceSetAvatarDragController dragController,
            HighlightingModel hoverModel, ViewAccessor viewAccessor,
            PopupManagerFactory popupManagerFactory,
            CommandManager commandManager) {

        this.dragController = dragController;
        this.hoverModel = hoverModel;
        this.viewAccessor = viewAccessor;
        this.popupManagerFactory = popupManagerFactory;
        this.commandManager = commandManager;
    }

    @Override
    public ResourceSetAvatarFactory get() {
        ResourceSetAvatarFactory defaultFactory = new DraggableResourceSetAvatarFactory(
                "avatar-selection", ResourceSetAvatarType.SELECTION,
                dragController);

        ResourceSetAvatarFactory updateFactory = new UpdateResourceSetAvatarLabelFactory(
                defaultFactory);

        ResourceSetAvatarFactory highlightingFactory = new HighlightingDraggableResourceSetAvatarFactory(
                updateFactory, hoverModel, dragController);

        ResourceSetAvatarFactory clickFactory = new SelectionResourceSetAvatarFactory(
                highlightingFactory, viewAccessor, commandManager);

        List<Action> actions = new ArrayList<Action>();
        actions.add(new RemoveSelectionSetAction(commandManager));
        // actions.add(new CreateSetAction(resourceSetFactory,
        // resourceSetLabelFactory, commandManager));

        return new PopupResourceSetAvatarFactory(clickFactory, viewAccessor,
                popupManagerFactory, actions, "View selection",
                "<p><b>Drag</b> this selection to create filtered views"
                        + " (by dropping on view content), to"
                        + " synchronize the selection in multiple views "
                        + "(by dropping on other selections) "
                        + "or to add resources"
                        + " to a different set or view "
                        + "(by dropping on 'All').</p>", true);
    }
}