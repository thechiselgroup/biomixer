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
package org.thechiselgroup.biomixer.client.dnd.windows;

import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.util.math.MathUtils;

import com.allen_sauer.gwt.dnd.client.AbstractDragController;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.Location;
import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class WindowDragController extends AbstractDragController {

    protected final CommandManager commandManager;

    private final WindowManager controller;

    private int desktopHeight;

    private int desktopOffsetX;

    private int desktopOffsetY;

    private int desktopWidth;

    protected WindowPanel windowPanel = null;

    public WindowDragController(WindowManager controller,
            CommandManager commandManager) {

        super(controller.getBoundaryPanel());

        this.controller = controller;
        this.commandManager = commandManager;

        setBehaviorConstrainedToBoundaryPanel(true);
        setBehaviorMultipleSelection(false);
    }

    protected void bringToFront(WindowPanel window) {
        controller.bringToFront(window);
    }

    @Override
    public final void dragMove() {
        int rectrictedDesiredX = MathUtils.restrictToInterval(
                context.desiredDraggableX - desktopOffsetX, 0, desktopWidth
                        - getDraggable().getOffsetWidth());
        int restrictedDesiredY = MathUtils.restrictToInterval(
                context.desiredDraggableY - desktopOffsetY, 0, desktopHeight
                        - getDraggable().getOffsetHeight());

        dragMove(rectrictedDesiredX, restrictedDesiredY);
    }

    protected abstract void dragMove(int desiredDraggableX,
            int desiredDraggableY);

    @Override
    public void dragStart() {
        super.dragStart();

        AbsolutePanel desktop = context.boundaryPanel;
        Element desktopElement = desktop.getElement();
        desktopWidth = DOMUtil.getClientWidth(desktopElement);
        desktopHeight = DOMUtil.getClientHeight(desktopElement);

        Location desktopLocation = new WidgetLocation(desktop, null);
        desktopOffsetX = desktopLocation.getLeft()
                + DOMUtil.getBorderLeft(desktopElement);
        desktopOffsetY = desktopLocation.getTop()
                + DOMUtil.getBorderTop(desktopElement);

        getWindowPanelFromDraggable();
        bringToFront(windowPanel);
    }

    protected int getDesktopOffsetX() {
        return desktopOffsetX;
    }

    protected int getDesktopOffsetY() {
        return desktopOffsetY;
    }

    protected Widget getDraggable() {
        return context.draggable;
    }

    private void getWindowPanelFromDraggable() {
        Widget draggable = getDraggable();
        while ((draggable != null) && !(draggable instanceof WindowPanel)) {
            draggable = draggable.getParent();
        }
        windowPanel = (WindowPanel) draggable;
    }

}