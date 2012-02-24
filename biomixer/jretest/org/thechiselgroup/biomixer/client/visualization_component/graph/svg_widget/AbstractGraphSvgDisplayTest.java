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
import org.thechiselgroup.biomixer.client.svg.AbstractSvgTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.parser.SvgResultParser;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMenuItemClickedHandler;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElementFactory;

public abstract class AbstractGraphSvgDisplayTest extends AbstractSvgTest {

    protected static final String TYPE = "type";

    protected static final String LABEL1 = "Concept1";

    protected static final String LABEL2 = "Concept2";

    protected static final String ID1 = "n1";

    protected static final String ID2 = "n2";

    protected static final String ARC_ID1 = "aid1";

    protected static final String MENU_ITEM_ID_0 = "menuItemId-0";

    @Mock
    protected NodeMenuItemClickedHandler menuItemHandler0;

    @Mock
    protected NodeMenuItemClickedHandler menuItemHandler1;

    protected TestGraphSvgDisplay underTest;

    protected SvgResultParser parser;

    protected Arc addArc(String arcId, String sourceNodeId,
            String targetNodeId, String type, boolean directed) {
        Arc arc = new Arc(arcId, sourceNodeId, targetNodeId, type, directed);
        underTest.addArc(arc);
        return arc;
    }

    protected Node addNode(String id, String label, String type) {
        Node node = new Node(id, label, type);
        underTest.addNode(node);
        return node;
    }

    public void assertUnderTestAsSvgEqualsFile(String fileIdentifier) {
        assertSvgRootElementEqualsFile(fileIdentifier, underTest.asSvg());
    }

    @Before
    public void setUpGraphDisplay() {
        MockitoAnnotations.initMocks(this);
        underTest = new TestGraphSvgDisplay(400, 300,
                new TextSvgElementFactory());
        underTest.addNodeMenuItemHandler("MenuItem1", menuItemHandler0, TYPE);
        underTest.addNodeMenuItemHandler("MenuItem2", menuItemHandler1, TYPE);
    }

    protected String extractElementAsString(String xpath) throws Exception {
        return parser.extractElementAsString(underTest.asSvg(), xpath);
    }

    @Before
    public void initParser() throws ParserConfigurationException {
        parser = new SvgResultParser(new StandardJavaXMLDocumentProcessor());
    }

}
