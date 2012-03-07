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

import java.util.ArrayList;
import java.util.List;

public class TestLayoutGraph implements LayoutGraph {

    private BoundsDouble graphBounds;

    private List<LayoutNodeType> nodeTypes;

    private TestLayoutNodeType[] testLayoutNodeTypes;

    private List<LayoutArcType> arcTypes;

    private TestLayoutArcType[] testLayoutArcTypes;

    public TestLayoutGraph(BoundsDouble graphBounds, int numberOfNodeTypes,
            int numberOfArcTypes) {

        this.nodeTypes = new ArrayList<LayoutNodeType>();
        this.testLayoutNodeTypes = new TestLayoutNodeType[numberOfNodeTypes];
        for (int i = 0; i < numberOfNodeTypes; i++) {
            testLayoutNodeTypes[i] = new TestLayoutNodeType();
            nodeTypes.add(testLayoutNodeTypes[i]);
        }
        this.arcTypes = new ArrayList<LayoutArcType>();
        this.testLayoutArcTypes = new TestLayoutArcType[numberOfArcTypes];
        for (int i = 0; i < numberOfArcTypes; i++) {
            testLayoutArcTypes[i] = new TestLayoutArcType();
            arcTypes.add(testLayoutArcTypes[i]);
        }
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

    public TestLayoutNode createNode(double width, double height,
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
    public List<LayoutArc> getAllArcs() {
        List<LayoutArc> allArcs = new ArrayList<LayoutArc>();
        for (LayoutArcType layoutArcType : arcTypes) {
            allArcs.addAll(layoutArcType.getArcs());
        }
        return allArcs;
    }

    @Override
    public List<LayoutNode> getAllNodes() {
        List<LayoutNode> allNodes = new ArrayList<LayoutNode>();
        for (LayoutNodeType layoutNodeType : nodeTypes) {
            allNodes.addAll(layoutNodeType.getNodes());
        }
        return allNodes;
    }

    @Override
    public List<LayoutArcType> getArcTypes() {
        return arcTypes;
    }

    @Override
    public BoundsDouble getBounds() {
        return graphBounds;
    }

    @Override
    public List<LayoutNodeType> getNodeTypes() {
        return nodeTypes;
    }

    public TestLayoutArcType[] getTestLayoutArcTypes() {
        return testLayoutArcTypes;
    }

    public TestLayoutNodeType[] getTestLayoutNodeTypes() {
        return testLayoutNodeTypes;
    }

}
