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
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutAlgorithmTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.Tree;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.TreeFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.TreeNode;

public class TreeFactoryTest extends AbstractLayoutAlgorithmTest {

    private TreeFactory underTest;

    private List<LayoutNode> getNodesAtDepth(Tree tree, int depth) {
        List<LayoutNode> layoutNodes = new ArrayList<LayoutNode>();
        for (TreeNode treeNode : tree.getNodesAtDepth(depth)) {
            layoutNodes.add(treeNode.getLayoutNode());
        }
        return layoutNodes;
    }

    @Test
    public void getNodesAtDepthTest() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(6);
        createArc(nodes[5], nodes[1]);
        createArc(nodes[4], nodes[1]);
        createArc(nodes[3], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[2], nodes[0]);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);

        assertThat(getNodesAtDepth(tree, 0), containsExactly(nodes[0]));
        assertThat(getNodesAtDepth(tree, 1),
                containsExactly(nodes[1], nodes[2]));
        assertThat(getNodesAtDepth(tree, 2),
                containsExactly(nodes[3], nodes[4], nodes[5]));
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
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(2);
        createArc(nodes[1], nodes[0]);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(2));
        assertThat(tree.getRoot(),
                equalsTree(nodes[0], 1, equalsTree(nodes[1], 0)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootOneChildOneGrandchild() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(3);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[1], nodes[0]);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(3));
        assertThat(
                tree.getRoot(),
                equalsTree(nodes[0], 2,
                        equalsTree(nodes[1], 1, equalsTree(nodes[2], 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootThreeChildren() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(4);
        createArc(nodes[3], nodes[0]);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[1], nodes[0]);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(2));
        assertThat(
                tree.getRoot(),
                equalsTree(nodes[0], 3, equalsTree(nodes[1], 0),
                        equalsTree(nodes[2], 0), equalsTree(nodes[3], 0)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootTwoChildrenOneHasThreeChildrenOtherHasNone() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(6);
        createArc(nodes[3], nodes[1]);
        createArc(nodes[4], nodes[1]);
        createArc(nodes[5], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[2], nodes[0]);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(3));
        assertThat(
                tree.getRoot(),
                equalsTree(
                        nodes[0],
                        5,
                        equalsTree(nodes[1], 3, equalsTree(nodes[3], 0),
                                equalsTree(nodes[4], 0),
                                equalsTree(nodes[5], 0)),
                        equalsTree(nodes[2], 0)));
    }

    @Before
    public void setUp() {
        this.underTest = new TreeFactory();
    }

    @Test
    public void singleNode() {
        createGraph(0, 0, 400, 400);
        createNodes(1);

        List<Tree> trees = underTest.getTrees(graph);

        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(1));
        assertThat(tree.size(), equalTo(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void treeContainingNodeWithTwoParents() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(5);
        createArc(nodes[4], nodes[2]);
        createArc(nodes[4], nodes[3]);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[3], nodes[0]);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(
                tree.getRoot(),
                equalsTree(
                        nodes[0],
                        4,
                        equalsTree(
                                nodes[1],
                                2,
                                equalsTree(nodes[2], 1, equalsTree(nodes[4], 0))),
                        equalsTree(nodes[3], 1, equalsTree(nodes[4], 0))));
    }

    @Test
    public void twoParentsDifferentLengthPathsDepthTest() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(5);

        createArc(nodes[4], nodes[2]);
        createArc(nodes[4], nodes[3]);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[3], nodes[0]);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);

        assertThat(getNodesAtDepth(tree, 0), containsExactly(nodes[0]));
        assertThat(getNodesAtDepth(tree, 1),
                containsExactly(nodes[1], nodes[3]));
        assertThat(getNodesAtDepth(tree, 2), containsExactly(nodes[2]));
        assertThat(getNodesAtDepth(tree, 3), containsExactly(nodes[4]));
    }

    @Test
    public void twoRootsNoChildren() {
        createGraph(0, 0, 400, 400);
        createNodes(2);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(2));
        assertThat(trees.get(0).size(), equalTo(1));
        assertThat(trees.get(1).size(), equalTo(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void twoRootsOneWithTwoChildrenOneWithOne() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(5);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[3], nodes[0]);
        createArc(nodes[4], nodes[1]);

        List<Tree> trees = underTest.getTrees(graph);
        assertThat(trees.size(), equalTo(2));

        Tree tree1 = getTreeWithRootNode(trees, nodes[0]);
        assertThat(tree1.getHeight(), equalTo(2));
        assertThat(
                tree1.getRoot(),
                equalsTree(nodes[0], 2, equalsTree(nodes[2], 0),
                        equalsTree(nodes[3], 0)));

        Tree tree2 = getTreeWithRootNode(trees, nodes[1]);
        assertThat(tree2.getHeight(), equalTo(2));
        assertThat(tree2.getRoot(),
                equalsTree(nodes[1], 1, equalsTree(nodes[4], 0)));
    }

}
