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

public class BoundsAwareAttractionCalculator extends BoundsAwareForceCalculator {

    public BoundsAwareAttractionCalculator(LayoutGraph graph) {
        super(graph);
    }

    @Override
    public Vector2D getForce(LayoutNode currentNode, LayoutNode otherNode) {
        if (!currentNode.isConnectedTo(otherNode)) {
            /*
             * If the nodes are not connected by an arc then there is no
             * attraction force.
             */
            return new Vector2D(0, 0);
        }
        double magnitude = Math.pow(getDistanceBetween(currentNode, otherNode),
                2) / getOptimalEdgeLength();

        /*
         * Note the force is directed towards the other node since it is an
         * attraction force on the current node.
         */
        return Vector2DFactory.createVectorFromPolarCoordinates(magnitude,
                getAngleBetween(currentNode, otherNode));
    }

}
