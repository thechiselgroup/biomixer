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
import static org.thechiselgroup.biomixer.client.visualization_component.graph.layout.tree.NetworkNodeMatcher.equalsNetwork;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutAlgorithmTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DirectedNodeNetwork;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.NetworkBuilder;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.NetworkNode;

public class NetworkBuilderTest extends AbstractLayoutAlgorithmTest {

    private NetworkBuilder underTest;

    private NetworkNode getNetworkNode(LayoutNode layoutNode,
            DirectedNodeNetwork network) {
        for (NetworkNode networkNode : network.getAllNodes()) {
            if (networkNode.getLayoutNode().equals(layoutNode)) {
                return networkNode;
            }
        }
        Assert.fail();
        return null;
    }

    private DirectedNodeNetwork getNetworkWithRootNode(
            List<DirectedNodeNetwork> trees, LayoutNode node) {
        for (DirectedNodeNetwork tree : trees) {
            for (NetworkNode root : tree.getRoots()) {
                if (root.getLayoutNode().equals(node)) {
                    return tree;
                }
            }
        }
        Assert.fail();
        return null;
    }

    private List<LayoutNode> getNodesAtDistance(DirectedNodeNetwork network,
            int distance) {
        List<LayoutNode> layoutNodes = new ArrayList<LayoutNode>();
        for (NetworkNode networkNode : network
                .getNodesAtDistanceFromRoot(distance)) {
            layoutNodes.add(networkNode.getLayoutNode());
        }
        return layoutNodes;
    }

    @Test
    public void getNodesAtDistanceTest() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(6);
        createArc(nodes[5], nodes[1]);
        createArc(nodes[4], nodes[1]);
        createArc(nodes[3], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[2], nodes[0]);

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(1));

        DirectedNodeNetwork network = networks.get(0);

        assertThat(getNodesAtDistance(network, 0), containsExactly(nodes[0]));
        assertThat(getNodesAtDistance(network, 1),
                containsExactly(nodes[1], nodes[2]));
        assertThat(getNodesAtDistance(network, 2),
                containsExactly(nodes[3], nodes[4], nodes[5]));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneNetworkTwoRoots() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(6);
        // nodes 0 and 3 are the roots
        createArc(nodes[1], nodes[0]);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[2], nodes[3]);
        createArc(nodes[4], nodes[2]);
        createArc(nodes[5], nodes[2]);

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(1));

        DirectedNodeNetwork network = networks.get(0);
        assertThat(network.getNumberOfNodesOnLongestPath(), equalTo(4));
        assertThat(network.getRoots().size(), equalTo(2));
        // TODO update matcher so more general networks can be matched
        // current have to match "tree" based at each root, and there can be a
        // lot of overlap
        assertThat(
                getNetworkNode(nodes[0], network),
                equalsNetwork(
                        nodes[0],
                        4,
                        equalsNetwork(
                                nodes[1],
                                3,
                                equalsNetwork(nodes[2], 2,
                                        equalsNetwork(nodes[4], 0),
                                        equalsNetwork(nodes[5], 0)))));
        assertThat(
                getNetworkNode(nodes[3], network),
                equalsNetwork(
                        nodes[3],
                        3,
                        equalsNetwork(nodes[2], 2, equalsNetwork(nodes[4], 0),
                                equalsNetwork(nodes[5], 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneNetworkWithNodeThatHasTwoParents() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(5);
        createArc(nodes[4], nodes[2]);
        createArc(nodes[4], nodes[3]);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[3], nodes[0]);

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(1));
        DirectedNodeNetwork network = networks.get(0);
        assertThat(
                network.getRoots().get(0),
                equalsNetwork(
                        nodes[0],
                        4,
                        equalsNetwork(
                                nodes[1],
                                2,
                                equalsNetwork(nodes[2], 1,
                                        equalsNetwork(nodes[4], 0))),
                        equalsNetwork(nodes[3], 1, equalsNetwork(nodes[4], 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootOneChild() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(2);
        createArc(nodes[1], nodes[0]);

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(1));

        DirectedNodeNetwork network = networks.get(0);
        assertThat(network.getNumberOfNodesOnLongestPath(), equalTo(2));
        List<NetworkNode> roots = network.getRoots();
        assertThat(roots.size(), equalTo(1));
        assertThat(roots.get(0),
                equalsNetwork(nodes[0], 1, equalsNetwork(nodes[1], 0)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootOneChildOneGrandchild() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(3);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[1], nodes[0]);

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(1));

        DirectedNodeNetwork network = networks.get(0);
        assertThat(network.getNumberOfNodesOnLongestPath(), equalTo(3));
        assertThat(
                network.getRoots().get(0),
                equalsNetwork(nodes[0], 2,
                        equalsNetwork(nodes[1], 1, equalsNetwork(nodes[2], 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneRootThreeChildren() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(4);
        createArc(nodes[3], nodes[0]);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[1], nodes[0]);

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(1));

        DirectedNodeNetwork network = networks.get(0);
        assertThat(network.getNumberOfNodesOnLongestPath(), equalTo(2));
        assertThat(
                network.getRoots().get(0),
                equalsNetwork(nodes[0], 3, equalsNetwork(nodes[1], 0),
                        equalsNetwork(nodes[2], 0), equalsNetwork(nodes[3], 0)));
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

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(1));

        DirectedNodeNetwork network = networks.get(0);
        assertThat(network.getNumberOfNodesOnLongestPath(), equalTo(3));
        assertThat(
                network.getRoots().get(0),
                equalsNetwork(
                        nodes[0],
                        5,
                        equalsNetwork(nodes[1], 3, equalsNetwork(nodes[3], 0),
                                equalsNetwork(nodes[4], 0),
                                equalsNetwork(nodes[5], 0)),
                        equalsNetwork(nodes[2], 0)));
    }

    @Before
    public void setUp() {
        this.underTest = new NetworkBuilder();
    }

    @Test
    public void singleNode() {
        createGraph(0, 0, 400, 400);
        createNodes(1);

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);

        assertThat(networks.size(), equalTo(1));
        DirectedNodeNetwork network = networks.get(0);
        assertThat(network.getNumberOfNodesOnLongestPath(), equalTo(1));
        assertThat(network.getNumberOfNodes(), equalTo(1));
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

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(1));

        DirectedNodeNetwork network = networks.get(0);

        assertThat(getNodesAtDistance(network, 0), containsExactly(nodes[0]));
        assertThat(getNodesAtDistance(network, 1),
                containsExactly(nodes[1], nodes[3]));
        assertThat(getNodesAtDistance(network, 2), containsExactly(nodes[2]));
        assertThat(getNodesAtDistance(network, 3), containsExactly(nodes[4]));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void twoRootsInTwoNetworksOneWithTwoChildrenOneWithOne() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(5);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[3], nodes[0]);
        createArc(nodes[4], nodes[1]);

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(2));

        DirectedNodeNetwork network1 = getNetworkWithRootNode(networks,
                nodes[0]);
        assertThat(network1.getNumberOfNodesOnLongestPath(), equalTo(2));
        assertThat(
                network1.getRoots().get(0),
                equalsNetwork(nodes[0], 2, equalsNetwork(nodes[2], 0),
                        equalsNetwork(nodes[3], 0)));

        DirectedNodeNetwork network2 = getNetworkWithRootNode(networks,
                nodes[1]);
        assertThat(network2.getNumberOfNodesOnLongestPath(), equalTo(2));
        assertThat(network2.getRoots().get(0),
                equalsNetwork(nodes[1], 1, equalsNetwork(nodes[4], 0)));
    }

    @Test
    public void twoRootsNoChildren() {
        createGraph(0, 0, 400, 400);
        createNodes(2);

        List<DirectedNodeNetwork> networks = underTest.getNetworks(graph);
        assertThat(networks.size(), equalTo(2));
        assertThat(networks.get(0).getNumberOfNodes(), equalTo(1));
        assertThat(networks.get(1).getNumberOfNodes(), equalTo(1));
    }

}
