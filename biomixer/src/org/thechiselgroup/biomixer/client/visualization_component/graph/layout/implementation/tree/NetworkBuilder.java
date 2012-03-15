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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.LayoutNodeAnimationWrapper;

/**
 * Takes a graph and finds all the separate {@link DirectedNodeNetwork}s on it.
 * 
 * @author drusk
 * 
 */
public class NetworkBuilder {

    /**
     * 
     * @param graph
     *            the graph whose nodes and arcs are to be examined for networks
     * @return the distinct networks on the graph
     */
    public List<DirectedNodeNetwork> getNetworks(LayoutGraph graph) {

        Map<LayoutNode, NetworkNode> networkNodes = new HashMap<LayoutNode, NetworkNode>();
        List<NetworkNode> potentialRoots = new ArrayList<NetworkNode>();
        for (LayoutNode node : graph.getAllNodes()) {
            NetworkNode root = new NetworkNode(node);
            // XXX how can I properly retrieve a source/target node from an arc
            // if I am wrapping them?
            if (node instanceof LayoutNodeAnimationWrapper) {
                networkNodes.put(
                        ((LayoutNodeAnimationWrapper) node).getWrappedNode(),
                        root);
            } else {
                networkNodes.put(node, root);
            }
            potentialRoots.add(root);
        }

        for (LayoutArc arc : graph.getAllArcs()) {
            // XXX arcs point from child to parent. Therefore sourceNode is a
            // child of targetNode.
            NetworkNode sourceNode = networkNodes.get(arc.getSourceNode());
            NetworkNode targetNode = networkNodes.get(arc.getTargetNode());

            targetNode.addChild(sourceNode);
            potentialRoots.remove(sourceNode);
        }

        List<DirectedNodeNetwork> networks = new ArrayList<DirectedNodeNetwork>();
        List<List<NetworkNode>> networkRootLists = new ArrayList<List<NetworkNode>>();
        List<NetworkNode> rootsAlreadyInANetwork = new ArrayList<NetworkNode>();

        for (int i = 0; i < potentialRoots.size(); i++) {
            NetworkNode root1 = potentialRoots.get(i);
            if (rootsAlreadyInANetwork.contains(root1)) {
                continue;
            }
            List<NetworkNode> rootsInSameTree = new ArrayList<NetworkNode>();
            rootsInSameTree.add(root1);
            rootsAlreadyInANetwork.add(root1);

            for (int j = i + 1; j < potentialRoots.size(); j++) {
                NetworkNode root2 = potentialRoots.get(j);
                if (rootsAlreadyInANetwork.contains(root2)) {
                    continue;
                }
                Collection<NetworkNode> intersection = CollectionUtils
                        .getIntersection(root1.getDescendants(),
                                root2.getDescendants());
                if (intersection.size() > 0) {
                    // there are common descendants
                    rootsInSameTree.add(root2);
                    rootsAlreadyInANetwork.add(root2);
                }

            }
            networkRootLists.add(rootsInSameTree);

        }

        for (List<NetworkNode> roots : networkRootLists) {
            networks.add(new DirectedNodeNetwork(roots));
        }

        return networks;
    }
}
