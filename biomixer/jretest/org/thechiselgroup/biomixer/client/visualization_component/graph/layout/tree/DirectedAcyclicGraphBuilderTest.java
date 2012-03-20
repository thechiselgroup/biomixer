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
import static org.thechiselgroup.biomixer.client.visualization_component.graph.layout.tree.DirectedAcyclicGraphNodeMatcher.equalsDag;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutGraphTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DirectedAcyclicGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DirectedAcyclicGraphBuilder;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DirectedAcyclicGraphNode;

public class DirectedAcyclicGraphBuilderTest extends AbstractLayoutGraphTest {

    private DirectedAcyclicGraphBuilder underTest;

    @Test
    public void dagSizeTestRootsShareAChildNode() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(5);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[3], nodes[0]);
        createArc(nodes[3], nodes[1]);
        createArc(nodes[4], nodes[1]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(1));

        DirectedAcyclicGraph dag = dags.get(0);
        assertThat(dag.getNumberOfNodes(), equalTo(5));
    }

    private DirectedAcyclicGraphNode getDagNode(LayoutNode layoutNode,
            DirectedAcyclicGraph dag) {
        for (DirectedAcyclicGraphNode dagNode : dag.getAllNodes()) {
            if (dagNode.getLayoutNode().equals(layoutNode)) {
                return dagNode;
            }
        }
        Assert.fail();
        return null;
    }

    private DirectedAcyclicGraph getDagWithRootNode(
            List<DirectedAcyclicGraph> dags, LayoutNode node) {
        for (DirectedAcyclicGraph dag : dags) {
            for (DirectedAcyclicGraphNode root : dag.getRoots()) {
                if (root.getLayoutNode().equals(node)) {
                    return dag;
                }
            }
        }
        Assert.fail();
        return null;
    }

    private List<LayoutNode> getNodesAtDistance(DirectedAcyclicGraph dag,
            int distance) {
        List<LayoutNode> layoutNodes = new ArrayList<LayoutNode>();
        for (DirectedAcyclicGraphNode dagNode : dag
                .getNodesAtDistanceFromRoot(distance)) {
            layoutNodes.add(dagNode.getLayoutNode());
        }
        return layoutNodes;
    }

    @Test
    public void getNodesAtDistanceTest() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(6);
        createArc(nodes[5], nodes[1]);
        createArc(nodes[4], nodes[1]);
        createArc(nodes[3], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[2], nodes[0]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(1));

        DirectedAcyclicGraph dag = dags.get(0);

        assertThat(getNodesAtDistance(dag, 0),
                containsExactly((LayoutNode) nodes[0]));
        assertThat(getNodesAtDistance(dag, 1),
                containsExactly((LayoutNode) nodes[1], (LayoutNode) nodes[2]));
        assertThat(
                getNodesAtDistance(dag, 2),
                containsExactly((LayoutNode) nodes[3], (LayoutNode) nodes[4],
                        (LayoutNode) nodes[5]));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneDagTwoRoots() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(6);
        // nodes 0 and 3 are the roots
        createArc(nodes[1], nodes[0]);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[2], nodes[3]);
        createArc(nodes[4], nodes[2]);
        createArc(nodes[5], nodes[2]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(1));

        DirectedAcyclicGraph dag = dags.get(0);
        assertThat(dag.getNumberOfNodesOnLongestPath(), equalTo(4));
        assertThat(dag.getRoots().size(), equalTo(2));
        /*
         * TODO update matcher so more general graph structures can be matched.
         * Currently have to match "tree" based at each root, and there can be a
         * lot of overlap
         */
        assertThat(
                getDagNode(nodes[0], dag),
                equalsDag(
                        nodes[0],
                        4,
                        equalsDag(
                                nodes[1],
                                3,
                                equalsDag(nodes[2], 2, equalsDag(nodes[4], 0),
                                        equalsDag(nodes[5], 0)))));
        assertThat(
                getDagNode(nodes[3], dag),
                equalsDag(
                        nodes[3],
                        3,
                        equalsDag(nodes[2], 2, equalsDag(nodes[4], 0),
                                equalsDag(nodes[5], 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneDagWithNodeThatHasTwoParents() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(5);
        createArc(nodes[4], nodes[2]);
        createArc(nodes[4], nodes[3]);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[3], nodes[0]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(1));
        DirectedAcyclicGraph dag = dags.get(0);
        assertThat(
                dag.getRoots().get(0),
                equalsDag(
                        nodes[0],
                        4,
                        equalsDag(nodes[1], 2,
                                equalsDag(nodes[2], 1, equalsDag(nodes[4], 0))),
                        equalsDag(nodes[3], 1, equalsDag(nodes[4], 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootOneChild() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(2);
        createArc(nodes[1], nodes[0]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(1));

        DirectedAcyclicGraph dag = dags.get(0);
        assertThat(dag.getNumberOfNodesOnLongestPath(), equalTo(2));
        List<DirectedAcyclicGraphNode> roots = dag.getRoots();
        assertThat(roots.size(), equalTo(1));
        assertThat(roots.get(0), equalsDag(nodes[0], 1, equalsDag(nodes[1], 0)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootOneChildOneGrandchild() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(3);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[1], nodes[0]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(1));

        DirectedAcyclicGraph dag = dags.get(0);
        assertThat(dag.getNumberOfNodesOnLongestPath(), equalTo(3));
        assertThat(
                dag.getRoots().get(0),
                equalsDag(nodes[0], 2,
                        equalsDag(nodes[1], 1, equalsDag(nodes[2], 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootThreeChildren() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(4);
        createArc(nodes[3], nodes[0]);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[1], nodes[0]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(1));

        DirectedAcyclicGraph dag = dags.get(0);
        assertThat(dag.getNumberOfNodesOnLongestPath(), equalTo(2));
        assertThat(
                dag.getRoots().get(0),
                equalsDag(nodes[0], 3, equalsDag(nodes[1], 0),
                        equalsDag(nodes[2], 0), equalsDag(nodes[3], 0)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootTwoChildrenOneHasThreeChildrenOtherHasNone() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(6);
        createArc(nodes[3], nodes[1]);
        createArc(nodes[4], nodes[1]);
        createArc(nodes[5], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[2], nodes[0]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(1));

        DirectedAcyclicGraph dag = dags.get(0);
        assertThat(dag.getNumberOfNodesOnLongestPath(), equalTo(3));
        assertThat(
                dag.getRoots().get(0),
                equalsDag(
                        nodes[0],
                        5,
                        equalsDag(nodes[1], 3, equalsDag(nodes[3], 0),
                                equalsDag(nodes[4], 0), equalsDag(nodes[5], 0)),
                        equalsDag(nodes[2], 0)));
    }

    @Before
    public void setUp() {
        this.underTest = new DirectedAcyclicGraphBuilder();
    }

    @Test
    public void singleNode() {
        createGraph(0, 0, 400, 400);
        createNodes(1);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);

        assertThat(dags.size(), equalTo(1));
        DirectedAcyclicGraph dag = dags.get(0);
        assertThat(dag.getNumberOfNodesOnLongestPath(), equalTo(1));
        assertThat(dag.getNumberOfNodes(), equalTo(1));
    }

    @Test
    public void twoParentsDifferentLengthPathsDepthTest() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(5);

        createArc(nodes[4], nodes[2]);
        createArc(nodes[4], nodes[3]);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[3], nodes[0]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(1));

        DirectedAcyclicGraph dag = dags.get(0);

        assertThat(getNodesAtDistance(dag, 0),
                containsExactly((LayoutNode) nodes[0]));
        assertThat(getNodesAtDistance(dag, 1),
                containsExactly((LayoutNode) nodes[1], (LayoutNode) nodes[3]));
        assertThat(getNodesAtDistance(dag, 2),
                containsExactly((LayoutNode) nodes[2]));
        assertThat(getNodesAtDistance(dag, 3),
                containsExactly((LayoutNode) nodes[4]));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void twoRootsInTwoDagsOneWithTwoChildrenOneWithOne() {
        createGraph(0, 0, 400, 400);
        TestLayoutNode[] nodes = createNodes(5);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[3], nodes[0]);
        createArc(nodes[4], nodes[1]);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(2));

        DirectedAcyclicGraph dag1 = getDagWithRootNode(dags, nodes[0]);
        assertThat(dag1.getNumberOfNodesOnLongestPath(), equalTo(2));
        assertThat(
                dag1.getRoots().get(0),
                equalsDag(nodes[0], 2, equalsDag(nodes[2], 0),
                        equalsDag(nodes[3], 0)));

        DirectedAcyclicGraph dag2 = getDagWithRootNode(dags, nodes[1]);
        assertThat(dag2.getNumberOfNodesOnLongestPath(), equalTo(2));
        assertThat(dag2.getRoots().get(0),
                equalsDag(nodes[1], 1, equalsDag(nodes[4], 0)));
    }

    @Test
    public void twoRootsNoChildren() {
        createGraph(0, 0, 400, 400);
        createNodes(2);

        List<DirectedAcyclicGraph> dags = underTest
                .getDirectedAcyclicGraphs(graph);
        assertThat(dags.size(), equalTo(2));
        assertThat(dags.get(0).getNumberOfNodes(), equalTo(1));
        assertThat(dags.get(1).getNumberOfNodes(), equalTo(1));
    }

}
