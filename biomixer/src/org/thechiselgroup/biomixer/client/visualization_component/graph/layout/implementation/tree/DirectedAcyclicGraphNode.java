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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * A single node in a {@link DirectedAcyclicGraph}. Wraps a {@link LayoutNode}
 * so that it can be placed into the graph structure.
 * 
 * @author drusk
 * 
 */
public class DirectedAcyclicGraphNode {

    private final LayoutNode layoutNode;

    private final List<DirectedAcyclicGraphNode> children = new ArrayList<DirectedAcyclicGraphNode>();

    public DirectedAcyclicGraphNode(LayoutNode layoutNode) {
        this.layoutNode = layoutNode;
    }

    public void addChild(DirectedAcyclicGraphNode child) {
        children.add(child);
    }

    /**
     * 
     * @return the nodes immediately adjacent to this node further down the
     *         path. It may return multiple nodes because the path may branch at
     *         this node.
     */
    public List<DirectedAcyclicGraphNode> getChildren() {
        return children;
    }

    /**
     * 
     * @return the set of all nodes further along the path which this node is
     *         on.
     */
    public Set<DirectedAcyclicGraphNode> getDescendants() {
        Set<DirectedAcyclicGraphNode> descendants = new HashSet<DirectedAcyclicGraphNode>();
        for (DirectedAcyclicGraphNode childNode : children) {
            descendants.add(childNode);
            descendants.addAll(childNode.getDescendants());
        }
        return descendants;
    }

    /**
     * 
     * @return the wrapped <code>LayoutNode</code>
     */
    public LayoutNode getLayoutNode() {
        return layoutNode;
    }

    /**
     * 
     * @param targetNode
     *            the node being searched for
     * @return the length of the longest path from the current node to the
     *         <code>targetNode</code>. Returns 0 if the current node is the
     *         <code>targetNode</code>. Returns -1 if the
     *         <code>targetNode</code> cannot be reached from the current node.
     */
    public int getMaxDistance(DirectedAcyclicGraphNode targetNode) {
        if (targetNode.equals(this)) {
            return 0;
        }

        int maxDistance = -1;
        if (children.contains(targetNode)) {
            maxDistance = 1;
        }
        for (DirectedAcyclicGraphNode child : children) {
            if (child.getDescendants().contains(targetNode)
                    && (child.getMaxDistance(targetNode) + 1) > maxDistance) {
                maxDistance = child.getMaxDistance(targetNode) + 1;
            }
        }
        return maxDistance;
    }

    /**
     * 
     * @return the maximum number of hops (edges) between this node and a node
     *         which is at the end of a path.
     */
    public int getMaxLengthToEndOfPath() {
        int maxLength = 0;
        for (int i = 0; i < children.size(); i++) {
            int length = children.get(i).getMaxLengthToEndOfPath() + 1;
            if (length > maxLength) {
                maxLength = length;
            }
        }
        return maxLength;
    }

    /**
     * 
     * @return the number of nodes further along the path which this node is on.
     */
    public int getNumberOfDescendants() {
        return getDescendants().size();
    }

    /**
     * 
     * @return <code>true</code> if there are more nodes further along the
     *         current path
     */
    public boolean isEndOfPath() {
        return children.size() == 0;
    }

}
