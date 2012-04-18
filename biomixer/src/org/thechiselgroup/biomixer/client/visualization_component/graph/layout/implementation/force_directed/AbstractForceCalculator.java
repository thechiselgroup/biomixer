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

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Methods that may be useful to multiple force calculator implementations.
 * 
 * @author drusk
 * 
 */
public abstract class AbstractForceCalculator implements ForceCalculator {

    protected double getAngleBetween(LayoutNode node1, LayoutNode node2) {
        return getDistanceVector(node1, node2).getDirection();
    }

    /**
     * 
     * @return the magnitude of the distance vector between the two nodes,
     *         buffering it to a minimum value of 1 if it is less than 1.
     */
    protected double getBufferedDistanceBetween(LayoutNode currentNode,
            LayoutNode otherNode) {
        /*
         * If distance is too close to 0 then division will produce an almost
         * infinite force. Therefore limit the distance to a min value of 1.
         */
        double interNodeDistance = getDistanceBetween(currentNode, otherNode);
        if (interNodeDistance < 1) {
            interNodeDistance = 1;
        }
        return interNodeDistance;
    }

    /**
     * See also getBufferedDistanceBetween(LayoutNode, LayoutNode)
     * 
     * @return the magnitude of the distance vector.
     */
    protected double getDistanceBetween(LayoutNode node1, LayoutNode node2) {
        return getDistanceVector(node1, node2).getMagnitude();
    }

    /**
     * Gets the distance vector between the centre points of the two specified
     * nodes.
     * 
     * @param source
     *            node at start of distance vector (vector points away from this
     *            node)
     * @param target
     *            node at end of distance vector (vector points towards this
     *            node)
     * @return distance vector
     */
    protected Vector2D getDistanceVector(LayoutNode source, LayoutNode target) {
        PointDouble sourceCentre = source.getCentre();
        PointDouble targetCentre = target.getCentre();
        return new Vector2D(targetCentre.getX() - sourceCentre.getX(),
                targetCentre.getY() - sourceCentre.getY());
    }

}
