/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations;

import org.thechiselgroup.biomixer.client.core.util.animation.Animation;
import org.thechiselgroup.biomixer.client.core.util.animation.Interpolations;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Animates the movement of a node.
 * 
 * @author drusk
 * 
 */
public class LayoutNodeAnimation implements Animation {

    private final LayoutNode node;

    private final double startX;

    private final double startY;

    private final double destinationX;

    private final double destinationY;

    private Interpolations interpolator = new Interpolations();

    // TODO easing function instead of plain interpolator

    public LayoutNodeAnimation(LayoutNode node, double destinationX,
            double destinationY) {
        this.node = node;
        this.startX = node.getX();
        this.startY = node.getY();
        this.destinationX = destinationX;
        this.destinationY = destinationY;
    }

    @Override
    public void update(double progress) {
        double currentX = interpolator.interpolate(progress, startX,
                destinationX);
        double currentY = interpolator.interpolate(progress, startY,
                destinationY);
        node.setPosition(currentX, currentY);
    }

}
