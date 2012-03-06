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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.thechiselgroup.biomixer.client.visualization_component.graph.layout.vertical_tree.TreeNodeMatcher.equalsTree;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.Tree;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.TreeFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.TreeNode;

public class TreeFactoryTest extends AbstractLayoutAlgorithmTest {

    private TreeFactory underTest;

    private List<LayoutNode> getLayoutNodes(List<TreeNode> treeNodes) {
        List<LayoutNode> layoutNodes = new ArrayList<LayoutNode>();
        for (TreeNode treeNode : treeNodes) {
            layoutNodes.add(treeNode.getLayoutNode());
        }
        return layoutNodes;
    }

    @Test
    public void getNodesAtDepthTest() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        LayoutNode node3 = createDefaultNode(graph);
        LayoutNode node4 = createDefaultNode(graph);
        LayoutNode node5 = createDefaultNode(graph);
        LayoutNode node6 = createDefaultNode(graph);
        createDefaultArc(graph, node6, node2);
        createDefaultArc(graph, node5, node2);
        createDefaultArc(graph, node4, node2);
        createDefaultArc(graph, node2, node1);
        createDefaultArc(graph, node3, node1);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);

        // TODO find a more declarative way to do this.
        List<LayoutNode> nodesAtDepth0 = getLayoutNodes(tree.getNodesAtDepth(0));
        assertThat(nodesAtDepth0, containsExactly(node1));

        // TODO find a more declarative way to do this.
        List<LayoutNode> nodesAtDepth1 = getLayoutNodes(tree.getNodesAtDepth(1));
        assertThat(nodesAtDepth1, containsExactly(node2, node3));

        // TODO find a more declarative way to do this.
        List<LayoutNode> nodesAtDepth2 = getLayoutNodes(tree.getNodesAtDepth(2));
        assertThat(nodesAtDepth2, containsExactly(node4, node5, node6));
    }

    private Tree getTreeWithRootNode(List<Tree> trees, LayoutNode node) {
        for (Tree tree : trees) {
            if (tree.getRoot().getLayoutNode().equals(node)) {
                return tree;
            }
        }
        Assert.fail();
        return null;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootOneChild() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        createDefaultArc(graph, node2, node1);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(2));
        assertThat(tree.getRoot(), equalsTree(node1, 1, equalsTree(node2, 0)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootOneChildOneGrandchild() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        LayoutNode node3 = createDefaultNode(graph);
        createDefaultArc(graph, node3, node2);
        createDefaultArc(graph, node2, node1);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(3));
        assertThat(
                tree.getRoot(),
                equalsTree(node1, 2, equalsTree(node2, 1, equalsTree(node3, 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootThreeChildren() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        LayoutNode node3 = createDefaultNode(graph);
        LayoutNode node4 = createDefaultNode(graph);
        createDefaultArc(graph, node4, node1);
        createDefaultArc(graph, node3, node1);
        createDefaultArc(graph, node2, node1);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(2));
        assertThat(
                tree.getRoot(),
                equalsTree(node1, 3, equalsTree(node2, 0),
                        equalsTree(node3, 0), equalsTree(node4, 0)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootTwoChildrenOneHasThreeChildrenOtherHasNone() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        LayoutNode node3 = createDefaultNode(graph);
        LayoutNode node4 = createDefaultNode(graph);
        LayoutNode node5 = createDefaultNode(graph);
        LayoutNode node6 = createDefaultNode(graph);
        createDefaultArc(graph, node4, node2);
        createDefaultArc(graph, node5, node2);
        createDefaultArc(graph, node6, node2);
        createDefaultArc(graph, node2, node1);
        createDefaultArc(graph, node3, node1);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(3));
        assertThat(
                tree.getRoot(),
                equalsTree(
                        node1,
                        5,
                        equalsTree(node2, 3, equalsTree(node4, 0),
                                equalsTree(node5, 0), equalsTree(node6, 0)),
                        equalsTree(node3, 0)));
    }

    @Before
    public void setUp() {
        this.underTest = new TreeFactory();
    }

    @Test
    public void singleNode() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        createDefaultNode(graph);

        List<Tree> trees = underTest.getTrees(graph);

        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(1));
        assertThat(tree.size(), equalTo(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void treeContainingNodeWithTwoParents() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        LayoutNode node3 = createDefaultNode(graph);
        LayoutNode node4 = createDefaultNode(graph);
        LayoutNode node5 = createDefaultNode(graph);
        createDefaultArc(graph, node5, node3);
        createDefaultArc(graph, node5, node4);
        createDefaultArc(graph, node3, node2);
        createDefaultArc(graph, node2, node1);
        createDefaultArc(graph, node4, node1);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(
                tree.getRoot(),
                equalsTree(
                        node1,
                        4,
                        equalsTree(node2, 2,
                                equalsTree(node3, 1, equalsTree(node5, 0))),
                        equalsTree(node4, 1, equalsTree(node5, 0))));
    }

    @Test
    public void twoParentsDifferentLengthPathsDepthTest() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        LayoutNode node3 = createDefaultNode(graph);
        LayoutNode node4 = createDefaultNode(graph);
        LayoutNode node5 = createDefaultNode(graph);
        createDefaultArc(graph, node5, node3);
        createDefaultArc(graph, node5, node4);
        createDefaultArc(graph, node3, node2);
        createDefaultArc(graph, node2, node1);
        createDefaultArc(graph, node4, node1);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        List<LayoutNode> nodesAtDepth0 = getLayoutNodes(tree.getNodesAtDepth(0));
        assertThat(nodesAtDepth0, containsExactly(node1));

        List<LayoutNode> nodesAtDepth1 = getLayoutNodes(tree.getNodesAtDepth(1));
        assertThat(nodesAtDepth1, containsExactly(node2, node4));

        List<LayoutNode> nodesAtDepth2 = getLayoutNodes(tree.getNodesAtDepth(2));
        assertThat(nodesAtDepth2, containsExactly(node3));

        List<LayoutNode> nodesAtDepth3 = getLayoutNodes(tree.getNodesAtDepth(3));
        assertThat(nodesAtDepth3, containsExactly(node5));
    }

    @Test
    public void twoRootsNoChildren() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        createDefaultNode(graph);
        createDefaultNode(graph);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(2));
        assertThat(trees.get(0).size(), equalTo(1));
        assertThat(trees.get(1).size(), equalTo(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void twoRootsOneWithTwoChildrenOneWithOne() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        LayoutNode node3 = createDefaultNode(graph);
        LayoutNode node4 = createDefaultNode(graph);
        LayoutNode node5 = createDefaultNode(graph);
        createDefaultArc(graph, node3, node1);
        createDefaultArc(graph, node4, node1);
        createDefaultArc(graph, node5, node2);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(2));

        Tree tree1 = getTreeWithRootNode(trees, node1);
        assertThat(tree1.getHeight(), equalTo(2));
        assertThat(
                tree1.getRoot(),
                equalsTree(node1, 2, equalsTree(node3, 0), equalsTree(node4, 0)));

        Tree tree2 = getTreeWithRootNode(trees, node2);
        assertThat(tree2.getHeight(), equalTo(2));
        assertThat(tree2.getRoot(), equalsTree(node2, 1, equalsTree(node5, 0)));
    }

}
