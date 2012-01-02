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

import org.thechiselgroup.choosel.core.client.command.CommandManager;
import org.thechiselgroup.choosel.core.client.geometry.Point;

import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.user.client.ui.AbsolutePanel;

public abstract class AbstractWindowManager implements WindowManager {

    protected final AbsolutePanel boundaryPanel;

    protected final WindowMoveController moveController;

    protected final WindowResizeController resizeDragController;

    public AbstractWindowManager(AbsolutePanel boundaryPanel,
            CommandManager commandManager) {

        assert boundaryPanel != null;

        this.boundaryPanel = boundaryPanel;

        moveController = new WindowMoveController(this, commandManager);
        resizeDragController = new WindowResizeController(this, commandManager);
    }

    @Override
    public void bringToFront(WindowPanel window) {
        // stub, can be overwritten by subclasses
    }

    @Override
    public AbsolutePanel getBoundaryPanel() {
        return boundaryPanel;
    }

    @Override
    public Point getLocation(WindowPanel window) {
        // TODO inline calculations
        WidgetLocation location = new WidgetLocation(window, boundaryPanel);
        return new Point(location.getLeft(), location.getTop());
    }

    @Override
    public WindowMoveController getMoveDragController() {
        return moveController;
    }

    @Override
    public WindowResizeController getResizeDragController() {
        return resizeDragController;
    }

    @Override
    public void setLocation(WindowPanel window, int x, int y) {
        // TODO inline calculations
        boundaryPanel.setWidgetPosition(window, x, y);

        assert x == new WidgetLocation(window, boundaryPanel).getLeft();
        assert y == new WidgetLocation(window, boundaryPanel).getTop();
    }

}