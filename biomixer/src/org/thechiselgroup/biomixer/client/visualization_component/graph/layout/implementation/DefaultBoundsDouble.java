/*******************************************************************************
 * Copyright 2012 Lars Grammel, David Rusk 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;

public class DefaultBoundsDouble implements BoundsDouble {

    private double width;

    private double height;

    private double leftX;

    private double topY;

    private double rightX;

    private double bottomY;

    public DefaultBoundsDouble(double leftX, double topY, double width,
            double height) {

        this.width = width;
        this.height = height;
        this.leftX = leftX;
        this.rightX = leftX + width;
        this.topY = topY;
        this.bottomY = topY + height;
    }

    @Override
    public double getArea() {
        return width * height;
    }

    @Override
    public double getBottomY() {
        return bottomY;
    }

    @Override
    public PointDouble getCentre() {
        return new PointDouble(getLeftX() + getWidth() / 2, getTopY()
                + getHeight() / 2);
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getLeftX() {
        return leftX;
    }

    @Override
    public double getRightX() {
        return rightX;
    }

    @Override
    public double getTopY() {
        return topY;
    }

    @Override
    public double getWidth() {
        return width;
    }

}
