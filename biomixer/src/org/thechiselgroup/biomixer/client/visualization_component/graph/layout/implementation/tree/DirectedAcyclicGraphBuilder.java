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

/**
 * Takes a graph and finds all the separate {@link DirectedAcyclicGraph}s on it.
 * 
 * @author drusk
 * 
 */
public class DirectedAcyclicGraphBuilder {

    /**
     * 
     * @param graph
     *            the graph whose nodes and arcs are to be examined for directed
     *            acyclic graphs
     * @return the distinct {@link DirectedAcyclicGraphs}s on the
     *         {@link LayoutGraph}
     */
    public List<DirectedAcyclicGraph> getDirectedAcyclicGraphs(LayoutGraph graph) {

        Map<LayoutNode, DirectedAcyclicGraphNode> directedAcyclicGraphNodes = new HashMap<LayoutNode, DirectedAcyclicGraphNode>();
        List<DirectedAcyclicGraphNode> potentialRoots = new ArrayList<DirectedAcyclicGraphNode>();
        for (LayoutNode node : graph.getAllNodes()) {
            DirectedAcyclicGraphNode root = new DirectedAcyclicGraphNode(node);
            directedAcyclicGraphNodes.put(node, root);
            potentialRoots.add(root);
        }

        for (LayoutArc arc : graph.getAllArcs()) {
            if (!arc.isDirected()) {
                /*
                 * This is a DIRECTED acyclic graph. For now just ignore
                 * undirected edges.
                 */
                continue;
            }
            // XXX arcs point from child to parent. Therefore sourceNode is a
            // child of targetNode.
            DirectedAcyclicGraphNode sourceNode = directedAcyclicGraphNodes
                    .get(arc.getSourceNode());
            DirectedAcyclicGraphNode targetNode = directedAcyclicGraphNodes
                    .get(arc.getTargetNode());

            targetNode.addChild(sourceNode);
            potentialRoots.remove(sourceNode);
        }

        List<DirectedAcyclicGraph> directedAcyclicGraphs = new ArrayList<DirectedAcyclicGraph>();
        List<List<DirectedAcyclicGraphNode>> rootLists = new ArrayList<List<DirectedAcyclicGraphNode>>();
        List<DirectedAcyclicGraphNode> rootsAlreadyInADirectedAcyclicGraph = new ArrayList<DirectedAcyclicGraphNode>();

        for (int i = 0; i < potentialRoots.size(); i++) {
            DirectedAcyclicGraphNode root1 = potentialRoots.get(i);
            if (rootsAlreadyInADirectedAcyclicGraph.contains(root1)) {
                continue;
            }
            List<DirectedAcyclicGraphNode> rootsInSameDirectedAcyclicGraph = new ArrayList<DirectedAcyclicGraphNode>();
            rootsInSameDirectedAcyclicGraph.add(root1);
            rootsAlreadyInADirectedAcyclicGraph.add(root1);

            for (int j = i + 1; j < potentialRoots.size(); j++) {
                DirectedAcyclicGraphNode root2 = potentialRoots.get(j);
                if (rootsAlreadyInADirectedAcyclicGraph.contains(root2)) {
                    continue;
                }
                Collection<DirectedAcyclicGraphNode> intersection = CollectionUtils
                        .getIntersection(root1.getDescendants(),
                                root2.getDescendants());
                if (intersection.size() > 0) {
                    // there are common descendants
                    rootsInSameDirectedAcyclicGraph.add(root2);
                    rootsAlreadyInADirectedAcyclicGraph.add(root2);
                }

            }
            rootLists.add(rootsInSameDirectedAcyclicGraph);

        }

        for (List<DirectedAcyclicGraphNode> roots : rootLists) {
            directedAcyclicGraphs.add(new DirectedAcyclicGraph(roots));
        }

        return directedAcyclicGraphs;
    }
}
