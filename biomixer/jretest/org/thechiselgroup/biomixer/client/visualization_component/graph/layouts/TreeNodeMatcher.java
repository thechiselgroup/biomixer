package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import static org.junit.Assert.assertThat;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class TreeNodeMatcher extends TypeSafeMatcher<TreeNode> {

    public static <T> Matcher<TreeNode> isTree(StubGraphStructure stubGraph) {
        return new TreeNodeMatcher(stubGraph, 0);
    }

    public static <T> Matcher<TreeNode> isTree(StubGraphStructure stubGraph,
            int nodeNumber) {
        return new TreeNodeMatcher(stubGraph, nodeNumber);
    }

    private final StubGraphStructure stubGraph;

    private final int nodeNumber;

    public TreeNodeMatcher(StubGraphStructure stubGraph, int nodeNumber) {
        this.stubGraph = stubGraph;
        this.nodeNumber = nodeNumber;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(stubGraph);
    }

    @Override
    public boolean matchesSafely(TreeNode treeNode) {
        int i = 1;
        for (TreeNode childNode : treeNode.getChildren()) {
            // XXX they should not have to necessarily be stored in a certain
            // order
            assertThat(childNode, isTree(stubGraph, nodeNumber + i));
            i++;
        }
        return treeNode.getNodeItem().equals(stubGraph.getNodeItem(nodeNumber));
        // TODO
        // 1. check expectedNodeItem
        // 2. check expectedNumberOfDescendants
        // 3. recursively check expected child matchers
    }
}
