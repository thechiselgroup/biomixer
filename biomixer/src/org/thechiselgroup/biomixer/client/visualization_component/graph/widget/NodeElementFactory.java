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

import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class NodeElementFactory {

    private SvgElementFactory svgElementFactory;

    private GraphSvgDisplay graphDisplay;

    public NodeElementFactory(SvgElementFactory svgElementFactory,
            GraphSvgDisplay graphDisplay) {
        this.svgElementFactory = svgElementFactory;
        this.graphDisplay = graphDisplay;
    }

    public NodeElement createNodeElement(final Node node) {
        assert node != null;

        SvgElement nodeContainer = svgElementFactory.createElement(Svg.SVG);
        nodeContainer.setAttribute(Svg.ID, node.getId());
        nodeContainer.setAttribute(Svg.X, 0.0);
        nodeContainer.setAttribute(Svg.Y, 0.0);

        SvgElement rectangle = svgElementFactory.createElement(Svg.RECT);
        rectangle.setAttribute(Svg.FILL, "white");
        // TODO proper colors
        rectangle.setAttribute(Svg.STROKE, "black");
        rectangle.setAttribute(Svg.RX, 10.0);
        rectangle.setAttribute(Svg.RY, 10.0);

        // TODO currently using default width and height. Should be determined
        // based on text size.
        rectangle.setAttribute(Svg.WIDTH, 100.0);
        rectangle.setAttribute(Svg.HEIGHT, 40.0);
        rectangle.setAttribute(Svg.X, 0.0);
        rectangle.setAttribute(Svg.Y, 0.0);

        SvgElement text = svgElementFactory.createElement(Svg.TEXT);
        text.setTextContent(node.getLabel());
        text.setAttribute(Svg.X, 10.0);
        text.setAttribute(Svg.Y, 20.0);

        nodeContainer.appendChild(rectangle);
        nodeContainer.appendChild(text);

        nodeContainer.setEventListener(new SvgNodeEventHandler(node.getId(),
                graphDisplay));

        return new NodeElement(node, nodeContainer, rectangle, text);
    }

}
