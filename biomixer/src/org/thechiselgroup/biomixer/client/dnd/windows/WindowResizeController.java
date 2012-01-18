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

import java.util.HashMap;

import org.thechiselgroup.biomixer.client.core.command.CommandManager;

import com.google.gwt.user.client.ui.Widget;

public final class WindowResizeController extends WindowDragController {

    private static final String CSS_WINDOW_TRANSPARENT = "windowTransparent";

    // for test
    static void resize(int desiredDraggableX, int desiredDraggableY,
            int draggableLeft, int draggableTop, ResizeDirection direction,
            WindowController windowPanel) {

        int verticalDelta = 0;
        if (direction.isNorth()) {
            verticalDelta = draggableTop - desiredDraggableY;
        } else if (direction.isSouth()) {
            verticalDelta = desiredDraggableY - draggableTop;
        }

        int horizontalDelta = 0;
        if (direction.isWest()) {
            horizontalDelta = draggableLeft - desiredDraggableX;
        } else if (direction.isEast()) {
            horizontalDelta = desiredDraggableX - draggableLeft;
        }

        if (verticalDelta != 0 || horizontalDelta != 0) {
            int height = windowPanel.getHeight();
            int width = windowPanel.getWidth();

            int targetWidth = Math.max(0, width + horizontalDelta);
            int targetHeight = Math.max(0, height + verticalDelta);

            int horizontalMove = 0;
            if (direction.isWest()) {
                horizontalMove = width - targetWidth;
            }

            int verticalMove = 0;
            if (direction.isNorth()) {
                verticalMove = height - targetHeight;
            }

            windowPanel.resize(horizontalMove, verticalMove, targetWidth,
                    targetHeight);
        }
    }

    private final HashMap<Widget, ResizeDirection> directionMap = new HashMap<Widget, ResizeDirection>();

    public WindowResizeController(WindowManager controller,
            CommandManager commandManager) {
        super(controller, commandManager);
    }

    @Override
    public void dragEnd() {
        super.dragEnd();

        windowPanel.removeStyleName(CSS_WINDOW_TRANSPARENT);
    }

    @Override
    protected void dragMove(int rectrictedDesiredX, int restrictedDesiredY) {
        ResizeDirection direction = getDirection();

        int top = context.draggable.getAbsoluteTop()
                - getBoundaryPanel().getAbsoluteTop();

        int left = context.draggable.getAbsoluteLeft()
                - getBoundaryPanel().getAbsoluteLeft();

        resize(rectrictedDesiredX, restrictedDesiredY, left, top, direction,
                windowPanel);
    }

    @Override
    public void dragStart() {
        super.dragStart();

        windowPanel.addStyleName(CSS_WINDOW_TRANSPARENT);
    }

    private ResizeDirection getDirection() {
        return directionMap.get(getDraggable());
    }

    public void makeDraggable(Widget widget, ResizeDirection direction) {
        super.makeDraggable(widget);
        directionMap.put(widget, direction);
    }
}
