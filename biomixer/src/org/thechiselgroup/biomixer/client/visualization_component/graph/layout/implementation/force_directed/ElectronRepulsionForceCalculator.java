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
 * Calculates forces as if the nodes were electrons pushing each other apart
 * according to Coulomb's Law.
 * 
 * @author drusk
 * 
 */
public class ElectronRepulsionForceCalculator extends AbstractForceCalculator {

    private final double chargeConstant;

    public ElectronRepulsionForceCalculator(double chargeConstant) {
        this.chargeConstant = chargeConstant;
    }

    @Override
    public Vector2D getForce(LayoutNode currentNode, LayoutNode otherNode) {
        double distanceBetween = getDistanceBetween(currentNode, otherNode);

        double repulsionMagnitude = chargeConstant
                / Math.pow(distanceBetween, 2);

        double repulsionAngle = getAngleBetween(currentNode, otherNode);

        return Vector2DFactory.createVectorFromPolarCoordinates(
                repulsionMagnitude, repulsionAngle);
    }

}
