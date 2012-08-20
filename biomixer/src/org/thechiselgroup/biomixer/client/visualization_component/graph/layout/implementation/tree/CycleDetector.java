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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Finds nodes which are part of a cycle in a graph. Does this by locating
 * strongly connected components in the graph using Tarjan's algorithm.
 * 
 * See
 * <code>http://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm</code>
 * 
 * @author drusk
 * 
 */
public class CycleDetector {

    /**
     * A vertex abstraction which contains data and operations specific to
     * Tarjan's algorithm.
     * 
     * @author drusk
     * 
     */
    private class Vertex {

        private LayoutNode layoutNode;

        private int index = -1;

        private int lowLink = -1;

        public Vertex(LayoutNode layoutNode) {
            this.layoutNode = layoutNode;
        }

        public int getIndex() {
            return index;
        }

        public LayoutNode getLayoutNode() {
            return layoutNode;
        }

        /**
         * 
         * @return the smallest index of a vertex reachable from this vertex.
         */
        public int getLowLink() {
            return lowLink;
        }

        public boolean isUnprocessed() {
            return index == -1;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        /**
         * Sets the smallest vertex index which is currently known to be
         * reachable from this vertex.
         * 
         */
        public void setLowLink(int lowLink) {
            this.lowLink = lowLink;
        }

    }

    private boolean resultCached = false;

    private int index = 0;

    private Stack<Vertex> visitedVertices = new Stack<Vertex>();

    private List<Set<LayoutNode>> stronglyConnectedComponents = new ArrayList<Set<LayoutNode>>();

    private Map<LayoutNode, Vertex> vertexRepresentations = new HashMap<LayoutNode, Vertex>();

    private final LayoutGraph graph;

    /**
     * Creates a new CycleDetector.
     * 
     * @param graph
     *            the graph to operate on.
     */
    public CycleDetector(LayoutGraph graph) {
        this.graph = graph;
    }

    /**
     * Finds strongly connected components in a graph. These are sets of nodes
     * in which every node can be reached from every other node. In a directed
     * acyclic graph each node will be in its own strongly connected component.
     * If there are cycles, there will be components with multiple nodes.
     * 
     * @param graph
     *            the graph whose nodes will be searched
     * @return a list of strongly connected components.
     */
    public List<Set<LayoutNode>> getStronglyConnectedComponents() {
        if (resultCached) {
            return stronglyConnectedComponents;
        }

        /* Maintaining state between calls to this method. */
        resultCached = true;

        /*
         * Initialize the abstract vertex representations of the nodes.
         */
        for (LayoutNode node : graph.getAllNodes()) {
            vertexRepresentations.put(node, new Vertex(node));
        }

        for (Vertex vertex : vertexRepresentations.values()) {
            if (vertex.isUnprocessed()) {
                process(vertex);
            }
        }
        return stronglyConnectedComponents;
    }

    /**
     * Detects whether there are cycles in the graph. If there are cycles, there
     * will be strongly connected components with multiple nodes.
     * 
     * @return <code>true</code> if there are cycles, <code>false</code>
     *         otherwise
     */
    public boolean hasCycles() {
        getStronglyConnectedComponents();
        return stronglyConnectedComponents.size() != graph.getAllNodes().size();
    }

    /**
     * Performs a recursive search for a strongly connected component.
     * 
     * @param vertex
     *            the vertex to start from
     */
    private void process(Vertex vertex) {
        vertex.setIndex(index);
        vertex.setLowLink(index);
        index++;
        visitedVertices.push(vertex);

        for (LayoutArc arc : vertex.getLayoutNode().getConnectedArcs()) {
            Vertex sourceVertex = vertexRepresentations
                    .get(arc.getSourceNode());

            if (vertex.equals(sourceVertex)) {
                Vertex targetVertex = vertexRepresentations.get(arc
                        .getTargetNode());

                if (targetVertex.isUnprocessed()) {
                    /* "Successor" node has not yet been visited. Recurse on it. */
                    process(targetVertex);
                    vertex.setLowLink(Math.min(vertex.getLowLink(),
                            targetVertex.getLowLink()));
                } else if (visitedVertices.contains(targetVertex)) {
                    /*
                     * "Successor" is in stack and therefore in the current
                     * strongly connected component.
                     */
                    vertex.setLowLink(Math.min(vertex.getLowLink(),
                            targetVertex.getIndex()));
                }
            }
        }

        /*
         * If the current vertex is a "root" node, pop the stack and generate a
         * strongly connected component.
         */
        if (vertex.getLowLink() == vertex.getIndex()) {
            /* Start a new strongly connected component */
            Set<LayoutNode> stronglyConnectedComponent = new HashSet<LayoutNode>();

            boolean shouldContinue = true;
            while (shouldContinue) {
                Vertex poppedVertex = visitedVertices.pop();
                if (poppedVertex.equals(vertex)) {
                    shouldContinue = false;
                }
                stronglyConnectedComponent.add(poppedVertex.getLayoutNode());
            }

            stronglyConnectedComponents.add(stronglyConnectedComponent);
        }

    }

    /**
     * Call this to uncache results from a previous run and make sure the
     * algorithm will run from scratch.
     */
    public void resetState() {
        index = 0;
        visitedVertices = new Stack<Vertex>();
        stronglyConnectedComponents = new ArrayList<Set<LayoutNode>>();
        vertexRepresentations = new HashMap<LayoutNode, Vertex>();
        resultCached = false;
    }

}
