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

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.util.animation.TestAnimationRunner;
import org.thechiselgroup.biomixer.client.core.visualization.behaviors.rendered_items.RenderedItemPopupManager;
import org.thechiselgroup.biomixer.client.svg.AbstractSvgTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphRendererConceptGraphTestFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.ArcSizeTransformer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSizeTransformer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.parser.SvgResultParser;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMenuItemClickedHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseClickEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseClickHandler;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public abstract class AbstractConceptGraphSvgDisplayTest extends
        AbstractSvgTest {

    protected static final String TYPE1 = "type1";

    protected static final String TYPE2 = "type2";

    protected static final String LABEL1 = "Concept1";

    protected static final String LABEL2 = "Concept2";

    protected static final String N1 = "n1";

    protected static final String N2 = "n2";

    protected static final String N3 = "n3";

    protected static final String A1 = "a1";

    protected static final String MENU_ITEM_0_LABEL = "MenuItem0";

    protected static final String MENU_ITEM_1_LABEL = "MenuItem1";

    protected static final String MENU_ITEM_2_LABEL = "MenuItem2";

    @Mock
    protected ErrorHandler errorHandler;

    @Mock
    RenderedItemPopupManager renderedArcPopupManager;

    @Mock
    NodeSizeTransformer nodeSizeTransformer;

    @Mock
    ArcSizeTransformer arcSizeTransformer;

    @Mock
    protected NodeMouseClickHandler nodeMouseClickHandler;

    @Mock
    protected NodeMenuItemClickedHandler menuItemHandler0;

    @Mock
    protected NodeMenuItemClickedHandler menuItemHandler1;

    @Mock
    protected NodeMenuItemClickedHandler menuItemHandler2;

    GraphRendererConceptGraphTestFactory factory = new GraphRendererConceptGraphTestFactory();

    protected TestGraphSvgDisplay underTest;

    protected SvgResultParser parser;

    protected Arc addArc(String arcId, String sourceNodeId,
            String targetNodeId, String type, String arcLabel, boolean directed) {
        Arc arc = new Arc(arcId, sourceNodeId, targetNodeId, type, arcLabel,
                directed);
        underTest.addArc(arc);
        return arc;
    }

    protected Node addNode(String id, String label, String type) {
        Node node = new Node(id, label, type, 1);
        underTest.addNode(node);
        return node;
    }

    protected TestAnimationRunner animate(Node node, Point destination) {
        underTest.animateMoveTo(node, destination);
        return underTest.getTestAnimationRunner();
    }

    /**
     * 
     * @param id
     *            the id of the element to match
     * @param fileIdentifier
     *            the file to look in for the element with specified id
     * @throws Exception
     */
    protected void assertComponentWithIdEqualsFile(String id,
            String fileIdentifier) throws Exception {
        assertUnderTestComponentEqualsFile("//*[@id='" + id + "']",
                fileIdentifier);
    }

    protected void assertUnderTestAsSvgEqualsFile(String fileIdentifier) {
        assertSvgRootElementEqualsFile(fileIdentifier, underTest.asSvg());
    }

    protected void assertUnderTestComponentEqualsFile(String xpath,
            String fileIdentifier) throws Exception {
        assertElementEqualsFile(fileIdentifier, extractElementAsString(xpath));
    }

    protected String extractElementAsString(String xpath) throws Exception {
        return parser.extractElementAsString(underTest.asSvg(), xpath);
    }

    @Before
    public void initParser() throws ParserConfigurationException {
        parser = new SvgResultParser(new StandardJavaXMLDocumentProcessor());
    }

    @Before
    public void setUpGraphDisplay() {
        MockitoAnnotations.initMocks(this);
        underTest = new TestGraphSvgDisplay(
                400,
                300,
                // factory.createGraphRenderer(400, 300,
                // new TextSvgElementFactory()), errorHandler);
                factory.createGraphRenderer(400, 300), errorHandler,
                renderedArcPopupManager, nodeSizeTransformer,
                arcSizeTransformer);
        underTest.addEventHandler(NodeMouseClickEvent.TYPE,
                nodeMouseClickHandler);
        underTest.addNodeMenuItemHandler(MENU_ITEM_0_LABEL, menuItemHandler0,
                TYPE1);
        underTest.addNodeMenuItemHandler(MENU_ITEM_1_LABEL, menuItemHandler1,
                TYPE1);
        underTest.addNodeMenuItemHandler(MENU_ITEM_2_LABEL, menuItemHandler2,
                TYPE2);
    }
}
