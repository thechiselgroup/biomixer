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

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Calculates forces as if the nodes and arcs were part of a physical system.
 * The arcs are modelled as springs using Hooke's law and nodes are modelled as
 * electrons using Coulomb's law.
 * 
 * @author drusk
 * 
 */
public class SpringAndElectronForceCalculator extends AbstractForceCalculator {

    private final double springConstant;

    private final double chargeConstant;

    public SpringAndElectronForceCalculator(double springConstant,
            double chargeConstant) {
        this.springConstant = springConstant;
        this.chargeConstant = chargeConstant;
    }

    @Override
    public Vector2D getAttractionForce(LayoutNode currentNode,
            LayoutArc arc) {
        LayoutNode otherNode = arc.getSourceNode().equals(currentNode) ? arc
                .getTargetNode() : arc.getSourceNode();

        Vector2D distanceVector = getDistanceVector(currentNode, otherNode);
        return distanceVector.scaleBy(springConstant);
    }

    @Override
    public Vector2D getRepulsionForce(LayoutNode currentNode,
            LayoutNode otherNode) {
        double distanceBetween = getDistanceBetween(currentNode, otherNode);

        double repulsionMagnitude = chargeConstant
                / Math.pow(distanceBetween, 2);

        double repulsionAngle = getAngleBetween(currentNode, otherNode);

        return Vector2DFactory.createVectorFromPolarCoordinates(
                repulsionMagnitude, repulsionAngle);
    }

}
