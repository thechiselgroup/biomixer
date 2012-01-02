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

import org.thechiselgroup.choosel.core.client.geometry.Point;
import org.thechiselgroup.choosel.core.client.geometry.Size;

public class PositionManager {

    private Size desktop;

    private final int horizontalSteps;

    private int invocationCounter = 0;

    private final int padding;

    private final int verticalSteps;

    public PositionManager(Size desktop, int horizontalSteps,
            int verticalSteps, int padding) {

        this.desktop = desktop;
        this.verticalSteps = verticalSteps;
        this.horizontalSteps = horizontalSteps;
        this.padding = padding;
    }

    private int calculate(int windowLength, int offsetLength, int steps) {
        int availableLength = offsetLength - windowLength - 2 * padding;
        int stepLength = availableLength / steps;
        return padding + stepLength * (invocationCounter % steps);
    }

    public Point getNextLocation(int windowWidth, int windowHeight) {
        int x = calculate(windowWidth, desktop.getWidth(), horizontalSteps);
        int y = calculate(windowHeight, desktop.getHeight(), verticalSteps);

        invocationCounter++;

        return new Point(x, y);
    }
}
