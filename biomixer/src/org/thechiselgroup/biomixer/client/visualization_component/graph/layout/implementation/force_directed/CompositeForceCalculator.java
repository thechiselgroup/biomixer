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
 * Calculates the net force resulting from the application of multiple
 * {@link ForceCalculator}s.
 * 
 * @author drusk
 * 
 */
public class CompositeForceCalculator implements ForceCalculator {

    private final ForceCalculator[] forceCalculators;

    public CompositeForceCalculator(ForceCalculator... forceCalculators) {
        this.forceCalculators = forceCalculators;
    }

    @Override
    public Vector2D getForce(LayoutNode currentNode, LayoutNode otherNode) {
        Vector2D compositeForce = new Vector2D(0, 0);
        for (ForceCalculator forceCalculator : forceCalculators) {
            compositeForce
                    .add(forceCalculator.getForce(currentNode, otherNode));
        }
        return compositeForce;
    }

}
