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

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.Tree;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.TreeFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.TreeNode;

public class TreeNodeTest extends AbstractLayoutAlgorithmTest {

    private TreeFactory treeFactory = new TreeFactory();

    @Test
    public void getMaxDistanceToChild() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        createDefaultArc(graph, node2, node1);

        Tree tree = getTree(graph);
        TreeNode treeNode1 = getTreeNode(node1, tree);
        TreeNode treeNode2 = getTreeNode(node2, tree);

        assertThat(treeNode1.getMaxDistance(treeNode2), equalTo(1));
    }

    @Test
    public void getMaxDistanceToCurrentNode() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);

        Tree tree = getTree(graph);
        TreeNode treeNode1 = getTreeNode(node1, tree);

        assertThat(treeNode1.getMaxDistance(treeNode1), equalTo(0));
    }

    @Test
    public void getMaxDistanceToNodeWithTwoPaths() {
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

        Tree tree = getTree(graph);
        TreeNode treeNode1 = getTreeNode(node1, tree);
        TreeNode treeNode5 = getTreeNode(node5, tree);

        assertThat(treeNode1.getMaxDistance(treeNode5), equalTo(3));
    }

    private Tree getTree(LayoutGraph graph) {
        List<Tree> trees = treeFactory.getTrees(graph);
        assert trees.size() == 1;
        return trees.get(0);
    }

    private TreeNode getTreeNode(LayoutNode node, Tree tree) {
        Set<TreeNode> allNodes = tree.getAllNodes();
        for (TreeNode treeNode : allNodes) {
            if (treeNode.getLayoutNode().equals(node)) {
                return treeNode;
            }
        }
        Assert.fail();
        return null;
    }

}
