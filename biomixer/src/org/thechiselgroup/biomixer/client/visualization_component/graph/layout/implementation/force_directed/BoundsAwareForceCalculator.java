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
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DirectedAcyclicGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DirectedAcyclicGraphBuilder;

public abstract class BoundsAwareForceCalculator extends
        AbstractForceCalculator {

    protected final LayoutGraph graph;

    private DirectedAcyclicGraphBuilder graphBuilder = new DirectedAcyclicGraphBuilder();

    public BoundsAwareForceCalculator(LayoutGraph graph) {
        this.graph = graph;
    }

    /**
     * Determines whether two layout nodes are part of the same
     * DirectedAcyclicGraph.
     */
    protected boolean areNodesInSameGraph(LayoutNode node1, LayoutNode node2) {
        DirectedAcyclicGraph node1Graph = null;
        DirectedAcyclicGraph node2Graph = null;
        for (DirectedAcyclicGraph directedAcyclicGraph : graphBuilder
                .getDirectedAcyclicGraphs(graph)) {
            if (directedAcyclicGraph.containsLayoutNode(node1)) {
                node1Graph = directedAcyclicGraph;
            }
            if (directedAcyclicGraph.containsLayoutNode(node2)) {
                node2Graph = directedAcyclicGraph;
            }
        }
        if (node1Graph == null || node2Graph == null) {
            return false;
        }
        return node1Graph.equals(node2Graph);
    }

    protected double getOptimalEdgeLength() {
        /*
         * k = C*sqrt(area/numberOfNodes). With C=1 the nodes tend to push each
         * other the the outermost edges of the graph. Therefore use a smaller
         * value to keep them further within bounds.
         */
        return 0.5 * getUnscaledOptimalEdgeLength();
    }

    private double getUnscaledOptimalEdgeLength() {
        /*
         * k = C*sqrt(area/numberOfNodes). C=1 for unscaled.
         */
        return Math.sqrt(graph.getBounds().getArea()
                / graph.getAllNodes().size());
    }

}
