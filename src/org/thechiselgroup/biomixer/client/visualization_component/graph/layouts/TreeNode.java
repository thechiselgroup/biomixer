package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;

public class TreeNode {

    private final NodeItem nodeItem;

    private final List<TreeNode> children = new ArrayList<TreeNode>();

    public TreeNode(NodeItem nodeItem) {
        this.nodeItem = nodeItem;
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public NodeItem getNodeItem() {
        return nodeItem;
    }

    public List<TreeNode> getNodesFromLevelsBelow(int levelsBelow) {
        if (levelsBelow == 0) {
            // This is for the case where there is just the root
            List<TreeNode> selfList = new ArrayList<TreeNode>();
            selfList.add(this);
            return selfList;
        } else if (levelsBelow == 1) {
            return children;
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
        int numberOfDescendants = 0;
        for (TreeNode child : children) {
            numberOfDescendants += child.getNumberOfDescendants() + 1;
        }
        return numberOfDescendants;
    }

    public int height() {
        if (children.size() == 0) {
            return 1;
        }

        int maxHeight = Integer.MIN_VALUE;
        for (int i = 0; i < children.size(); i++) {
            int height = children.get(i).height();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }

        return maxHeight + 1;
    }
}
