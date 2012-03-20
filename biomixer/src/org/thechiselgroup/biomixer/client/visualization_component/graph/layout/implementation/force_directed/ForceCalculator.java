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
 * Calculates forces applied to nodes in a force-directed layout.
 * 
 * @see {@link ForceDirectedLayoutComputation}.
 * @author drusk
 * 
 */
public interface ForceCalculator {

    /**
     * Calculates the attractive force applied to <code>currentNode</code> due
     * to <code>arc</code>.
     * 
     * @return two-dimensional force vector
     */
    Vector2D getAttractionForce(LayoutNode currentNode, LayoutArc arc);

    /**
     * Calculates the repulsive force applied to <code>currentNode</code> by the
     * presence of <code>otherNode</code>.
     * 
     * @return two-dimensional force vector
     */
    Vector2D getRepulsionForce(LayoutNode currentNode,
            LayoutNode otherNode);

}
