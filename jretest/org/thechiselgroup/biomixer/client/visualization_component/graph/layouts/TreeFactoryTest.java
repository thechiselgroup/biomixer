package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

public class TreeFactoryTest {

    private TreeFactory underTest;

    private void assertThatChildrenContainsDummyNodes(TreeNode treeNode,
            DummyNodeContainer dummyNodes, int[] dummyNodeNumbers) {
        for (int i = 0; i < dummyNodeNumbers.length; i++) {
            assertThatChildrenContainsNodeItem(treeNode,
                    dummyNodes.getDummyNodeItem(dummyNodeNumbers[i]));
        }
    }

    private void assertThatChildrenContainsNodeItem(TreeNode treeNode,
            NodeItem nodeItem) {
        boolean nodeItemFound = false;
        for (TreeNode child : treeNode.getChildren()) {
            if (child.getNodeItem().equals(nodeItem)) {
                nodeItemFound = true;
                break;
            }
        }
        assertTrue(nodeItemFound);
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

    public int numberOf(List<Tree> trees) {
        return trees.size();
    }

    @Test
    public void oneRootOneChildOneGrandchildTreeTest() {
        DummyNodeContainer dummyNodes = DummyNodeContainer
                .getDummyNodeContainer(3);
        DummyArcContainer dummyArcs = DummyArcContainer
                .getDummyArcContainer(new String[][] { { "0", "1" },
                        { "1", "2" } });

        List<Tree> trees = underTest.getTrees(dummyNodes.getDummyNodeItems(),
                dummyArcs.getDummyArcItems());

        assertThat(numberOf(trees), equalTo(1));
        Tree tree = trees.get(0);
        assertThat(tree.size(), equalTo(3));
        TreeNode root = tree.getRoot();
        assertThat(root.getNumberOfDescendants(), equalTo(2));
        assertThat(root.getChildren().size(), equalTo(1));
        TreeNode rootChild = root.getChildren().get(0);
        assertTrue(rootChild.getNodeItem().equals(
                dummyNodes.getDummyNodeItem(1)));
        assertThat(rootChild.getNumberOfDescendants(), equalTo(1));
        assertThat(rootChild.getChildren().size(), equalTo(1));
        TreeNode rootGrandChild = rootChild.getChildren().get(0);
        assertTrue(rootGrandChild.getNodeItem().equals(
                dummyNodes.getDummyNodeItem(2)));
        assertThat(rootGrandChild.getNumberOfDescendants(), equalTo(0));
        assertThat(rootGrandChild.getChildren().size(), equalTo(0));
    }

    @Test
    public void oneRootOneChildTreeTest() {
        int numberOfNodes = 2;
        DummyNodeContainer dummyNodes = DummyNodeContainer
                .getDummyNodeContainer(numberOfNodes);
        DummyArcContainer dummyArcs = DummyArcContainer
                .getDummyArcContainer(new String[][] { { "0", "1" } });

        List<Tree> trees = underTest.getTrees(dummyNodes.getDummyNodeItems(),
                dummyArcs.getDummyArcItems());
        assertThat(numberOf(trees), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.size(), equalTo(numberOfNodes));
        TreeNode root = tree.getRoot();
        assertTrue(root.getNodeItem().equals(dummyNodes.getDummyNodeItem(0)));
        assertThat(root.getNumberOfDescendants(), equalTo(numberOfNodes - 1));
        assertTrue(root.getChildren().get(0).getNodeItem()
                .equals(dummyNodes.getDummyNodeItem(1)));
    }

    @Test
    public void oneRootThreeChildrenTreeTest() {
        int numberOfNodes = 4;
        DummyNodeContainer dummyNodes = DummyNodeContainer
                .getDummyNodeContainer(numberOfNodes);
        DummyArcContainer dummyArcs = DummyArcContainer
                .getDummyArcContainer(new String[][] { { "0", "1" },
                        { "0", "2" }, { "0", "3" } });

        List<Tree> trees = underTest.getTrees(dummyNodes.getDummyNodeItems(),
                dummyArcs.getDummyArcItems());
        assertThat(numberOf(trees), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.size(), equalTo(numberOfNodes));
        TreeNode root = tree.getRoot();
        assertTrue(root.getNodeItem().equals(dummyNodes.getDummyNodeItem(0)));
        assertThat(root.getNumberOfDescendants(), equalTo(numberOfNodes - 1));
        assertThat(root.getChildren().size(), equalTo(3));
        assertThatChildrenContainsDummyNodes(root, dummyNodes, new int[] { 1,
                2, 3 });
    }

    @Test
    public void oneRootTwoChildrenOneHasThreeChildrenOtherHasNoneTreeTest() {
        int numberOfNodes = 6;
        DummyNodeContainer dummyNodes = DummyNodeContainer
                .getDummyNodeContainer(numberOfNodes);
        DummyArcContainer dummyArcs = DummyArcContainer
                .getDummyArcContainer(new String[][] { { "0", "1" },
                        { "0", "2" }, { "1", "3" }, { "1", "4" }, { "1", "5" } });

        List<Tree> trees = underTest.getTrees(dummyNodes.getDummyNodeItems(),
                dummyArcs.getDummyArcItems());
        assertThat(numberOf(trees), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.size(), equalTo(numberOfNodes));
        TreeNode root = tree.getRoot();
        assertTrue(root.getNodeItem().equals(dummyNodes.getDummyNodeItem(0)));
        assertThat(root.getNumberOfDescendants(), equalTo(numberOfNodes - 1));
        assertThat(root.getChildren().size(), equalTo(2));
        assertThatChildrenContainsNodeItem(root, dummyNodes.getDummyNodeItem(1));
        assertThatChildrenContainsNodeItem(root, dummyNodes.getDummyNodeItem(2));
        TreeNode nodeWithoutChildren = getTreeNodeForNodeItem(
                root.getChildren(), dummyNodes.getDummyNodeItem(2));
        assertThat(nodeWithoutChildren.getNumberOfDescendants(), equalTo(0));
        TreeNode nodeWithChildren = getTreeNodeForNodeItem(root.getChildren(),
                dummyNodes.getDummyNodeItem(1));
        assertThat(nodeWithChildren.getNumberOfDescendants(), equalTo(3));
        assertThat(nodeWithChildren.getChildren().size(), equalTo(3));
        assertThatChildrenContainsDummyNodes(nodeWithChildren, dummyNodes,
                new int[] { 3, 4, 5 });
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.underTest = TreeFactory.getInstance();
    }

    @Test
    public void singleNodeTreeTest() {

        Node dummyNode = mock(Node.class);
        when(dummyNode.getId()).thenReturn("1");

        NodeItem singleNodeItem = mock(NodeItem.class);
        when(singleNodeItem.getNode()).thenReturn(dummyNode);

        NodeItem[] nodes = { singleNodeItem };
        ArcItem[] arcs = {};

        List<Tree> trees = underTest.getTrees(nodes, arcs);

        assertThat(numberOf(trees), equalTo(1));
        assertThat(trees.get(0).size(), equalTo(1));
    }

    @Test
    public void twoRootsNoChildrenTreeTest() {
        int numberOfNodes = 2;
        DummyNodeContainer dummyNodes = DummyNodeContainer
                .getDummyNodeContainer(numberOfNodes);

        List<Tree> trees = underTest.getTrees(dummyNodes.getDummyNodeItems(),
                new ArcItem[] {});
        assertThat(numberOf(trees), equalTo(2));
        assertThat(trees.get(0).size(), equalTo(1));
        assertThat(trees.get(1).size(), equalTo(1));
    }

    @Test
    public void twoRootsOneWithTwoChildrenOneWithOneTreeTest() {
        int numberOfNodes = 5;
        DummyNodeContainer dummyNodes = DummyNodeContainer
                .getDummyNodeContainer(numberOfNodes);
        DummyArcContainer dummyArcs = DummyArcContainer
                .getDummyArcContainer(new String[][] { { "0", "2" },
                        { "0", "3" }, { "1", "4" } });

        List<Tree> trees = underTest.getTrees(dummyNodes.getDummyNodeItems(),
                dummyArcs.getDummyArcItems());
        assertThat(numberOf(trees), equalTo(2));

        Tree tree1 = getTreeWithRootNodeItem(trees,
                dummyNodes.getDummyNodeItem(0));
        assertThat(tree1.size(), equalTo(3));
        assertThatChildrenContainsDummyNodes(tree1.getRoot(), dummyNodes,
                new int[] { 2, 3 });

        Tree tree2 = getTreeWithRootNodeItem(trees,
                dummyNodes.getDummyNodeItem(1));
        assertThat(tree2.size(), equalTo(2));
        assertThatChildrenContainsDummyNodes(tree2.getRoot(), dummyNodes,
                new int[] { 4 });
    }
}
