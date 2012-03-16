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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.tree;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DirectedAcyclicGraphNode;

public class DirectedAcyclicGraphNodeMatcher extends TypeSafeMatcher<DirectedAcyclicGraphNode> {

    public static <T> Matcher<DirectedAcyclicGraphNode> equalsDag(LayoutNode node,
            int numberOfDescendants, Matcher<DirectedAcyclicGraphNode>... matchers) {
        return new DirectedAcyclicGraphNodeMatcher(node, numberOfDescendants, matchers);
    }

    private final LayoutNode node;

    private final int numberOfDescendants;

    private final Matcher<DirectedAcyclicGraphNode>[] directedAcyclicGraphNodeMatchers;

    public DirectedAcyclicGraphNodeMatcher(LayoutNode node, int numberOfDescendants,
            Matcher<DirectedAcyclicGraphNode>... matchers) {
        this.node = node;
        this.numberOfDescendants = numberOfDescendants;
        this.directedAcyclicGraphNodeMatchers = matchers;
    }

    /**
     * 
     * @param dagNode
     *            The {@link DirectedAcyclicGraphNode} whose children the matchers will try to
     *            match.
     * @return Each child must be matched by one of the matchers, and there
     *         cannot be any extra matchers left over. The order of the children
     *         and matchers is not important.
     */
    private boolean allChildrenExactlyMatch(DirectedAcyclicGraphNode dagNode) {
        if (dagNode.getChildren().size() != directedAcyclicGraphNodeMatchers.length) {
            return false;
        }
        for (DirectedAcyclicGraphNode childDagNode : dagNode.getChildren()) {
            // one of the matchers must match it
            if (!childHasMatch(childDagNode)) {
                return false;
            }
        }
        return true;
    }

    private boolean childHasMatch(DirectedAcyclicGraphNode childDagNode) {
        for (Matcher<DirectedAcyclicGraphNode> matcher : directedAcyclicGraphNodeMatchers) {
            if (matcher.matches(childDagNode)) {
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
    public boolean matchesSafely(DirectedAcyclicGraphNode dagNode) {
        return (node.equals(dagNode.getLayoutNode())
                && dagNode.getNumberOfDescendants() == numberOfDescendants && allChildrenExactlyMatch(dagNode));

    }
}
