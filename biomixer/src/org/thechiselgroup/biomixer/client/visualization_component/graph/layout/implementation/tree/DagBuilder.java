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
public class DagBuilder {

    /**
     * 
     * @param graph
     *            the graph whose nodes and arcs are to be examined for directed
     *            acyclic graphs
     * @return the distinct {@link DirectedAcyclicGraphs}s on the
     *         {@link LayoutGraph}
     */
    public List<DirectedAcyclicGraph> getDags(LayoutGraph graph) {

        Map<LayoutNode, DagNode> dagNodes = new HashMap<LayoutNode, DagNode>();
        List<DagNode> potentialRoots = new ArrayList<DagNode>();
        for (LayoutNode node : graph.getAllNodes()) {
            DagNode root = new DagNode(node);
            dagNodes.put(node, root);
            potentialRoots.add(root);
        }

        for (LayoutArc arc : graph.getAllArcs()) {
            // XXX arcs point from child to parent. Therefore sourceNode is a
            // child of targetNode.
            DagNode sourceNode = dagNodes.get(arc.getSourceNode());
            DagNode targetNode = dagNodes.get(arc.getTargetNode());

            targetNode.addChild(sourceNode);
            potentialRoots.remove(sourceNode);
        }

        List<DirectedAcyclicGraph> dags = new ArrayList<DirectedAcyclicGraph>();
        List<List<DagNode>> dagRootLists = new ArrayList<List<DagNode>>();
        List<DagNode> rootsAlreadyInADag = new ArrayList<DagNode>();

        for (int i = 0; i < potentialRoots.size(); i++) {
            DagNode root1 = potentialRoots.get(i);
            if (rootsAlreadyInADag.contains(root1)) {
                continue;
            }
            List<DagNode> rootsInSameDag = new ArrayList<DagNode>();
            rootsInSameDag.add(root1);
            rootsAlreadyInADag.add(root1);

            for (int j = i + 1; j < potentialRoots.size(); j++) {
                DagNode root2 = potentialRoots.get(j);
                if (rootsAlreadyInADag.contains(root2)) {
                    continue;
                }
                Collection<DagNode> intersection = CollectionUtils
                        .getIntersection(root1.getDescendants(),
                                root2.getDescendants());
                if (intersection.size() > 0) {
                    // there are common descendants
                    rootsInSameDag.add(root2);
                    rootsAlreadyInADag.add(root2);
                }

            }
            dagRootLists.add(rootsInSameDag);

        }

        for (List<DagNode> roots : dagRootLists) {
            dags.add(new DirectedAcyclicGraph(roots));
        }

        return dags;
    }
}
