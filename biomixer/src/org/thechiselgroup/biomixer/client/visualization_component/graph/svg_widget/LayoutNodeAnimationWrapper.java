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

import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimation;

/**
 * Wraps an ordinary LayoutNode giving it animations for methods that cause
 * movement. This can allow layout algorithms to cause animations while normal
 * user movements of nodes just change the position normally, without an
 * animated effect.
 * 
 * @author drusk
 * 
 */
public class LayoutNodeAnimationWrapper implements LayoutNode {

    private final LayoutNode wrappedNode;

    private final AnimationRunner animationRunner;

    private final int duration;

    public LayoutNodeAnimationWrapper(LayoutNode wrappedNode,
            AnimationRunner animationRunner, int duration) {
        this.wrappedNode = wrappedNode;
        this.animationRunner = animationRunner;
        this.duration = duration;
    }

    @Override
    public SizeDouble getLabelSize() {
        return wrappedNode.getLabelSize();
    }

    @Override
    public double getLabelX() {
        return wrappedNode.getLabelX();
    }

    @Override
    public double getLabelY() {
        return wrappedNode.getLabelY();
    }

    @Override
    public SizeDouble getSize() {
        return wrappedNode.getSize();
    }

    @Override
    public LayoutNodeType getType() {
        return wrappedNode.getType();
    }

    public LayoutNode getWrappedNode() {
        return wrappedNode;
    }

    @Override
    public double getX() {
        return wrappedNode.getX();
    }

    @Override
    public double getY() {
        return wrappedNode.getY();
    }

    @Override
    public boolean hasLabel() {
        return wrappedNode.hasLabel();
    }

    @Override
    public boolean isAnchored() {
        return wrappedNode.isAnchored();
    }

    @Override
    public void setLabelPosition(double x, double y) {
        wrappedNode.setLabelPosition(x, y);
    }

    @Override
    public void setLabelX(double x) {
        wrappedNode.setLabelX(x);
    }

    @Override
    public void setLabelY(double y) {
        wrappedNode.setLabelY(y);
    }

    @Override
    public void setPosition(double x, double y) {
        animationRunner.run(new LayoutNodeAnimation(wrappedNode, x, y),
                duration);
    }

    @Override
    public void setX(double x) {
        animationRunner.run(
                new LayoutNodeAnimation(wrappedNode, x, wrappedNode.getY()),
                duration);
    }

    @Override
    public void setY(double y) {
        animationRunner.run(
                new LayoutNodeAnimation(wrappedNode, wrappedNode.getX(), y),
                duration);
    }

}
