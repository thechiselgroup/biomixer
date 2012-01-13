package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;


public class Tree {

    private final TreeNode root;

    public Tree(TreeNode root) {
        this.root = root;
    }

    public TreeNode getRoot() {
        return root;
    }

    public int size() {
        return root.getNumberOfDescendants() + 1;
    }

}
