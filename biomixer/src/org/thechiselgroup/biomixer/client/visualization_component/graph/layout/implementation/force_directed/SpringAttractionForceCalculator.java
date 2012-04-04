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
 * Calculates forces as if arcs where springs pulling nodes together according
 * to Hooke's Law.
 * 
 * @author drusk
 * 
 */
// XXX not currently in use.
public class SpringAttractionForceCalculator extends AbstractForceCalculator {

    private final double springConstant;

    public SpringAttractionForceCalculator(double springConstant) {
        this.springConstant = springConstant;
    }

    @Override
    public Vector2D getForce(LayoutNode currentNode, LayoutNode otherNode) {
        if (!currentNode.isConnectedTo(otherNode)) {
            /*
             * If the nodes are not connected by an arc then there is no spring
             * force.
             */
            return new Vector2D(0, 0);
        }
        Vector2D springForce = getDistanceVector(currentNode, otherNode)
                .scaleBy(springConstant);
        return springForce;
    }

}
