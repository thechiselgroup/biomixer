package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class TreeNodeMatcher extends TypeSafeMatcher<TreeNode> {

    public static <T> Matcher<TreeNode> equalsTree(int nodeIndex,
            int numberOfDescendants, Matcher<TreeNode>... matchers) {
        return new TreeNodeMatcher(nodeIndex, numberOfDescendants, matchers);
    }

    private final int nodeIndex;

    private final int numberOfDescendants;

    private final Matcher<TreeNode>[] treeNodeMatchers;

    public TreeNodeMatcher(int nodeIndex, int numberOfDescendants,
            Matcher<TreeNode>... matchers) {
        this.nodeIndex = nodeIndex;
        this.numberOfDescendants = numberOfDescendants;
        this.treeNodeMatchers = matchers;
    }

    /**
     * 
     * @param treeNode
     *            The TreeNode whose children the matchers will try to match.
     * @return Each child must be matched by one of the matchers, and there
     *         cannot be any extra matchers left over. The order of the children
     *         and matchers is not important.
     */
    private boolean allChildrenExactlyMatch(TreeNode treeNode) {
        if (treeNode.getChildren().size() != treeNodeMatchers.length) {
            return false;
        }
        for (TreeNode childTreeNode : treeNode.getChildren()) {
            // one of the matchers must match it
            if (!childHasMatch(childTreeNode)) {
                return false;
            }
        }
        return true;
    }

    private boolean childHasMatch(TreeNode childTreeNode) {
        for (Matcher<TreeNode> matcher : treeNodeMatchers) {
            if (matcher.matches(childTreeNode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(nodeIndex);
        for (Matcher<TreeNode> matcher : treeNodeMatchers) {
            matcher.describeTo(description);
        }
    }

    @Override
    public boolean matchesSafely(TreeNode treeNode) {
        return ("" + nodeIndex)
                .equals(treeNode.getNodeItem().getNode().getId())
                && treeNode.getNumberOfDescendants() == numberOfDescendants
                && allChildrenExactlyMatch(treeNode);

    }
}
