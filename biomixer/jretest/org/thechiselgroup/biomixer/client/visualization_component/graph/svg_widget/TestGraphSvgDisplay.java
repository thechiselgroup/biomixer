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

import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.text.TestTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElement;

/**
 * This class provides default values for anything that would normally involve
 * GWT dependencies to retrieve, and therefore not be usable in regular unit
 * tests. It also provides methods for firing test events.
 * 
 * @author drusk
 * 
 */
public class TestGraphSvgDisplay extends GraphSvgDisplay {

    public TestGraphSvgDisplay(int width, int height,
            SvgElementFactory svgElementFactory) {
        super(width, height, svgElementFactory);
    }

    public void fireNodeTabTestEvent(Node node, ChooselEvent event) {
        TextSvgElement tabContainer = (TextSvgElement) getNodeComponent(node)
                .getExpanderTab().getSvgElement();
        tabContainer.getEventListener().onEvent(event);
    }

    public void fireNodeTestEvent(Node node, ChooselEvent event) {
        TextSvgElement nodeContainer = (TextSvgElement) getNodeComponent(node)
                .getNodeContainer();
        nodeContainer.getEventListener().onEvent(event);
    }

    public void fireTabMenuItemTestEvent(String expanderLabel, ChooselEvent event) {
        // XXX better way of firing these events?
        PopupExpanderSvgComponent popupExpanderList = (PopupExpanderSvgComponent) popupGroup
                .getCompositeSubComponents().get(0);
        TextSvgElement menuItemContainer = (TextSvgElement) popupExpanderList
                .getEntryByExpanderLabel(expanderLabel).getSvgElement();
        menuItemContainer.getEventListener().onEvent(event);
    }

    public void fireViewWideTestEvent(ChooselEvent chooselEvent) {
        getTextRootSvgElement().getEventListener().onEvent(chooselEvent);
    }

    @Override
    public int getGraphAbsoluteLeft() {
        return 0;
    }

    @Override
    public int getGraphAbsoluteTop() {
        return 0;
    }

    private TextSvgElement getTextRootSvgElement() {
        return (TextSvgElement) rootSvgComponent.getSvgElement();
    }

    @Override
    protected void initTextBoundsEstimator() {
        textBoundsEstimator = new TestTextBoundsEstimator(10, 20);
    }

}
