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

import java.util.List;

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
     * Calculates the force applied to <code>currentNode</code> by
     * <code>otherNode</code>.
     */
    Vector2D getForce(LayoutNode currentNode, LayoutNode otherNode);

    /**
     * Calculates the net force applied to <code>currentNode</code> by
     * <code>otherNodes</code>.
     */
    Vector2D getNetForce(LayoutNode currentNode, List<LayoutNode> otherNodes);

}
