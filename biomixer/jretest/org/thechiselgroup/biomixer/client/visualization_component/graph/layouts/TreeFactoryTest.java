package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.thechiselgroup.biomixer.client.visualization_component.graph.layouts.TreeNodeMatcher.equalsTree;

import java.util.List;

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
                // TODO return immediately
                break;
            }
        }

        // TODO Assert.fail() instead, with return null
        assert desiredTree != null;
        return desiredTree;
    }

    @Test
    public void oneRootOneChild() {
        int numberOfNodes = 2; // TODO inline
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);
        stubGraph.createArc(0, 1);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(tree.getRoot(), equalsTree(0, 1, equalsTree(1, 0)));
        assertThat(tree.getHeight(), equalTo(2));
    }

    @Test
    public void oneRootOneChildOneGrandchild() {
        int numberOfNodes = 3; // TODO inline
        StubGraphStructure stubGraph = new StubGraphStructure(numberOfNodes);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(1, 2);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());

        assertThat(trees.size(), equalTo(1));
        Tree tree = trees.get(0);
        // TODO add suppresswarnings
        assertThat(tree.getRoot(),
                equalsTree(0, 2, equalsTree(1, 1, equalsTree(2, 0))));
        assertThat(tree.getHeight(), equalTo(3));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootThreeChildren() {
        StubGraphStructure stubGraph = new StubGraphStructure(4);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(0, 3);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(1));

        Tree tree = trees.get(0);
        assertThat(
                tree.getRoot(),
                equalsTree(0, 3, equalsTree(1, 0), equalsTree(2, 0),
                        equalsTree(3, 0)));
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
        assertThat(
                tree.getRoot(),
                equalsTree(
                        0,
                        5,
                        equalsTree(1, 3, equalsTree(3, 0), equalsTree(4, 0),
                                equalsTree(5, 0)), equalsTree(2, 0)));

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
        for (TreeNode child : children) {
            if (child.getNodeItem().equals(nodeItem)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void twoRootsNoChildren() {
        StubGraphStructure stubGraph = new StubGraphStructure(2);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(2));
        assertThat(trees.get(0).size(), equalTo(1));
        assertThat(trees.get(1).size(), equalTo(1));
    }

    @Test
    public void twoRootsOneWithTwoChildrenOneWithOne() {
        StubGraphStructure stubGraph = new StubGraphStructure(5);
        stubGraph.createArc(0, 2);
        stubGraph.createArc(0, 3);
        stubGraph.createArc(1, 4);

        List<Tree> trees = underTest.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assertThat(trees.size(), equalTo(2));

        Tree tree1 = getTreeWithRootNodeItem(trees, stubGraph.getNodeItem(0));
        assertThat(tree1.getHeight(), equalTo(2));
        assertThat(tree1.getRoot(),
                equalsTree(0, 2, equalsTree(2, 0), equalsTree(3, 0)));

        Tree tree2 = getTreeWithRootNodeItem(trees, stubGraph.getNodeItem(1));
        assertThat(tree2.getRoot(), equalsTree(1, 1, equalsTree(4, 0)));
        assertThat(tree2.getHeight(), equalTo(2));
    }

}
