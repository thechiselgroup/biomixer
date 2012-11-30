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

import org.junit.Test;
import org.mockito.Mock;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * Tests interactions such as mouse over, mouse down, etc. as well as more
 * complex combinations of these, by checking the svg output.
 * 
 * @author drusk
 * 
 */
public class OntologyOverviewGraphSvgDisplayInteractionOutputTest extends
        AbstractOntologyOverviewGraphSvgDisplayTest {

    @Mock
    ChooselEvent chooselEvent;

    private void clickBackground(int x, int y) {
        underTest.onBackgroundClick(x, y);
    }

    private void clickNode(Node node, int x, int y) {
        underTest.onNodeMouseClick(node.getId(), chooselEvent, x, y);
    }

    private void clickNodeExpanderItem(Node node, String itemId) {
        underTest.onNodeExpanderClick(getRenderedNodeExpander(node), itemId);
    }

    private void clickNodeTab(Node node) {
        underTest.onNodeTabClick(getRenderedNode(node));
    }

    // TODO Circular nodes don't have expander tabs...but they might in the
    // future! See the concept node tests for where these came from.

    // @Test
    // public void clickingNodeBodyRemovesExpander() throws Exception {
    // Node node = addNode(N1, LABEL1, TYPE1);
    // clickNodeTab(node);
    // clickNode(node, 10, 10);
    // verify(nodeMouseClickHandler, times(1)).onMouseClick(
    // any(NodeMouseClickEvent.class));
    // assertComponentWithIdEqualsFile(N1, "circleNode1");
    // }

    // @Test
    // public void expandTab() {
    // Node node = addNode(N1, LABEL1, TYPE1);
    // clickNodeTab(node);
    // assertUnderTestAsSvgEqualsFile("tabExpanded");
    // }

    // @Test
    // public void expandTabClickMenuItem() throws Exception {
    // Node node = addNode(N1, LABEL1, TYPE1);
    // clickNodeTab(node);
    // moveMouse(50, 50);
    // clickNodeExpanderItem(node, MENU_ITEM_0_LABEL);
    // verify(menuItemHandler0, times(1)).onNodeMenuItemClicked(node);
    // // mock handler will not add nodes, but tab menu should still disappear
    // assertComponentWithIdEqualsFile(N1, "circleNode1");
    // }

    // @Test
    // public void expandTabMoveMouseOffNode() {
    // Node node = addNode(N1, LABEL1, TYPE1);
    // clickNodeTab(node);
    // moveMouse(300, 200);
    // assertUnderTestAsSvgEqualsFile("tabExpanded");
    // }

    // @Test
    // public void expandTabMoveMouseOffNodeClickBackground() throws Exception {
    // Node node = addNode(N1, LABEL1, TYPE1);
    // clickNodeTab(node);
    // moveMouse(300, 200);
    // clickBackground(300, 200);
    // assertComponentWithIdEqualsFile(N1, "circleNode1");
    // }

    // @Test
    // public void expandTabMoveMouseOverMenuItem() {
    // Node node = addNode(N1, LABEL1, TYPE1);
    // clickNodeTab(node);
    // moveMouse(50, 50);
    // mouseOverNodeExpanderItem(node, MENU_ITEM_0_LABEL);
    // assertUnderTestAsSvgEqualsFile("tabExpandedMouseOverFirstOption");
    // }

    // @Test
    // public void expandTabMoveMouseOverMenuItemMouseOut() {
    // Node node = addNode(N1, LABEL1, TYPE1);
    // clickNodeTab(node);
    // moveMouse(50, 50);
    // mouseOverNodeExpanderItem(node, MENU_ITEM_0_LABEL);
    // mouseOutNodeExpanderItem(node, MENU_ITEM_0_LABEL);
    // assertUnderTestAsSvgEqualsFile("tabExpanded");
    // }

    private RenderedNode getRenderedNode(Node node) {
        return underTest.graphRenderer.getRenderedNode(node);
    }

    private RenderedNodeExpander getRenderedNodeExpander(Node node) {
        return underTest.graphRenderer.getRenderedNodeExpander(node);
    }

    @Test
    public void mouseDownMouseMoveCausesNodeToMove() throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        mouseDownOnNode(node, 5, 5);
        moveMouse(20, 20);
        assertComponentWithIdEqualsFile(N1, "circleNodeDrag");
    }

    @Test
    public void mouseDownMouseMoveTwiceCausesNodeToMoveToFinalDestination()
            throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        // TODO This test isn't as thorough as it could be. It doesn't assert
        // before and after the move. Currently does not pass either. Check all
        // the node moving tests in both graph display test classes.
        mouseDownOnNode(node, 5, 5);
        moveMouse(20, 20);
        moveMouse(10, 10);
        assertComponentWithIdEqualsFile(N1, "circleNodeDragForwardBack");
    }

    private void mouseDownOnNode(Node node, int x, int y) {
        underTest.onNodeMouseDown(node, chooselEvent, x, y);
    }

    private void mouseOutNodeExpanderItem(Node node, String itemId) {
        underTest.onNodeExpanderMouseOut(getRenderedNodeExpander(node), itemId);
    }

    private void mouseOverNode(Node node, int x, int y) {
        underTest.onNodeMouseOver(getRenderedNode(node), chooselEvent, x, y);
    }

    private void mouseOverNodeExpanderItem(Node node, String itemId) {
        underTest
                .onNodeExpanderMouseOver(getRenderedNodeExpander(node), itemId);
    }

    @Test
    public void mousingOverPartiallyCoveredNodeBringsItToTop() {
        Node node1 = addNode(N1, LABEL1, TYPE1);
        Node node2 = addNode(N2, LABEL2, TYPE1);
        // test nodes have height of 40
        underTest.setLocation(node2, new Point(0, 20));
        assertUnderTestAsSvgEqualsFile("circleOverlappingNodesN2OnTop");
        mouseOverNode(node1, 5, 10);
        assertUnderTestAsSvgEqualsFile("circleOverlappingNodesN1OnTop");
    }

    private void moveMouse(int x, int y) {
        underTest.onViewMouseMove(chooselEvent, x, y);
    }

}
