package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;

public class Tree {

    private final TreeNode root;

    public Tree(NodeItem root) {
        this.root = new TreeNode(root);
    }

    public TreeNode getRoot() {
        return root;
    }

    public int size() {
        return root.getNumberOfDescendants() + 1;
    }

}
