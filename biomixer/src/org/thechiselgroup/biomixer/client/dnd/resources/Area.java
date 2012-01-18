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

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.Rectangle;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowPanel;

/**
 * Area for shade and drop target calculation.
 */
public class Area {

    // can be null, TODO introduce different area classes
    private ResourceSetAvatarDropController dropController;

    private Rectangle rectangle;

    private WindowPanel window;

    public Area(Rectangle r, WindowPanel window,
            ResourceSetAvatarDropController dropController) {

        assert r != null;
        assert window != null;

        this.rectangle = r;
        this.window = window;
        this.dropController = dropController;
    }

    private List<Rectangle> calculateHiddenParts(List<Area> windowAreas) {
        List<Rectangle> hiddenParts = new ArrayList<Rectangle>();
        for (Area windowArea : windowAreas) {
            if (isHiddenBy(windowArea)) {
                hiddenParts.add(getPartHiddenBy(windowArea));
            }
        }
        return hiddenParts;
    }

    public ResourceSetAvatarDropController getDropController() {
        return dropController;
    }

    private Rectangle getPartHiddenBy(Area windowArea) {
        return rectangle.intersection(windowArea.rectangle);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public List<Area> getVisibleParts(List<Area> windowAreas) {
        List<Rectangle> hiddenParts = calculateHiddenParts(windowAreas);

        List<Area> visibleAreas = new ArrayList<Area>();
        for (Rectangle remainingRectangle : rectangle
                .calculateRemainder(hiddenParts)) {
            visibleAreas.add(new Area(remainingRectangle, window,
                    dropController));
        }

        return visibleAreas;
    }

    private boolean isHiddenBy(Area windowArea) {
        return (window != windowArea.window)
                && (window.getZIndex() < windowArea.window.getZIndex())
                && (rectangle.intersects(windowArea.rectangle));
    }

    @Override
    public String toString() {
        return rectangle.toString();
    }

}