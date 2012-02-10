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

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TreeNodeTest {

    private TreeFactory treeFactory = new TreeFactory();

    @Test
    public void getMaxDistanceToChild() {
        StubGraphStructure stubGraph = new StubGraphStructure(2);
        stubGraph.createArc(0, 1);

        Tree tree = getTree(stubGraph);
        TreeNode node0 = getTreeNodeWithNodeId("0", tree);
        TreeNode node1 = getTreeNodeWithNodeId("1", tree);

        assertThat(node0.getMaxDistance(node1), equalTo(1));
    }

    @Test
    public void getMaxDistanceToCurrentNode() {
        StubGraphStructure stubGraph = new StubGraphStructure(1);

        Tree tree = getTree(stubGraph);
        TreeNode node0 = getTreeNodeWithNodeId("0", tree);

        assertThat(node0.getMaxDistance(node0), equalTo(0));
    }

    @Ignore("TODO: Assert that an assertion error occurs")
    @Test
    public void getMaxDistanceToNodeThatIsNotADescendant() {
        StubGraphStructure stubGraph = new StubGraphStructure(4);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(1, 2);
        stubGraph.createArc(2, 3);

        Tree tree = getTree(stubGraph);
        TreeNode node2 = getTreeNodeWithNodeId("2", tree);
        TreeNode node0 = getTreeNodeWithNodeId("0", tree);

        // -1 is returned if it is not a descendant
        // XXX this should cause an assertion error. Is there a way to test that
        // it does?
        assertThat(node2.getMaxDistance(node0), equalTo(-1));
    }

    @Test
    public void getMaxDistanceToNodeWithTwoPaths() {
        StubGraphStructure stubGraph = new StubGraphStructure(5);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(1, 2);
        stubGraph.createArc(0, 3);
        stubGraph.createArc(2, 4);
        stubGraph.createArc(3, 4);

        Tree tree = getTree(stubGraph);
        TreeNode node0 = getTreeNodeWithNodeId("0", tree);
        TreeNode node4 = getTreeNodeWithNodeId("4", tree);

        assertThat(node0.getMaxDistance(node4), equalTo(3));
    }

    private Tree getTree(StubGraphStructure stubGraph) {
        List<Tree> trees = treeFactory.getTrees(stubGraph.getNodeItems(),
                stubGraph.getArcItems());
        assert trees.size() == 1;
        return trees.get(0);
    }

    private TreeNode getTreeNodeWithNodeId(String nodeId, Tree tree) {
        Set<TreeNode> allNodes = tree.getAllNodes();
        for (TreeNode treeNode : allNodes) {
            if (treeNode.getNodeItem().getNode().getId().equals(nodeId)) {
                return treeNode;
            }
        }
        Assert.fail("There is no node with id '" + nodeId + "' in the tree.");
        return null;
    }

}
