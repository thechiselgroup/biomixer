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

public abstract class BoundsAwareForceCalculator extends
        AbstractForceCalculator {

    private final LayoutGraph graph;

    public BoundsAwareForceCalculator(LayoutGraph graph) {
        this.graph = graph;
    }

    protected double getOptimalEdgeLength() {
        /*
         * k = C*sqrt(area/numberOfNodes). With C=1 the nodes tend to push each
         * other the the outermost edges of the graph. Therefore use a smaller
         * value to keep them further within bounds.
         */
        return Math.sqrt(graph.getBounds().getArea()
                / graph.getAllNodes().size()) / 2;
    }

}
