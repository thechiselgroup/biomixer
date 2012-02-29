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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.TestEventFactory.createMouseDownEvent;
import static org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.TestEventFactory.createMouseMoveEvent;
import static org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.TestEventFactory.createMouseOutEvent;
import static org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.TestEventFactory.createMouseOverEvent;
import static org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.TestEventFactory.createMouseclickEvent;

import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * Tests interactions such as mouse over, mouse down, etc. as well as more
 * complex combinations of these, by checking the svg output.
 * 
 * @author drusk
 * 
 */
public class GraphSvgDisplayInteractionOutputTest extends
        AbstractGraphSvgDisplayTest {

    @Test
    public void expandTab() {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.fireNodeTabTestEvent(node, createMouseclickEvent());
        assertUnderTestAsSvgEqualsFile("tabExpanded");
    }

    @Test
    public void expandTabClickMenuItem() throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.fireNodeTabTestEvent(node, createMouseclickEvent());
        underTest.fireViewWideTestEvent(createMouseMoveEvent(50, 50));
        underTest.fireTabMenuItemTestEvent(MENU_ITEM_0_LABEL,
                TestEventFactory.createMouseclickEvent());
        verify(menuItemHandler0, times(1)).onNodeMenuItemClicked(node);
        // mock handler will not add nodes, but tab menu should still disappear
        assertComponentWithIdEqualsFile(N1, "basicNode");
    }

    @Test
    public void expandTabMoveMouseOffNode() {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.fireNodeTabTestEvent(node, createMouseclickEvent());
        underTest.fireViewWideTestEvent(createMouseMoveEvent(300, 200));
        assertUnderTestAsSvgEqualsFile("tabExpanded");
    }

    @Test
    public void expandTabMoveMouseOffNodeMouseDown() throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.fireNodeTabTestEvent(node,
                TestEventFactory.createMouseclickEvent());
        underTest.fireViewWideTestEvent(createMouseMoveEvent(300, 200));
        underTest.fireViewWideTestEvent(createMouseDownEvent(300, 200));
        assertComponentWithIdEqualsFile(N1, "basicNode");
    }

    @Test
    public void expandTabMoveMouseOverMenuItem() {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.fireNodeTabTestEvent(node, createMouseclickEvent());
        underTest.fireViewWideTestEvent(createMouseMoveEvent(50, 50));
        underTest.fireTabMenuItemTestEvent(MENU_ITEM_0_LABEL,
                createMouseOverEvent());
        assertUnderTestAsSvgEqualsFile("tabExpandedMouseOverFirstOption");
    }

    @Test
    public void expandTabMoveMouseOverMenuItemMouseOut() {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.fireNodeTabTestEvent(node, createMouseclickEvent());
        underTest.fireViewWideTestEvent(createMouseMoveEvent(50, 50));
        underTest.fireTabMenuItemTestEvent(MENU_ITEM_0_LABEL,
                createMouseOverEvent());
        underTest.fireTabMenuItemTestEvent(MENU_ITEM_0_LABEL,
                createMouseOutEvent());
        assertUnderTestAsSvgEqualsFile("tabExpanded");
    }

    @Test
    public void mouseDownMouseMoveCausesNodeToMove() throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.fireNodeTestEvent(node, createMouseDownEvent(5, 5));
        underTest.fireViewWideTestEvent(createMouseMoveEvent(20, 20));
        assertComponentWithIdEqualsFile(N1, "nodeDrag");
    }

    @Test
    public void mouseDownMouseMoveTwiceCausesNodeToMoveToFinalDestination()
            throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.fireNodeTestEvent(node, createMouseDownEvent(5, 5));
        underTest.fireViewWideTestEvent(createMouseMoveEvent(20, 20));
        underTest.fireViewWideTestEvent(createMouseMoveEvent(10, 10));
        assertComponentWithIdEqualsFile(N1, "nodeDragForwardBack");
    }

    @Test
    public void mousingOverPartiallyCoveredNodeBringsItToTop() {
        Node node1 = addNode(N1, LABEL1, TYPE1);
        Node node2 = addNode(N2, LABEL2, TYPE1);
        // test nodes have height of 40
        underTest.setLocation(node2, new Point(0, 20));
        assertUnderTestAsSvgEqualsFile("overlappingNodesN2OnTop");
        underTest.fireNodeTestEvent(node1, createMouseOverEvent());
        assertUnderTestAsSvgEqualsFile("overlappingNodesN1OnTop");
    }

}
