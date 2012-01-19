package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import java.util.ArrayList;
import java.util.Arrays;
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

    public NodeItem getNodeItem() {
        return nodeItem;
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
        int numberOfDescendants = 0;
        for (TreeNode child : children) {
            numberOfDescendants += child.getNumberOfDescendants() + 1;
        }
        return numberOfDescendants;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }
}
