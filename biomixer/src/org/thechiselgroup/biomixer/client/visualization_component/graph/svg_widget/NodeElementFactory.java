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

import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class NodeElementFactory {

    public static final double TEXT_BUFFER = 10.0;

    public static final double RX_DEFAULT = 10.0;

    public static final double RY_DEFAULT = 10.0;

    private SvgElementFactory svgElementFactory;

    public NodeElementFactory(SvgElementFactory svgElementFactory) {
        this.svgElementFactory = svgElementFactory;
    }

    public NodeElement createNodeElement(final Node node) {
        assert node != null;

        SvgElement nodeContainer = svgElementFactory.createElement(Svg.SVG);
        nodeContainer.setAttribute(Svg.ID, node.getId());
        nodeContainer.setAttribute(Svg.X, 0.0);
        nodeContainer.setAttribute(Svg.Y, 0.0);

        SvgElement text = svgElementFactory.createElement(Svg.TEXT);
        text.setTextContent(node.getLabel());
        text.setAttribute(Svg.X, 0.0);
        text.setAttribute(Svg.Y, 0.0);

        // XXX causing errors. Because not rendered yet?
        // SizeDouble textBBox = text.getBBox();
        // double textWidth = textBBox.getWidth();
        // double textHeight = textBBox.getHeight();

        SvgElement rectangle = svgElementFactory.createElement(Svg.RECT);
        rectangle.setAttribute(Svg.FILL, "white");
        // TODO proper colors
        rectangle.setAttribute(Svg.STROKE, "black");
        rectangle.setAttribute(Svg.RX, RX_DEFAULT);
        rectangle.setAttribute(Svg.RY, RY_DEFAULT);

        // TODO currently using default width and height. Should be determined
        // based on text size.
        // rectangle.setAttribute(Svg.WIDTH, textWidth + 2 * TEXT_BUFFER);
        // rectangle.setAttribute(Svg.HEIGHT, textHeight + 2 * TEXT_BUFFER);
        rectangle.setAttribute(Svg.WIDTH, 100.0);
        rectangle.setAttribute(Svg.HEIGHT, 40.0);
        rectangle.setAttribute(Svg.X, 0.0);
        rectangle.setAttribute(Svg.Y, 0.0);

        // text.setAttribute(Svg.X, TEXT_BUFFER);
        // text.setAttribute(Svg.Y, TEXT_BUFFER);
        text.setAttribute(Svg.X, 10.0);
        text.setAttribute(Svg.Y, 20.0);

        nodeContainer.appendChild(rectangle);
        nodeContainer.appendChild(text);

        return new NodeElement(node, nodeContainer, rectangle, text);
    }

}
