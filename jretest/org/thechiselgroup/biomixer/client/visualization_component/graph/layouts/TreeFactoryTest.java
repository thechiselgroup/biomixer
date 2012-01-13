package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;

public class TreeFactoryTest {

    private TreeFactory underTest;

    private void assertThatChildrenContainNodeItem(TreeNode treeNode,
            NodeItem nodeItem) {
        assertTrue(treeNodesContainNodeItem(nodeItem, treeNode.getChildren()));
    }

    private void assertThatChildrenContainStubNodes(TreeNode treeNode,
            StubGraphStructure stubGraph, int[] stubNodeNumbers) {
        for (int i = 0; i < stubNodeNumbers.length; i++) {
            assertThatChildrenContainNodeItem(treeNode,
                    stubGraph.getNodeItem(stubNodeNumbers[i]));
        }
    }

    @Test
    public void getNodesAtDepthTest() {
        int numberOfNodes = 6;
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(1, 3);
        stubGraph.createArc(1, 4);
        stubGraph.createArc(1, 5);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        List<TreeNode> nodesAtDepth0 = tree.getNodesAtDepth(0);
        assertThat(nodesAtDepth0.size(), equalTo(1));
        assertTrue(treeNodesContainNodeItem(stubGraph.getNodeItem(0),
                nodesAtDepth0));
        List<TreeNode> nodesAtDepth1 = tree.getNodesAtDepth(1);
        assertThat(nodesAtDepth1.size(), equalTo(2));
        assertTrue(treeNodesContainNodeItem(stubGraph.getNodeItem(1),
                nodesAtDepth1));
        assertTrue(treeNodesContainNodeItem(stubGraph.getNodeItem(2),
                nodesAtDepth1));
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

        Tree desiredTree = null;
        for (Tree tree : trees) {
            if (tree.getRoot().getNodeItem().equals(dummyNodeItem)) {
                desiredTree = tree;
                break;
            }
        }
        assert desiredTree != null;
        return desiredTree;
    }

    @Test
    public void oneRootOneChild() {
        int numberOfNodes = 2;
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);
        stubGraph.createArc(0, 1);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.size(), equalTo(numberOfNodes));
        TreeNode root = tree.getRoot();
        assertTrue(root.getNodeItem().equals(stubGraph.getNodeItem(0)));
        assertThat(root.getNumberOfDescendants(), equalTo(numberOfNodes - 1));
        assertTrue(root.getChildren().get(0).getNodeItem()
                .equals(stubGraph.getNodeItem(1)));
        assertThat(tree.getHeight(), equalTo(2));
    }

    @Test
    public void oneRootOneChildOneGrandchild() {
        int numberOfNodes = 3;
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(1, 2);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());

        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(tree.size(), equalTo(numberOfNodes));
        TreeNode root = tree.getRoot();
        assertThat(root.getNumberOfDescendants(), equalTo(2));
        assertThat(root.getChildren().size(), equalTo(1));
        TreeNode rootChild = root.getChildren().get(0);
        assertTrue(rootChild.getNodeItem().equals(stubGraph.getNodeItem(1)));
        assertThat(rootChild.getNumberOfDescendants(), equalTo(1));
        assertThat(rootChild.getChildren().size(), equalTo(1));
        TreeNode rootGrandChild = rootChild.getChildren().get(0);
        assertTrue(rootGrandChild.getNodeItem()
                .equals(stubGraph.getNodeItem(2)));
        assertThat(rootGrandChild.getNumberOfDescendants(), equalTo(0));
        assertThat(rootGrandChild.getChildren().size(), equalTo(0));
        assertThat(tree.getHeight(), equalTo(3));
    }

    @Test
    public void oneRootThreeChildren() {
        int numberOfNodes = 4;
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(0, 3);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.size(), equalTo(numberOfNodes));
        TreeNode root = tree.getRoot();
        assertTrue(root.getNodeItem().equals(stubGraph.getNodeItem(0)));
        assertThat(root.getNumberOfDescendants(), equalTo(numberOfNodes - 1));
        assertThat(root.getChildren().size(), equalTo(3));
        assertThatChildrenContainStubNodes(root, stubGraph,
                new int[] { 1, 2, 3 });
        assertThat(tree.getHeight(), equalTo(2));
    }

    @Test
    public void oneRootTwoChildrenOneHasThreeChildrenOtherHasNone() {
        int numberOfNodes = 6;
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(1, 3);
        stubGraph.createArc(1, 4);
        stubGraph.createArc(1, 5);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.size(), equalTo(numberOfNodes));
        TreeNode root = tree.getRoot();
        assertTrue(root.getNodeItem().equals(stubGraph.getNodeItem(0)));
        assertThat(root.getNumberOfDescendants(), equalTo(numberOfNodes - 1));
        assertThat(root.getChildren().size(), equalTo(2));
        assertThatChildrenContainNodeItem(root, stubGraph.getNodeItem(1));
        assertThatChildrenContainNodeItem(root, stubGraph.getNodeItem(2));
        TreeNode nodeWithoutChildren = getTreeNodeForNodeItem(
                root.getChildren(), stubGraph.getNodeItem(2));
        assertThat(nodeWithoutChildren.getNumberOfDescendants(), equalTo(0));
        TreeNode nodeWithChildren = getTreeNodeForNodeItem(root.getChildren(),
                stubGraph.getNodeItem(1));
        assertThat(nodeWithChildren.getNumberOfDescendants(), equalTo(3));
        assertThat(nodeWithChildren.getChildren().size(), equalTo(3));
        assertThatChildrenContainStubNodes(nodeWithChildren, stubGraph,
                new int[] { 3, 4, 5 });
        assertThat(tree.getHeight(), equalTo(3));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.underTest = new TreeFactory();
    }

    @Test
    public void singleNode() {
        int numberOfNodes = 1;
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());

        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(tree.size(), equalTo(1));
        assertThat(tree.getHeight(), equalTo(1));
    }

    private boolean treeNodesContainNodeItem(NodeItem nodeItem,
            List<TreeNode> children) {
        boolean nodeItemFound = false;
        for (TreeNode child : children) {
            if (child.getNodeItem().equals(nodeItem)) {
                nodeItemFound = true;
                break;
            }
        }
        return nodeItemFound;
    }

    @Test
    public void twoRootsNoChildren() {
        int numberOfNodes = 2;
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(2));
        assertThat(trees.get(0).size(), equalTo(1));
        assertThat(trees.get(1).size(), equalTo(1));
    }

    @Test
    public void twoRootsOneWithTwoChildrenOneWithOne() {
        int numberOfNodes = 5;
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(0, 3);
        stubGraph.createArc(1, 4);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(2));

        Tree tree1 = getTreeWithRootNodeItem(trees, stubGraph.getNodeItem(0));
        assertThat(tree1.size(), equalTo(3));
        assertThatChildrenContainStubNodes(tree1.getRoot(), stubGraph,
                new int[] { 2, 3 });
        assertThat(tree1.getHeight(), equalTo(2));

        Tree tree2 = getTreeWithRootNodeItem(trees, stubGraph.getNodeItem(1));
        assertThat(tree2.size(), equalTo(2));
        assertThatChildrenContainStubNodes(tree2.getRoot(), stubGraph,
                new int[] { 4 });
        assertThat(tree2.getHeight(), equalTo(2));
    }

}
