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

import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class BoxedTextSvgFactory {

    public static final double DEFAULT_BOX_WIDTH = 100.0;

    public static final double DEFAULT_BOX_HEIGHT = 40.0;

    public static final double TEXT_BUFFER = 10.0;

    private final SvgElementFactory svgElementFactory;

    public BoxedTextSvgFactory(SvgElementFactory svgElementFactory) {
        this.svgElementFactory = svgElementFactory;
    }

    public BoxedTextSvgElement createBoxedText(String text) {
        SvgElement containerElement = svgElementFactory.createElement(Svg.SVG);

        SvgElement textElement = svgElementFactory.createElement(Svg.TEXT);
        textElement.setTextContent(text);

        // TODO set box width and height based on text size
        SvgElement boxElement = svgElementFactory.createElement(Svg.RECT);
        // set default colors
        boxElement.setAttribute(Svg.FILL, Colors.WHITE);
        boxElement.setAttribute(Svg.STROKE, Colors.BLACK);
        boxElement.setAttribute(Svg.WIDTH, DEFAULT_BOX_WIDTH);
        boxElement.setAttribute(Svg.HEIGHT, DEFAULT_BOX_HEIGHT);
        boxElement.setAttribute(Svg.X, 0.0);
        boxElement.setAttribute(Svg.Y, 0.0);

        // textElement.setAttribute(Svg.X, TEXT_BUFFER);
        // textElement.setAttribute(Svg.Y, TEXT_BUFFER);
        textElement.setAttribute(Svg.X, 10.0);
        textElement.setAttribute(Svg.Y, 20.0);

        containerElement.appendChild(boxElement);
        containerElement.appendChild(textElement);

        return new BoxedTextSvgElement(containerElement, textElement,
                boxElement);
    }

}
