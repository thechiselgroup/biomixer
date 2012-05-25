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

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Performs a modified version of Fruchterman and Reingold's attraction force
 * calculation. The ratio of number of arcs to number of nodes is taken into
 * account in order to prevent excessive clumping among highly interconnected
 * nodes.
 * 
 * @see BoundsAwareAttractionCalculator
 * 
 * @author drusk
 * 
 */
public class BoundsAwareRepulsionCalculator extends BoundsAwareForceCalculator {

    public BoundsAwareRepulsionCalculator(LayoutGraph graph) {
        super(graph);
    }

    @Override
    public Vector2D getForce(LayoutNode currentNode, LayoutNode otherNode) {
        /*
         * Note the force is directed towards the current node since it is a
         * repulsion force on the current node.
         */
        return Vector2DFactory.createVectorFromPolarCoordinates(
                getForceMagnitude(currentNode, otherNode),
                getAngleBetween(otherNode, currentNode));
    }

    private double getForceMagnitude(LayoutNode currentNode,
            LayoutNode otherNode) {

        double interNodeDistance = getBufferedDistanceBetween(currentNode,
                otherNode);
        double coefficient = getNumberOfArcsOverNumberOfNodes();
        if (coefficient == 0) {
            /*
             * Make sure there is some minimal force for the case where all
             * nodes are unconnected
             */
            coefficient = 0.25;
        }
        return Math.pow(coefficient * getOptimalEdgeLength(), 2)
                / interNodeDistance;
    }

}
