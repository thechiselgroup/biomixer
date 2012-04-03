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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimation;

/**
 * Wraps an ordinary {@link LayoutNode} to give its movements animations.
 * 
 * @author drusk
 * 
 */
public class AnimatableLayoutNode {

    private final LayoutNode wrappedNode;

    private final AnimationRunner animationRunner;

    private final int duration;

    public AnimatableLayoutNode(LayoutNode wrappedNode,
            AnimationRunner animationRunner, int duration) {
        this.wrappedNode = wrappedNode;
        this.animationRunner = animationRunner;
        this.duration = duration;
    }

    public void setPosition(double x, double y) {
        animationRunner.run(new LayoutNodeAnimation(wrappedNode, x, y),
                duration);
    }

    public void setPosition(PointDouble position) {
        setPosition(position.getX(), position.getY());
    }

    public void setX(double x) {
        animationRunner.run(
                new LayoutNodeAnimation(wrappedNode, x, wrappedNode.getY()),
                duration);
    }

    public void setY(double y) {
        animationRunner.run(
                new LayoutNodeAnimation(wrappedNode, wrappedNode.getX(), y),
                duration);
    }

}
