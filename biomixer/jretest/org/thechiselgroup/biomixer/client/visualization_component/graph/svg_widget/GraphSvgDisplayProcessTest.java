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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * This class tests methods in the GraphSvgDisplay without checking the SVG
 * output generated. For tests that do test SVG output, see
 * GraphSvgDisplayOutputTest.
 * 
 * @author drusk
 * 
 */
public class GraphSvgDisplayProcessTest extends AbstractGraphSvgDisplayTest {

    @Test
    public void addArcShouldContainArc() {
        addNode(N1, LABEL1, TYPE1);
        addNode(N2, LABEL2, TYPE1);
        addArc(A1, N1, N2, TYPE1, true);
        assertTrue(underTest.containsArc(A1));
    }

    @Test
    public void addNodeShouldContainNode() {
        addNode(N1, LABEL1, TYPE1);
        assertTrue(underTest.containsNode(N1));
    }

    private LayoutNode getLayoutNode(String id) {
        return underTest.layoutGraph.getIdentifiableLayoutNode(id);
    }

    @Test
    public void nodeHasExpectedLocation() {
        Node node = addNode(N1, LABEL1, TYPE1);
        Point newLocation = new Point(100, 100);
        underTest.setLocation(node, newLocation);
        assertThat(underTest.getLocation(node), equalTo(newLocation));
    }

    @Test
    public void runLayoutOnNodes() {
        Node node1 = addNode(N1, LABEL1, TYPE1);
        Node node2 = addNode(N2, LABEL1, TYPE1);
        addNode(N3, LABEL1, TYPE1);

        LayoutNode layoutNode1 = getLayoutNode(N1);
        LayoutNode layoutNode2 = getLayoutNode(N2);
        LayoutNode layoutNode3 = getLayoutNode(N3);

        PointDouble node1Before = layoutNode1.getCentre();
        PointDouble node2Before = layoutNode2.getCentre();
        PointDouble node3Before = layoutNode3.getCentre();

        underTest.runLayoutOnNodes(Arrays.asList(node1, node2));

        PointDouble node1After = layoutNode1.getCentre();
        PointDouble node2After = layoutNode2.getCentre();
        PointDouble node3After = layoutNode3.getCentre();

        assertFalse(node1Before.equals(node1After));
        assertFalse(node2Before.equals(node2After));
        assertTrue(node3Before.equals(node3After));
    }
}
