/*******************************************************************************
 * Copyright 2012 Lars Grammel, David Rusk 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout;

import java.util.List;

/**
 * <p>
 * Algorithm that computes the position of nodes and edges for a graph
 * structure.
 * </p>
 * <p>
 * This is a factory that creates layout computations. It exists because we need
 * to support continuous layouts. Implementations of LayoutAlgorithm should not
 * maintain state information about any specific ongoing computations.
 * </p>
 * 
 * @author Lars Grammel
 */
public interface LayoutAlgorithm {

    /**
     * Starts a layout computation on the given graph.
     * 
     * @param graph
     *            graph structure that will be laid out
     * 
     * @return computation that calculates the layout (for stopping, etc.)
     */
    LayoutComputation computeLayout(LayoutGraph graph);

    /**
     * Starts a layout computation on the given graph.
     * 
     * @param graph
     *            graph structure that will be laid out
     * 
     * @return computation that calculates the layout (for stopping, etc.)
     */
    LayoutComputation computeLayout(LayoutGraph graph,
            List<LayoutComputationFinishedHandler> handlers);

}