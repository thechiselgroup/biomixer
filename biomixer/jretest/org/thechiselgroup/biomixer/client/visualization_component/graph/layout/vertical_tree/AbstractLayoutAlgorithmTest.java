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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.vertical_tree;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultBoundsDouble;

public class AbstractLayoutAlgorithmTest {

    @Mock
    protected ErrorHandler errorHandler;

    protected BoundsDouble graphBounds;

    protected TestLayoutNodeType nodeType1 = new TestLayoutNodeType();

    protected TestLayoutArcType arcType1 = new TestLayoutArcType();

    public AbstractLayoutAlgorithmTest() {
        super();
    }

    protected void assertNodeHasCentre(double x, double y, LayoutNode node) {
        SizeDouble nodeSize = node.getSize();
        assertThat(node.getX() + nodeSize.getWidth() / 2, equalTo(x));
        assertThat(node.getY() + nodeSize.getHeight() / 2, equalTo(y));
    }

    protected void assertNodesHaveCentreX(double x, LayoutNode... nodes) {
        for (LayoutNode layoutNode : nodes) {
            assertThat(getCentreX(layoutNode), equalTo(x));
        }
    }

    protected void assertNodesHaveCentreY(double y, LayoutNode... nodes) {
        for (LayoutNode layoutNode : nodes) {
            assertThat(getCentreY(layoutNode), equalTo(y));
        }
    }

    protected LayoutArc createDefaultArc(TestLayoutGraph graph,
            LayoutNode sourceNode, LayoutNode targetNode) {
        return graph.createArc(sourceNode, targetNode, 2, true, arcType1);
    }

    protected LayoutNode createDefaultNode(TestLayoutGraph graph) {
        return graph.createNode(10, 10, false, nodeType1);
    }

    protected TestLayoutGraph createGraph(double leftX, double topY,
            double width, double height) {
        setBounds(leftX, topY, width, height);

        TestLayoutGraph graph = new TestLayoutGraph(graphBounds);
        return graph;
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

    protected void setBounds(double leftX, double topY, double width,
            double height) {
        graphBounds = new DefaultBoundsDouble(leftX, topY, width, height);
    }

}