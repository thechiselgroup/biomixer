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
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutAlgorithmTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.Tree;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.TreeFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.TreeNode;

public class TreeNodeTest extends AbstractLayoutAlgorithmTest {

    private TreeFactory treeFactory = new TreeFactory();

    @Test
    public void getMaxDistanceToChild() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(2);
        createArc(nodes[1], nodes[0]);

        Tree tree = getTree(graph);
        TreeNode treenodes0 = getTreeNode(nodes[0], tree);
        TreeNode treenodes1 = getTreeNode(nodes[1], tree);

        assertThat(treenodes0.getMaxDistance(treenodes1), equalTo(1));
    }

    @Test
    public void getMaxDistanceToCurrentNode() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(1);

        Tree tree = getTree(graph);
        TreeNode treenodes0 = getTreeNode(nodes[0], tree);

        assertThat(treenodes0.getMaxDistance(treenodes0), equalTo(0));
    }

    @Test
    public void getMaxDistanceToNodeWithTwoPaths() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(5);
        createArc(nodes[4], nodes[2]);
        createArc(nodes[4], nodes[3]);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[3], nodes[0]);

        Tree tree = getTree(graph);
        TreeNode treenodes0 = getTreeNode(nodes[0], tree);
        TreeNode treenodes4 = getTreeNode(nodes[4], tree);

        assertThat(treenodes0.getMaxDistance(treenodes4), equalTo(3));
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
