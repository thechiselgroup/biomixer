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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultBoundsDouble;

public abstract class AbstractLayoutAlgorithmTest {

    private double delta = 0.1;

    @Mock
    protected ErrorHandler errorHandler;

    protected TestLayoutGraph graph;

    protected void assertNodeHasCentre(double x, double y, LayoutNode node) {
        SizeDouble nodeSize = node.getSize();
        assertEquals(x, node.getX() + nodeSize.getWidth() / 2, delta);
        assertEquals(y, node.getY() + nodeSize.getHeight() / 2, delta);
    }

    protected void assertNodesHaveCentreX(double x, LayoutNode... nodes) {
        for (LayoutNode layoutNode : nodes) {
            assertEquals(x, getCentreX(layoutNode), delta);
        }
    }

    protected void assertNodesHaveCentreY(double y, LayoutNode... nodes) {
        for (LayoutNode layoutNode : nodes) {
            assertEquals(y, getCentreY(layoutNode), delta);
        }
    }

    protected LayoutArc createArc(int arcType, LayoutNode sourceNode,
            LayoutNode targetNode) {
        return graph.createArc(sourceNode, targetNode, 2, true,
                graph.getTestLayoutArcTypes()[arcType]);
    }

    protected LayoutArc createArc(LayoutNode sourceNode, LayoutNode targetNode) {
        return createArc(0, sourceNode, targetNode);
    }

    protected void createGraph(double leftX, double topY, double width,
            double height) {

        createGraph(leftX, topY, width, height, 1, 1);
    }

    protected void createGraph(double leftX, double topY, double width,
            double height, int numberOfNodeTypes, int numberOfArcTypes) {

        graph = new TestLayoutGraph(new DefaultBoundsDouble(leftX, topY, width,
                height), numberOfNodeTypes, numberOfArcTypes);
    }

    protected TestLayoutNode[] createNodes(int numberOfNodes) {
        return createNodes(0, numberOfNodes);
    }

    protected TestLayoutNode[] createNodes(int nodeType, int numberOfNodes) {
        TestLayoutNodeType testNodeType = graph.getTestLayoutNodeTypes()[nodeType];
        TestLayoutNode[] result = new TestLayoutNode[numberOfNodes];
        for (int i = 0; i < result.length; i++) {
            result[i] = graph.createNode(10, 10, false, testNodeType);
        }
        return result;
    }

    protected double getCentreX(LayoutNode layoutNode) {
        return layoutNode.getX() + layoutNode.getSize().getWidth() / 2;
    }

    protected double getCentreY(LayoutNode layoutNode) {
        return layoutNode.getY() + layoutNode.getSize().getHeight() / 2;
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

}