package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import java.util.List;

public class Tree {

    private final TreeNode root;

    public Tree(TreeNode root) {
        this.root = root;
    }

    public int getHeight() {
        return root.getHeight();
    }

    /**
     * 
     * @param depth
     *            : 0 -> height - 1
     * @return Find all nodes at the specified depth from the root
     */
    public List<TreeNode> getNodesAtDepth(int depth) {
        assert depth >= 0;
        return root.getNodesFromLevelsBelow(depth);
    }

    public TreeNode getRoot() {
        return root;
    }

    public int size() {
        return root.getNumberOfDescendants() + 1;
    }

}
