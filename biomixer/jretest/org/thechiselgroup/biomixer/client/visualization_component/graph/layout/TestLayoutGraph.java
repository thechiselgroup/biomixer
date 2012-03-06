/*******************************************************************************
 * Copyright 2012 David Rusk, Lars Grammel 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout;

import java.util.HashSet;
import java.util.Set;

public class TestLayoutGraph implements LayoutGraph {

    private BoundsDouble graphBounds;

    private Set<LayoutNodeType> nodeTypes;

    private Set<LayoutArcType> arcTypes;

    public TestLayoutGraph(BoundsDouble graphBounds) {
        this(new HashSet<LayoutNodeType>(), new HashSet<LayoutArcType>(),
                graphBounds);
    }

    public TestLayoutGraph(Set<LayoutNodeType> nodeTypes,
            Set<LayoutArcType> arcTypes, BoundsDouble graphBounds) {
        this.nodeTypes = nodeTypes;
        this.arcTypes = arcTypes;
        this.graphBounds = graphBounds;
    }

    public LayoutArc createArc(LayoutNode sourceNode, LayoutNode targetNode,
            double thickness, boolean isDirected, TestLayoutArcType type) {
        assert getAllNodes().contains(sourceNode);
        assert getAllNodes().contains(sourceNode);
        TestLayoutArc arc = new TestLayoutArc(sourceNode, targetNode,
                thickness, isDirected, type);
        type.add(arc);
        if (!arcTypes.contains(type)) {
            arcTypes.add(type);
        }
        return arc;
    }

    public LayoutNode createNode(double width, double height,
            boolean isAnchored, TestLayoutNodeType type) {
        TestLayoutNode node = new TestLayoutNode(width, height, isAnchored,
                type);
        type.add(node);
        if (!nodeTypes.contains(type)) {
            nodeTypes.add(type);
        }
        return node;
    }

    @Override
    public Set<LayoutArc> getAllArcs() {
        Set<LayoutArc> allArcs = new HashSet<LayoutArc>();
        for (LayoutArcType layoutArcType : arcTypes) {
            allArcs.addAll(layoutArcType.getArcs());
        }
        return allArcs;
    }

    @Override
    public Set<LayoutNode> getAllNodes() {
        Set<LayoutNode> allNodes = new HashSet<LayoutNode>();
        for (LayoutNodeType layoutNodeType : nodeTypes) {
            allNodes.addAll(layoutNodeType.getNodes());
        }
        return allNodes;
    }

    @Override
    public Set<LayoutArcType> getArcTypes() {
        return arcTypes;
    }

    @Override
    public BoundsDouble getBounds() {
        return graphBounds;
    }

    @Override
    public Set<LayoutNodeType> getNodeTypes() {
        return nodeTypes;
    }

}
