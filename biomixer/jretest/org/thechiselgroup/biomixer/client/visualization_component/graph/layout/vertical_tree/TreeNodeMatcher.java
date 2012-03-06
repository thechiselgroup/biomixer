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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.vertical_tree;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.TreeNode;

public class TreeNodeMatcher extends TypeSafeMatcher<TreeNode> {

    public static <T> Matcher<TreeNode> equalsTree(LayoutNode node,
            int numberOfDescendants, Matcher<TreeNode>... matchers) {
        return new TreeNodeMatcher(node, numberOfDescendants, matchers);
    }

    private final LayoutNode node;

    private final int numberOfDescendants;

    private final Matcher<TreeNode>[] treeNodeMatchers;

    public TreeNodeMatcher(LayoutNode node, int numberOfDescendants,
            Matcher<TreeNode>... matchers) {
        this.node = node;
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
        // FIXME useful descriptions
        description.appendText("Node: " + node + ", Number of descendants: "
                + numberOfDescendants);
    }

    @Override
    public boolean matchesSafely(TreeNode treeNode) {
        return (node.equals(treeNode.getLayoutNode())
                && treeNode.getNumberOfDescendants() == numberOfDescendants && allChildrenExactlyMatch(treeNode));

    }
}
