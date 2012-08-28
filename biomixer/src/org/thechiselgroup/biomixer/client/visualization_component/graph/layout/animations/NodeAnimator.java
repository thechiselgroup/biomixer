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

import java.util.HashMap;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

import com.google.gwt.animation.client.Animation;

/**
 * Manages the animations of {@link LayoutNode}s.
 * 
 * @author drusk
 * 
 */
public class NodeAnimator {

    private Map<LayoutNode, Animation> currentAnimations = new HashMap<LayoutNode, Animation>();

    private void animateNodeTo(final LayoutNode node, double x, double y,
            int duration) {
        // 1. If the node has an animation running, cancel it.
        if (currentAnimations.containsKey(node)) {
            currentAnimations.get(node).cancel();
        }

        // 2. Create the new requested animation
        final LayoutNodeAnimation layoutNodeAnimation = new LayoutNodeAnimation(
                node, x, y);
        Animation animation = new Animation() {

            @Override
            public void cancel() {
                /*
                 * By default the node moves back to its original position if
                 * the animation is cancelled. However, we want it to hold at
                 * its current position.
                 */
                double cancelledX = node.getX();
                double cancelledY = node.getY();

                super.cancel();

                node.setPosition(cancelledX, cancelledY);
            }

            @Override
            protected void onUpdate(double progress) {
                layoutNodeAnimation.update(progress);
            }

        };

        // 3. Store animation for future reference
        currentAnimations.put(node, animation);

        // 4. Run it
        animation.run(duration);
    }

    /**
     * Animates the movement of a specified node.
     * 
     * @param node
     *            the node to be animated
     * @param location
     *            the position to which the animation should bring the node
     * @param duration
     *            how long it should take for the node to reach the end location
     *            (in milliseconds)
     */
    public void animateNodeTo(LayoutNode node, Point location, int duration) {
        animateNodeTo(node, location.getX(), location.getY(), duration);
    }

    /**
     * Animates the movement of a specified node.
     * 
     * @param node
     *            the node to be animated
     * @param location
     *            the position to which the animation should bring the node
     * @param duration
     *            how long it should take for the node to reach the end location
     *            (in milliseconds)
     */
    public void animateNodeTo(LayoutNode node, PointDouble location,
            int duration) {
        animateNodeTo(node, location.getX(), location.getY(), duration);
    }

}
