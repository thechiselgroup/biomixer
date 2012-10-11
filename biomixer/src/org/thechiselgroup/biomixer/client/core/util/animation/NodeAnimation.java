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
package org.thechiselgroup.biomixer.client.core.util.animation;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimatable;

public abstract class NodeAnimation implements Animation {

    private final LayoutNodeAnimatable animatable;

    private LayoutNode node;

    public NodeAnimation(LayoutNodeAnimatable animatable) {
        this.animatable = animatable;
        this.node = animatable.getLayoutNode();
    }

    @Override
    public void cancel() {
        /*
         * By default the node moves back to its original position if the
         * animation is cancelled. However, we want it to hold at its current
         * position.
         */
        double cancelledX = node.getX();
        double cancelledY = node.getY();

        doCancel();

        node.setPosition(cancelledX, cancelledY);
    }

    protected abstract void doCancel();

    protected abstract void doRun(int duration);

    protected void doUpdate(double progress) {
        animatable.update(progress);
    }

    @Override
    public void onUpdate(double progress) {
        doUpdate(progress);
    }

    @Override
    public void run(int duration) {
        doRun(duration);
    }

}
