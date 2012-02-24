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

import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent.Type;
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
        Node node = addNode(ID1, LABEL1, TYPE);
        underTest.fireNodeTabTestEvent(node, mouseclickEvent());
        assertSvgRootElementEqualsFile("tabExpanded", underTest.asSvg());
    }

    @Test
    public void expandTabClickMenuItem() {
        Node node = addNode(ID1, LABEL1, TYPE);
        underTest.fireNodeTabTestEvent(node, mouseclickEvent());
        underTest.fireViewWideTestEvent(mouseMoveEvent(50, 50));
        underTest.fireTabMenuItemTestEvent("menuItemId-0", mouseclickEvent());
        verify(menuItemHandler0, times(1)).onNodeMenuItemClicked(node);
        // mock handler will not add nodes, but tab menu should still disappear
        assertSvgRootElementEqualsFile("tabExpanded", underTest.asSvg());
    }

    @Test
    public void expandTabMoveMouseOffNode() {
        Node node = addNode(ID1, LABEL1, TYPE);
        underTest.fireNodeTabTestEvent(node, mouseclickEvent());
        underTest.fireViewWideTestEvent(mouseMoveEvent(300, 200));
        assertSvgRootElementEqualsFile("tabExpanded", underTest.asSvg());
    }

    @Test
    public void expandTabMoveMouseOffNodeMouseDown() {
        Node node = addNode(ID1, LABEL1, TYPE);
        underTest.fireNodeTabTestEvent(node, mouseclickEvent());
        underTest.fireViewWideTestEvent(mouseMoveEvent(300, 200));
        underTest.fireViewWideTestEvent(mouseDownEvent(300, 200));
        assertSvgRootElementEqualsFile("basicNode", underTest.asSvg());
    }

    @Test
    public void expandTabMoveMouseOverMenuItem() {
        Node node = addNode(ID1, LABEL1, TYPE);
        underTest.fireNodeTabTestEvent(node, mouseclickEvent());
        underTest.fireViewWideTestEvent(mouseMoveEvent(50, 50));
        underTest.fireTabMenuItemTestEvent("menuItemId-0", mouseOverEvent());
        assertSvgRootElementEqualsFile("tabExpandedMouseOverFirstOption",
                underTest.asSvg());
    }

    @Test
    public void expandTabMoveMouseOverMenuItemMouseOut() {
        Node node = addNode(ID1, LABEL1, TYPE);
        underTest.fireNodeTabTestEvent(node, mouseclickEvent());
        underTest.fireViewWideTestEvent(mouseMoveEvent(50, 50));
        underTest.fireTabMenuItemTestEvent("menuItemId-0", mouseOverEvent());
        underTest.fireTabMenuItemTestEvent("menuItemId-0", mouseOutEvent());
        assertSvgRootElementEqualsFile("tabExpanded", underTest.asSvg());
    }

    private ChooselEvent mouseclickEvent() {
        return new ChooselEvent(Type.CLICK);
    }

    private ChooselEvent mouseDownEvent(int x, int y) {
        return new ChooselEvent(Type.MOUSE_DOWN, x, y);
    }

    @Test
    public void mouseDownMouseMoveCausesNodeToMove() {
        Node node = addNode(ID1, LABEL1, TYPE);
        underTest.fireNodeTestEvent(node, mouseDownEvent(5, 5));
        underTest.fireViewWideTestEvent(mouseMoveEvent(20, 20));
        assertSvgRootElementEqualsFile("nodeDrag", underTest.asSvg());
    }

    @Test
    public void mouseDownMouseMoveTwiceCausesNodeToMoveToFinalDestination() {
        Node node = addNode(ID1, LABEL1, TYPE);
        underTest.fireNodeTestEvent(node, mouseDownEvent(5, 5));
        underTest.fireViewWideTestEvent(mouseMoveEvent(20, 20));
        underTest.fireViewWideTestEvent(mouseMoveEvent(10, 10));
        assertSvgRootElementEqualsFile("nodeDragForwardBack", underTest.asSvg());
    }

    private ChooselEvent mouseMoveEvent(int x, int y) {
        return new ChooselEvent(Type.MOUSE_MOVE, x, y);
    }

    private ChooselEvent mouseOutEvent() {
        return new ChooselEvent(Type.MOUSE_OUT);
    }

    private ChooselEvent mouseOverEvent() {
        return new ChooselEvent(Type.MOUSE_OVER);
    }

}
