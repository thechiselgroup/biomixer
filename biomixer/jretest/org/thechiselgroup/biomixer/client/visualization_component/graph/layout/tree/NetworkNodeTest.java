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
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.DirectedNodeNetwork;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.NetworkBuilder;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.NetworkNode;

public class NetworkNodeTest extends AbstractLayoutAlgorithmTest {

    private NetworkBuilder treeFactory = new NetworkBuilder();

    @Test
    public void getMaxDistanceToChild() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(2);
        createArc(nodes[1], nodes[0]);

        DirectedNodeNetwork network = getNetwork(graph);
        NetworkNode networkNodes0 = getNetworkNode(nodes[0], network);
        NetworkNode networkNodes1 = getNetworkNode(nodes[1], network);

        assertThat(networkNodes0.getMaxDistance(networkNodes1), equalTo(1));
    }

    @Test
    public void getMaxDistanceToCurrentNode() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(1);

        DirectedNodeNetwork network = getNetwork(graph);
        NetworkNode networkNode0 = getNetworkNode(nodes[0], network);

        assertThat(networkNode0.getMaxDistance(networkNode0), equalTo(0));
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

        DirectedNodeNetwork network = getNetwork(graph);
        NetworkNode networkNode0 = getNetworkNode(nodes[0], network);
        NetworkNode networkNode4 = getNetworkNode(nodes[4], network);

        assertThat(networkNode0.getMaxDistance(networkNode4), equalTo(3));
    }

    private DirectedNodeNetwork getNetwork(LayoutGraph graph) {
        List<DirectedNodeNetwork> networks = treeFactory.getNetworks(graph);
        assert networks.size() == 1;
        return networks.get(0);
    }

    private NetworkNode getNetworkNode(LayoutNode node, DirectedNodeNetwork network) {
        Set<NetworkNode> allNodes = network.getAllNodes();
        for (NetworkNode networkNode : allNodes) {
            if (networkNode.getLayoutNode().equals(node)) {
                return networkNode;
            }
        }
        Assert.fail();
        return null;
    }

}
