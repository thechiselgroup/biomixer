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

/**
 * Stores a node structure where there are nodes which directed arcs between
 * them. Nodes may have more than one parent, and there may be more than one
 * node which has no parents.
 * 
 * @author drusk
 * 
 */
public class DirectedNodeNetwork {

    /**
     * Nodes that have no parents.
     */
    private final List<NetworkNode> roots;

    /**
     * Provides quick lookup of nodes based on their max distance from any root
     */
    private Map<Integer, List<NetworkNode>> nodesByMaxDistanceFromARoot = new HashMap<Integer, List<NetworkNode>>();

    /**
     * 
     * @param roots
     *            nodes which have no parents. Cannot be an empty list.
     */
    public DirectedNodeNetwork(List<NetworkNode> roots) {
        assert roots != null;
        assert !roots.isEmpty();

        this.roots = roots;
        initializeNodeDistanceMapping();
    }

    /**
     * 
     * @return all nodes in the network
     */
    public Set<NetworkNode> getAllNodes() {
        Set<NetworkNode> allNodes = new HashSet<NetworkNode>();
        for (NetworkNode root : roots) {
            allNodes.add(root);
            allNodes.addAll(root.getDescendants());
        }
        return allNodes;
    }

    /**
     * 
     * @param networkNode
     *            the node to find the distance for
     * @return the maximum distance of <code>treeNode</code> from any of the
     *         roots
     */
    private int getMaxDistanceFromAnyRoot(NetworkNode networkNode) {
        int maxDepth = 0;
        for (NetworkNode root : roots) {
            int maxDepthFromRoot = root.getMaxDistance(networkNode);
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
    public List<NetworkNode> getNodesAtDistanceFromRoot(int distance) {
        assert distance >= 0;
        assert nodesByMaxDistanceFromARoot.containsKey(Integer
                .valueOf(distance)) : "no nodes at distance=" + distance
                + " from a root";
        return nodesByMaxDistanceFromARoot.get(Integer.valueOf(distance));
    }

    /**
     * 
     * @return the total number of nodes in the network, including roots
     */
    public int getNumberOfNodes() {
        int size = 0;
        for (NetworkNode root : roots) {
            size += root.getNumberOfDescendants() + 1;
        }
        return size;
    }

    /**
     * 
     * @return the number of nodes on the longest path from a root to leaf
     */
    public int getNumberOfNodesOnLongestPath() {
        int longestPath = 0;
        for (NetworkNode root : roots) {
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
    public List<NetworkNode> getRoots() {
        return roots;
    }

    private void initializeNodeDistanceMapping() {
        Set<NetworkNode> allNodes = getAllNodes();
        for (NetworkNode treeNode : allNodes) {
            Integer maxDepth = Integer
                    .valueOf(getMaxDistanceFromAnyRoot(treeNode));
            if (!nodesByMaxDistanceFromARoot.containsKey(maxDepth)) {
                nodesByMaxDistanceFromARoot.put(maxDepth,
                        new ArrayList<NetworkNode>());
            }
            nodesByMaxDistanceFromARoot.get(maxDepth).add(treeNode);
        }
    }

}
