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

    public void addChildren(List<NodeItem> childrenItemNodes) {
        assert childrenItemNodes != null;

        for (NodeItem childItemNode : childrenItemNodes) {
            children.add(new TreeNode(childItemNode));
        }
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public NodeItem getNodeItem() {
        return nodeItem;
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
