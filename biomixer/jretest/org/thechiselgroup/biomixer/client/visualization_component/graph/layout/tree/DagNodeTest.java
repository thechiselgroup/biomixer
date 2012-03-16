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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutAlgorithmTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DirectedAcyclicGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DagBuilder;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DagNode;

public class DagNodeTest extends AbstractLayoutAlgorithmTest {

    private DagBuilder dagBuilder = new DagBuilder();

    @Test
    public void getMaxDistanceToChild() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(2);
        createArc(nodes[1], nodes[0]);

        DirectedAcyclicGraph dag = getDag(graph);
        DagNode dagNodes0 = getDagNode(nodes[0], dag);
        DagNode dagNodes1 = getDagNode(nodes[1], dag);

        assertThat(dagNodes0.getMaxDistance(dagNodes1), equalTo(1));
    }

    @Test
    public void getMaxDistanceToCurrentNode() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(1);

        DirectedAcyclicGraph dag = getDag(graph);
        DagNode dagNode0 = getDagNode(nodes[0], dag);

        assertThat(dagNode0.getMaxDistance(dagNode0), equalTo(0));
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

        DirectedAcyclicGraph dag = getDag(graph);
        DagNode dagNode0 = getDagNode(nodes[0], dag);
        DagNode dagNode4 = getDagNode(nodes[4], dag);

        assertThat(dagNode0.getMaxDistance(dagNode4), equalTo(3));
    }

    private DirectedAcyclicGraph getDag(LayoutGraph graph) {
        List<DirectedAcyclicGraph> dags = dagBuilder.getDags(graph);
        assert dags.size() == 1;
        return dags.get(0);
    }

    private DagNode getDagNode(LayoutNode node, DirectedAcyclicGraph dag) {
        Set<DagNode> allNodes = dag.getAllNodes();
        for (DagNode dagNode : allNodes) {
            if (dagNode.getLayoutNode().equals(node)) {
                return dagNode;
            }
        }
        Assert.fail();
        return null;
    }

}
