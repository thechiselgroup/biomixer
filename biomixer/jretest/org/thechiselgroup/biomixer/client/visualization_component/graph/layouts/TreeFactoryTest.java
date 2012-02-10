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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.thechiselgroup.biomixer.client.visualization_component.graph.layouts.TreeNodeMatcher.equalsTree;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;

public class TreeFactoryTest {

    private TreeFactory underTest;

    @Test
    public void getNodesAtDepthTest() {
        StubGraphStructure stubGraph = new StubGraphStructure(6);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(1, 3);
        stubGraph.createArc(1, 4);
        stubGraph.createArc(1, 5);

        List<Tree> trees = getTrees(stubGraph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);

        // TODO find a more declarative way to do this.
        List<TreeNode> nodesAtDepth0 = tree.getNodesAtDepth(0);
        assertThat(nodesAtDepth0.size(), equalTo(1));
        assertTrue(treeNodesContainNodeItem(stubGraph.getNodeItem(0),
                nodesAtDepth0));

        // TODO find a more declarative way to do this.
        List<TreeNode> nodesAtDepth1 = tree.getNodesAtDepth(1);
        assertThat(nodesAtDepth1.size(), equalTo(2));
        assertTrue(treeNodesContainNodeItem(stubGraph.getNodeItem(1),
                nodesAtDepth1));
        assertTrue(treeNodesContainNodeItem(stubGraph.getNodeItem(2),
                nodesAtDepth1));

        // TODO find a more declarative way to do this.
        List<TreeNode> nodesAtDepth2 = tree.getNodesAtDepth(2);
        assertThat(nodesAtDepth2.size(), equalTo(3));
        assertTrue(treeNodesContainNodeItem(stubGraph.getNodeItem(3),
                nodesAtDepth2));
        assertTrue(treeNodesContainNodeItem(stubGraph.getNodeItem(4),
                nodesAtDepth2));
        assertTrue(treeNodesContainNodeItem(stubGraph.getNodeItem(5),
                nodesAtDepth2));
    }

    public TreeNode getTreeNodeForNodeItem(List<TreeNode> treeNodes,
            NodeItem nodeItem) {
        for (TreeNode treeNode : treeNodes) {
            if (treeNode.getNodeItem().equals(nodeItem)) {
                return treeNode;
            }
        }
        return null;
    }

    private Tree getTreeWithRootNodeItem(List<Tree> trees,
            NodeItem dummyNodeItem) {

        for (Tree tree : trees) {
            if (tree.getRoot().getNodeItem().equals(dummyNodeItem)) {
                return tree;
            }
        }

        Assert.fail();
        return null;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootOneChild() {
        StubGraphStructure stubGraph = new StubGraphStructure(2);
        stubGraph.createArc(0, 1);

        List<Tree> trees = getTrees(stubGraph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(2));
        assertThat(tree.getRoot(), equalsTree(0, 1, equalsTree(1, 0)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootOneChildOneGrandchild() {
        StubGraphStructure stubGraph = new StubGraphStructure(3);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(1, 2);

        List<Tree> trees = getTrees(stubGraph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(3));
        assertThat(tree.getRoot(),
                equalsTree(0, 2, equalsTree(1, 1, equalsTree(2, 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootThreeChildren() {
        StubGraphStructure stubGraph = new StubGraphStructure(4);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(0, 3);

        List<Tree> trees = getTrees(stubGraph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(2));
        assertThat(
                tree.getRoot(),
                equalsTree(0, 3, equalsTree(1, 0), equalsTree(2, 0),
                        equalsTree(3, 0)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootTwoChildrenOneHasThreeChildrenOtherHasNone() {
        StubGraphStructure stubGraph = new StubGraphStructure(6);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(1, 3);
        stubGraph.createArc(1, 4);
        stubGraph.createArc(1, 5);

        List<Tree> trees = getTrees(stubGraph);
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(3));
        assertThat(
                tree.getRoot(),
                equalsTree(
                        0,
                        5,
                        equalsTree(1, 3, equalsTree(3, 0), equalsTree(4, 0),
                                equalsTree(5, 0)), equalsTree(2, 0)));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.underTest = new TreeFactory();
    }

    @Test
    public void singleNode() {
        StubGraphStructure stubGraph = new StubGraphStructure(1);

        List<Tree> trees = getTrees(stubGraph);

        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(tree.getHeight(), equalTo(1));
        assertThat(tree.size(), equalTo(1));
    }

    private boolean treeNodesContainNodeItem(NodeItem nodeItem,
            List<TreeNode> children) {
        for (TreeNode child : children) {
            if (child.getNodeItem().equals(nodeItem)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void twoParents() {
        StubGraphStructure stubGraph = new StubGraphStructure(5);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(1, 2);
        stubGraph.createArc(2, 4);
        stubGraph.createArc(0, 3);
        stubGraph.createArc(3, 4);

        List<Tree> trees = getTrees(stubGraph);
        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(
                tree.getRoot(),
                equalsTree(0, 4,
                        equalsTree(1, 2, equalsTree(2, 1, equalsTree(4, 0))),
                        equalsTree(3, 1, equalsTree(4, 0))));
    }

    @Test
    public void twoParentsDifferentLengthPathsDepthTest() {
        StubGraphStructure stubGraph = new StubGraphStructure(5);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(1, 2);
        stubGraph.createArc(2, 4);
        stubGraph.createArc(0, 3);
        stubGraph.createArc(3, 4);

        List<Tree> trees = getTrees(stubGraph);
        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(tree.getNodesAtDepth(0).size(), equalTo(1));
        assertThat(tree.getNodesAtDepth(1).size(), equalTo(2));
        assertThat(tree.getNodesAtDepth(2).size(), equalTo(1));
        assertThat(tree.getNodesAtDepth(3).size(), equalTo(1));
    }

    private List<Tree> getTrees(StubGraphStructure stubGraph) {
        return underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
    }

    @Test
    public void twoRootsNoChildren() {
        StubGraphStructure stubGraph = new StubGraphStructure(2);

        List<Tree> trees = getTrees(stubGraph);
        assertThat(trees.size(), equalTo(2));
        assertThat(trees.get(0).size(), equalTo(1));
        assertThat(trees.get(1).size(), equalTo(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void twoRootsOneWithTwoChildrenOneWithOne() {
        StubGraphStructure stubGraph = new StubGraphStructure(5);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(0, 3);
        stubGraph.createArc(1, 4);

        List<Tree> trees = getTrees(stubGraph);
        assertThat(trees.size(), equalTo(2));

        Tree tree1 = getTreeWithRootNodeItem(trees, stubGraph.getNodeItem(0));
        assertThat(tree1.getHeight(), equalTo(2));
        assertThat(tree1.getRoot(),
                equalsTree(0, 2, equalsTree(2, 0), equalsTree(3, 0)));

        Tree tree2 = getTreeWithRootNodeItem(trees, stubGraph.getNodeItem(1));
        assertThat(tree2.getHeight(), equalTo(2));
        assertThat(tree2.getRoot(), equalsTree(1, 1, equalsTree(4, 0)));
    }

}
