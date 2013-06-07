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

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Stores a node structure where there are nodes which directed arcs between
 * them. Nodes may have more than one parent, and there may be more than one
 * node which has no parents.
 * 
 * @author drusk
 * 
 */
public class DirectedAcyclicGraph {

    /**
     * Nodes that have no parents.
     */
    private final List<DirectedAcyclicGraphNode> roots;

    /**
     * Provides quick lookup of nodes based on their max distance from any root
     */
    private Map<Integer, List<DirectedAcyclicGraphNode>> nodesByMaxDistanceFromARoot = new HashMap<Integer, List<DirectedAcyclicGraphNode>>();

    /**
     * 
     * @param roots
     *            nodes which have no parents. Cannot be an empty list.
     */
    public DirectedAcyclicGraph(List<DirectedAcyclicGraphNode> roots) {
        assert roots != null;
        assert !roots.isEmpty();

        this.roots = roots;
        initializeNodeDistanceMapping();
    }

    public boolean containsLayoutNode(LayoutNode node) {
        Set<DirectedAcyclicGraphNode> allNodes = getAllNodes();
        for (DirectedAcyclicGraphNode directedAcyclicGraphNode : allNodes) {
            if (directedAcyclicGraphNode.getLayoutNode().equals(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @return all nodes in the directed acycle graph
     */
    public Set<DirectedAcyclicGraphNode> getAllNodes() {
        Set<DirectedAcyclicGraphNode> allNodes = new HashSet<DirectedAcyclicGraphNode>();
        for (DirectedAcyclicGraphNode root : roots) {
            allNodes.add(root);
            allNodes.addAll(root.getDescendants());
        }
        return allNodes;
    }

    /**
     * 
     * @param dagNode
     *            the node to find the distance for
     * @return the maximum distance of <code>dagNode</code> from any of the
     *         roots
     */
    private int getMaxDistanceFromAnyRoot(DirectedAcyclicGraphNode dagNode) {
        int maxDepth = 0;
        for (DirectedAcyclicGraphNode root : roots) {
            int maxDepthFromRoot = root.getMaxDistance(dagNode);
            if (maxDepthFromRoot > maxDepth) {
                maxDepth = maxDepthFromRoot;
            }
        }
        return maxDepth;
    }

    /**
     * 
     * @param distance
     *            the max distance from any root
     * @return all nodes at the specified distance from a root
     */
    public List<DirectedAcyclicGraphNode> getNodesAtDistanceFromRoot(
            int distance) {
        assert distance >= 0;
        // This assertion was breaking on soem graphs, for linear and radial trees.
        // assert nodesByMaxDistanceFromARoot.containsKey(Integer
        // .valueOf(distance)) : "no nodes at distance=" + distance
        // + " from a root";
        return nodesByMaxDistanceFromARoot.get(Integer.valueOf(distance));
    }


    /**
     * 
     * @return the total number of nodes in the graph, including roots
     */
    public int getNumberOfNodes() {
        return getAllNodes().size();
    }

    /**
     * 
     * @return the number of nodes on the longest path from a root to leaf
     */
    public int getNumberOfNodesOnLongestPath() {
        int longestPath = 0;
        for (DirectedAcyclicGraphNode root : roots) {
            int numberOfNodes = root.getMaxLengthToEndOfPath() + 1;
            if (numberOfNodes > longestPath) {
                longestPath = numberOfNodes;
            }
        }
        return longestPath;
    }

    /**
     * 
     * @return nodes which have no parents
     */
    public List<DirectedAcyclicGraphNode> getRoots() {
        return roots;
    }

    private void initializeNodeDistanceMapping() {
        Set<DirectedAcyclicGraphNode> allNodes = getAllNodes();
        for (DirectedAcyclicGraphNode dagNode : allNodes) {
            Integer maxDepth = Integer
                    .valueOf(getMaxDistanceFromAnyRoot(dagNode));
            if (!nodesByMaxDistanceFromARoot.containsKey(maxDepth)) {
                nodesByMaxDistanceFromARoot.put(maxDepth,
                        new ArrayList<DirectedAcyclicGraphNode>());
            }
            nodesByMaxDistanceFromARoot.get(maxDepth).add(dagNode);
        }
    }

}
