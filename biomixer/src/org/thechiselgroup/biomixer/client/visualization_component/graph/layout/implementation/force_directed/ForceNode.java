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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * A node in the {@link ForceDirectedLayoutComputation}.
 * 
 * @author drusk
 * 
 */
public class ForceNode {

    /*
     * Position information is maintained only in the layout node.
     */
    private final LayoutNode layoutNode;

    private Vector2D velocity = new DefaultVector2D(0, 0);

    public ForceNode(LayoutNode layoutNode) {
        this.layoutNode = layoutNode;
    }

    /**
     * KE = 0.5mv^2. NOTE: All nodes assumed to have equal and constant mass,
     * therefore this just returns v^2.
     * 
     * @return kinetic energy of this node.
     */
    public double getKineticEnergy() {
        return Math.pow(velocity.getMagnitude(), 2);
    }

    public LayoutNode getLayoutNode() {
        return layoutNode;
    }

    /**
     * @return this node's current velocity.
     */
    public Vector2D getVelocity() {
        return velocity;
    }

    /**
     * Calculates the node's new position after a specified period of time. This
     * is determined using the node's velocity.
     * 
     * @param timeStep
     *            the node's position is updated as if it had been travelling at
     *            its current velocity for this amount of time
     */
    public void updatePosition(double timeStep) {
        Vector2D positionDelta = velocity.scaleBy(timeStep);
        layoutNode.setX(layoutNode.getX() + positionDelta.getXComponent());
        layoutNode.setY(layoutNode.getY() + positionDelta.getYComponent());
    }

    /**
     * Updates the node's velocity given the current forces applied to it.
     * 
     * @param force
     *            the net-force that was applied to this node in the current
     *            computation iteration
     * @param timeStep
     *            the time for which the force is applied.
     * @param damping
     *            causes the node's velocity to gradually decrease over time.
     */
    public void updateVelocity(Vector2D force, double timeStep, double damping) {
        velocity.add(force.scaleBy(timeStep)).scaleBy(damping);
    }

}
