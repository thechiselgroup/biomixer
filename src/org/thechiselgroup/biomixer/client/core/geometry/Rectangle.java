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
package org.thechiselgroup.biomixer.client.core.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.user.client.ui.Widget;

public final class Rectangle implements Size {

    public static Rectangle fromWidget(Widget widget) {
        int x = widget.getAbsoluteLeft();
        int y = widget.getAbsoluteTop();
        int height = widget.getOffsetHeight();
        int width = widget.getOffsetWidth();

        return new Rectangle(x, y, width, height);
    }

    // TODO move to library
    private static int[] toArray(Collection<Integer> set) {
        int[] result = new int[set.size()];
        int i = 0;
        for (Integer x : set) {
            result[i++] = x.intValue();
        }
        return result;
    }

    private final int height;

    private final int width;

    private final int x;

    private final int y;

    public Rectangle(int x, int y, int width, int height) {
        assert width >= 0;
        assert height >= 0;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    private List<Rectangle> calculateGrid(int[] xCoords, int[] yCoords) {
        List<Rectangle> grid = new ArrayList<Rectangle>();
        for (int i = 0; i < yCoords.length - 1; i++) {
            for (int j = 0; j < xCoords.length - 1; j++) {
                int x = xCoords[j];
                int y = yCoords[i];
                int width = xCoords[j + 1] - x;
                int height = yCoords[i + 1] - y;

                grid.add(new Rectangle(x, y, width, height));
            }
        }
        return grid;
    }

    /**
     * Returns the rectangles that represent the remainder of this rectangle
     * after removing the intersect of this rectangle with the union of the
     * other rectangles. This rectangle remains unchanged.
     */
    public List<Rectangle> calculateRemainder(List<Rectangle> rectangles) {
        int[] xCoords = calculateXCoordinates(rectangles);
        int[] yCoords = calculateYCoordinates(rectangles);

        List<Rectangle> grid = calculateGrid(xCoords, yCoords);

        removeHiddenAreas(rectangles, grid);

        return grid;
    }

    /**
     * calculates the set of x coordinates.
     */
    private int[] calculateXCoordinates(List<Rectangle> overlayingAreas) {
        SortedSet<Integer> xCoordinates = new TreeSet<Integer>();

        xCoordinates.add(Integer.valueOf(x));
        xCoordinates.add(Integer.valueOf(x + width));

        for (Rectangle r : overlayingAreas) {
            if (r.x > x && r.x < x + width) {
                xCoordinates.add(r.x);
            }

            if (r.x + r.width > x && r.x + r.width < x + width) {
                xCoordinates.add(r.x + r.width);
            }
        }

        return toArray(xCoordinates);
    }

    private int[] calculateYCoordinates(List<Rectangle> highlightedAreas) {
        SortedSet<Integer> yCoordinates = new TreeSet<Integer>();
        yCoordinates.add(Integer.valueOf(y));
        yCoordinates.add(Integer.valueOf(y + height));
        for (Rectangle r : highlightedAreas) {
            if (r.y > y && r.y < y + height) {
                yCoordinates.add(r.y);
            }

            if (r.y + r.height > y && r.y + r.height < y + height) {
                yCoordinates.add(r.y + r.height);
            }
        }
        return toArray(yCoordinates);
    }

    /**
     * Borders contain x, y as well.
     */
    public boolean contains(int x, int y) {
        return (this.x <= x) && (x < this.x + this.width) && (this.y <= y)
                && (y < this.y + this.height);
    }

    public boolean contains(Rectangle r) {
        assert r != null;

        if (this == r) {
            return true;
        }

        /*
         * all four corners of r have to be inside this rectangle --> this can
         * be checked by checking the two x and the two y numbers
         */
        if (!isValidX(r.x)) {
            return false;
        }

        if (!isValidX(r.x + r.width)) {
            return false;
        }

        if (!isValidY(r.y)) {
            return false;
        }

        if (!isValidY(r.y + r.height)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Rectangle other = (Rectangle) obj;
        if (height != other.height) {
            return false;
        }
        if (width != other.width) {
            return false;
        }
        if (x != other.x) {
            return false;
        }
        if (y != other.y) {
            return false;
        }
        return true;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + height;
        result = prime * result + width;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    public Rectangle intersection(Rectangle r) {
        assert intersects(r);

        int x1 = Math.max(this.x, r.x);
        int y1 = Math.max(this.y, r.y);

        int x2 = Math.min(this.x + this.width, r.x + r.width);
        int y2 = Math.min(this.y + this.height, r.y + r.height);

        int width = x2 - x1;
        int height = y2 - y1;

        return new Rectangle(x1, y1, width, height);
    }

    public boolean intersects(Rectangle r) {
        assert r != null;

        if (this == r) {
            return true;
        }
        return ((x + width >= r.x) && (y + height >= r.y)
                && (r.x + r.width >= x) && (r.y + r.height >= y));
    }

    private boolean isValidX(final int x) {
        return (x >= this.x) && (x <= this.x + this.width);
    }

    private boolean isValidY(final int y) {
        return (y >= this.y) && (y <= this.y + this.height);
    }

    /**
     * Returns a new Rectangle based on this one with new x, y.
     */
    public Rectangle move(int x, int y) {
        return new Rectangle(x, y, this.width, this.height);
    }

    private void removeHiddenAreas(List<Rectangle> overlayingRectangles,
            List<Rectangle> grid) {

        gridLoop: for (Iterator<Rectangle> it = grid.iterator(); it.hasNext();) {
            Rectangle r = it.next();
            for (Rectangle highlighted : overlayingRectangles) {
                if (highlighted.contains(r)) {
                    it.remove();
                    continue gridLoop;
                }
            }

        }
    }

    /**
     * Returns a new Rectangle based on this one with new width, height.
     */
    public Rectangle resize(int width, int height) {
        return new Rectangle(this.x, this.y, width, height);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + width + "," + height + ")";
    }
}
