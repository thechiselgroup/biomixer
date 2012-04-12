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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation;

import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

/**
 * Renders a node as the classic rectangle with text in it.
 * 
 * @author drusk
 * 
 */
public class BoxedTextSvgNodeRenderer implements NodeRenderer {

    public static final double RX_DEFAULT = 10.0;

    public static final double RY_DEFAULT = 10.0;

    private SvgElementFactory svgElementFactory;

    private ExpanderTabRenderer expanderTabRenderer;

    private TextBoundsEstimator textBoundsEstimator;

    public BoxedTextSvgNodeRenderer(SvgElementFactory svgElementFactory,
            TextBoundsEstimator textBoundsEstimator) {
        this.textBoundsEstimator = textBoundsEstimator;
        assert svgElementFactory != null;
        assert textBoundsEstimator != null;
        this.svgElementFactory = svgElementFactory;
        this.expanderTabRenderer = new ExpanderTabRenderer(svgElementFactory);
    }

    @Override
    public RenderedNode createRenderedNode(Node node) {
        assert node != null;

        SvgElement baseContainer = svgElementFactory.createElement(Svg.SVG);
        baseContainer.setAttribute(Svg.OVERFLOW, Svg.VISIBLE);
        baseContainer.setAttribute(Svg.ID, node.getId());
        baseContainer.setAttribute(Svg.X, 0.0);
        baseContainer.setAttribute(Svg.Y, 0.0);

        BoxedTextSvgComponent boxedText = new BoxedTextSvgComponent(
                node.getLabel(), textBoundsEstimator, svgElementFactory);
        boxedText.setCornerCurveWidth(RX_DEFAULT);
        boxedText.setCornerCurveHeight(RY_DEFAULT);

        ExpanderTabSvgComponent expanderTab = expanderTabRenderer
                .createExpanderTabSvgElement();
        expanderTab.setLocation(
                (boxedText.getTotalWidth() - ExpanderTabRenderer.TAB_WIDTH) / 2,
                boxedText.getTotalHeight());

        return new NodeSvgComponent(node, null, baseContainer, boxedText,
                expanderTab);
    }

}
