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

import org.junit.Before;
import org.thechiselgroup.biomixer.client.svg.AbstractSvgTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElementFactory;

public abstract class AbstractGraphSvgDisplayTest extends AbstractSvgTest {

    protected static final String TYPE = "type";

    protected static final String LABEL1 = "Concept1";

    protected static final String LABEL2 = "Concept2";

    protected static final String ID1 = "n1";

    protected static final String ID2 = "n2";

    protected static final String ARC_ID1 = "aid1";

    protected TestGraphSvgDisplay underTest;

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

    @Before
    public void setUpGraphDisplay() {
        underTest = new TestGraphSvgDisplay(400, 300,
                new TextSvgElementFactory());
    }

}
