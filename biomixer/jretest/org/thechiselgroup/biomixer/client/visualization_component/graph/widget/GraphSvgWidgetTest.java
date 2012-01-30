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
package org.thechiselgroup.biomixer.client.visualization_component.graph.widget;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.svg.AbstractSvgTest;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElementFactory;

public class GraphSvgWidgetTest extends AbstractSvgTest {

    private static final String TYPE = "type";

    private static final String LABEL1 = "Concept1";

    private static final String LABEL2 = "Concept2";

    private static final String ID1 = "id1";

    private static final String ID2 = "id2";

    private static final String ARC_ID1 = "aid1";

    private GraphSvgWidget underTest;

    private void addArc(String arcId, String sourceNodeId, String targetNodeId,
            String type, boolean directed) {
        underTest.addArc(new Arc(arcId, sourceNodeId, targetNodeId, type,
                directed));
    }

    @Test
    public void addArcShouldContainArc() {
        addNode(ID1, LABEL1, TYPE);
        addNode(ID2, LABEL2, TYPE);
        addArc(ARC_ID1, ID1, ID2, TYPE, true);
        assertTrue(underTest.containsArc(ARC_ID1));
    }

    private void addNode(String id, String label, String type) {
        underTest.addNode(new Node(id, label, type));
    }

    @Test
    public void addNodePutsRectangleInSvg() {
        addNode(ID1, LABEL1, TYPE);
        assertSvgElementEqualsFile("addSingleNode", underTest.asSvg());
    }

    @Test
    public void addNodeShouldContainNode() {
        addNode(ID1, LABEL1, TYPE);
        assertTrue(underTest.containsNode(ID1));
    }

    @Test
    public void addTwoNodesAddArcSetLocationShouldCauseArcToReposition() {
        addNode(ID1, LABEL1, TYPE);
        Node node2 = new Node(ID2, LABEL2, TYPE);
        underTest.addNode(node2);
        addArc(ARC_ID1, ID1, ID2, TYPE, true);
        underTest.setLocation(node2, new Point(130, 0));
        assertSvgElementEqualsFile("twoNodesOneArc", underTest.asSvg());
    }

    @Test
    public void addTwoNodesAndSetNewLocation() {
        Node node1 = new Node(ID1, LABEL1, TYPE);
        Node node2 = new Node(ID2, LABEL2, TYPE);
        underTest.addNode(node1);
        underTest.addNode(node2);
        underTest.setLocation(node2, new Point(130, 0));
        assertSvgElementEqualsFile("addTwoNodesSetLocation", underTest.asSvg());
    }

    @Test
    public void addTwoNodesPutsRectanglesInSvg() {
        addNode(ID1, LABEL1, TYPE);
        addNode(ID2, LABEL2, TYPE);
        assertSvgElementEqualsFile("addTwoNodes", underTest.asSvg());
    }

    @Test
    public void addTwoNodesRemoveOne() {
        Node node1 = new Node(ID1, LABEL1, TYPE);
        Node node2 = new Node(ID2, LABEL2, TYPE);
        underTest.addNode(node1);
        underTest.addNode(node2);
        underTest.removeNode(node1);
        assertSvgElementEqualsFile("addTwoNodesRemoveOne", underTest.asSvg());
    }

    @Test
    public void addTwoNodesSetLocationAddArc() {
        addNode(ID1, LABEL1, TYPE);
        Node node2 = new Node(ID2, LABEL2, TYPE);
        underTest.addNode(node2);
        underTest.setLocation(node2, new Point(130, 0));
        addArc(ARC_ID1, ID1, ID2, TYPE, true);
        assertSvgElementEqualsFile("twoNodesOneArc", underTest.asSvg());
    }

    @Test
    public void nodeHasExpectedLocation() {
        Node node = new Node(ID1, LABEL1, TYPE);
        underTest.addNode(node);
        Point newLocation = new Point(100, 100);
        underTest.setLocation(node, newLocation);
        assertThat(underTest.getLocation(node), equalTo(newLocation));
    }

    @Test
    public void removeArcBetweenTwoNodes() {
        addNode(ID1, LABEL1, TYPE);
        Node node2 = new Node(ID2, LABEL2, TYPE);
        underTest.addNode(node2);
        underTest.setLocation(node2, new Point(130, 0));
        Arc arc = new Arc(ARC_ID1, ID1, ID2, TYPE, true);
        underTest.addArc(arc);
        underTest.removeArc(arc);
        assertSvgElementEqualsFile("addTwoNodesSetLocation", underTest.asSvg());
    }

    @Test
    public void removingNodeShouldRemoveArc() {
        Node node1 = new Node(ID1, LABEL1, TYPE);
        Node node2 = new Node(ID2, LABEL2, TYPE);
        underTest.addNode(node1);
        underTest.addNode(node2);
        Arc arc = new Arc(ARC_ID1, ID1, ID2, TYPE, true);
        underTest.addArc(arc);
        underTest.setLocation(node2, new Point(130, 0));
        underTest.removeNode(node1);
        assertSvgElementEqualsFile("addTwoNodesAddArcMoveNode2RemoveNode1",
                underTest.asSvg());
    }

    @Before
    public void setUpGraphWidget() {
        underTest = new GraphSvgWidget(new TextSvgElementFactory());
    }

}
