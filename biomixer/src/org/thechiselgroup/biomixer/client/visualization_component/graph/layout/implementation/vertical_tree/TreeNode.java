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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

public class TreeNode {

    private final LayoutNode layoutNode;

    private final List<TreeNode> children = new ArrayList<TreeNode>();

    public TreeNode(LayoutNode layoutNode) {
        this.layoutNode = layoutNode;
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public Set<TreeNode> getDescendants() {
        Set<TreeNode> descendants = new HashSet<TreeNode>();
        for (TreeNode childNode : children) {
            descendants.add(childNode);
            descendants.addAll(childNode.getDescendants());
        }
        return descendants;
    }

    public int getHeight() {
        int maxHeight = 0;
        for (int i = 0; i < children.size(); i++) {
            int height = children.get(i).getHeight();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }
        return maxHeight + 1;
    }

    public LayoutNode getLayoutNode() {
        return layoutNode;
    }

    /**
     * 
     * @param treeNode
     *            The node being searched for
     * @return The maximum depth down the tree which the specified treeNode
     *         appears from the current node. For example, if there is a branch
     *         below the current node so that there are multiple paths to the
     *         specified treeNode, this method will return the length of the
     *         longest path.
     */
    public int getMaxDistance(TreeNode treeNode) {
        if (treeNode.equals(this)) {
            // for case of root
            return 0;
        }

        assert this.getDescendants().contains(treeNode);

        int maxDistance = -1;
        if (children.contains(treeNode)) {
            maxDistance = 1;
        }
        for (TreeNode child : children) {
            if (child.getDescendants().contains(treeNode)
                    && (child.getMaxDistance(treeNode) + 1) > maxDistance) {
                maxDistance = child.getMaxDistance(treeNode) + 1;
            }
        }
        assert maxDistance > 0;
        return maxDistance;
    }

    public List<TreeNode> getNodesFromLevelsBelow(int levelsBelow) {
        if (levelsBelow == 0) {
            return Arrays.asList(this);
        } else {
            List<TreeNode> descendantNodes = new ArrayList<TreeNode>();
            for (TreeNode childNode : children) {
                descendantNodes.addAll(childNode
                        .getNodesFromLevelsBelow(levelsBelow - 1));
            }
            return descendantNodes;
        }
    }

    public int getNumberOfDescendants() {
        return getDescendants().size();
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    // @Override
    // public String toString() {
    // String childrenIds = "[";
    // for (int i = 0; i < children.size(); i++) {
    // TreeNode child = children.get(i);
    // if (i != 0) {
    // childrenIds += ", ";
    // }
    // childrenIds += child.getNodeItem().getNode().getId();
    // }
    // childrenIds += "]";
    // return "Node Id: " + layoutNode.getNode().getId()
    // + ", Number of descendants: " + getNumberOfDescendants()
    // + ", Children Ids: " + childrenIds;
    // }

}
