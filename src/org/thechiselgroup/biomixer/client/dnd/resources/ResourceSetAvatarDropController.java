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

import org.thechiselgroup.choosel.core.client.command.CommandManager;
import org.thechiselgroup.choosel.core.client.command.UndoableCommand;
import org.thechiselgroup.choosel.core.client.geometry.Point;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.choosel.core.client.ui.CSS;
import org.thechiselgroup.choosel.core.client.ui.popup.DefaultDelayedPopupManager;
import org.thechiselgroup.choosel.core.client.ui.popup.DelayedPopupManager;
import org.thechiselgroup.choosel.core.client.ui.popup.Popup;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupFactory;
import org.thechiselgroup.choosel.core.client.util.HasDescription;
import org.thechiselgroup.choosel.core.client.visualization.View;
import org.thechiselgroup.choosel.core.client.visualization.ViewAccessor;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualizationModel;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class ResourceSetAvatarDropController extends SimpleDropController {

    private static final int MAX_POPUP_WIDTH = 250;

    private static final int POPUP_HIDE_DELAY = 200;

    private static final int POPUP_SHOW_DELAY = 800;

    private static final int POPUP_X_OFFSET = 15;

    private static final int POPUP_Y_OFFSET = 20;

    private final DropTargetCapabilityChecker capabilityChecker;

    private ResourceSetAvatarDropCommandFactory commandFactory;

    private CommandManager commandManager;

    private UndoableCommand executedCommand;

    private DelayedPopupManager popup = null;

    private ViewAccessor viewAccessor;

    private PopupFactory popupFactory;

    public ResourceSetAvatarDropController(Widget dropTarget,
            ResourceSetAvatarDropCommandFactory commandFactory,
            CommandManager commandManager, ViewAccessor viewAccessor,
            DropTargetCapabilityChecker capabilityChecker,
            PopupFactory popupFactory) {

        super(dropTarget);

        this.commandFactory = commandFactory;
        this.commandManager = commandManager;
        this.viewAccessor = viewAccessor;
        this.capabilityChecker = capabilityChecker;
        this.popupFactory = popupFactory;
    }

    // TODO prevent drag source self-drop
    public boolean canDrop(DragContext context) {
        if (!(context.draggable instanceof ResourceSetAvatar)) {
            return false;
        }

        ResourceSetAvatar avatar = getAvatar(context);

        /*
         * TODO we need the automatic visualization algorithm that takes the
         * current configuration and the new data and gives us a ranked list of
         * possible configurations. If that list is empty, we cannot drop.
         */

        return isValidDrop(avatar) && commandFactory.canDrop(avatar);
    }

    private UndoableCommand createCommand(final DragContext context) {
        return commandFactory.createCommand(getAvatar(context));
    }

    // protected for testing only
    // TODO move to factory
    // TODO refactor
    protected DelayedPopupManager createPopup(final DragContext context,
            String message) {

        Popup popup = popupFactory.createPopup();

        DelayedPopupManager popupManager = new DefaultDelayedPopupManager(
                POPUP_SHOW_DELAY, POPUP_HIDE_DELAY, popup) {

            @Override
            protected void updatePosition() {
                getPopup().setLocation(
                        new Point(context.mouseX + POPUP_X_OFFSET,
                                context.mouseY + POPUP_Y_OFFSET));
            }
        };

        HTML widget = new HTML(message);
        CSS.setMaxWidth(widget, MAX_POPUP_WIDTH);
        popupManager.getPopup().setContentWidget(widget);

        return popupManager;
    }

    private void executeCommand(UndoableCommand command) {
        command.execute();
        executedCommand = command;
    }

    private ResourceSetAvatar getAvatar(DragContext context) {
        return (ResourceSetAvatar) context.draggable;
    }

    private boolean hasCommandBeenExecuted() {
        return executedCommand != null;
    }

    private void hidePopup() {
        if (popup != null) {
            popup.hideDelayed();
            popup = null;
        }
    }

    private boolean isValidDrop(ResourceSetAvatar avatar) {
        View view = viewAccessor.findView(getDropTarget());
        assert view != null;

        VisualizationModel model = view.getModel();
        assert model != null;

        return capabilityChecker.isValidDrop(model.getSlots(),
                avatar.getResourceSet());
    }

    // TODO support dragging multiple widgets?
    @Override
    public void onDrop(DragContext context) {
        // remove popup even if error occurs
        try {
            if (canDrop(context) || hasCommandBeenExecuted()) {
                if (!hasCommandBeenExecuted()) {
                    executeCommand(createCommand(context));
                }

                // add the command to the command stack, and make
                // the manager fire an event.
                commandManager.execute(executedCommand);
                executedCommand = null; // prevent undo from later onLeave
            }
        } finally {
            hidePopup();
            super.onDrop(context);
        }
    }

    @Override
    public void onEnter(final DragContext context) {
        // TODO support dragging multiple widgets?
        super.onEnter(context);

        if (canDrop(context)) {
            UndoableCommand command = createCommand(context);

            if (command instanceof HasDescription) {
                showPopup(context, ((HasDescription) command).getDescription());
            }

            executeCommand(command);
        }
    }

    // TODO support dragging multiple widgets?
    @Override
    public void onLeave(DragContext context) {
        // remove popup even if command execution fails
        try {
            undoCommand();
        } finally {
            hidePopup();
            super.onLeave(context);
        }
    }

    // TODO refactor
    private void showPopup(final DragContext context, String message) {
        popup = createPopup(context, message);
        popup.showDelayed();
    }

    private void undoCommand() {
        if (hasCommandBeenExecuted()) {
            executedCommand.undo();
            executedCommand = null;
        }
    }
}