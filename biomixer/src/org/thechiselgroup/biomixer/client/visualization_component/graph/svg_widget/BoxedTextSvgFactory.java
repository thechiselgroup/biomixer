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

import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.text.SvgBBoxTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.TestTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.JsDomSvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElementFactory;

public class BoxedTextSvgFactory {

    public static final double TEXT_BUFFER = 10.0;

    private final SvgElementFactory svgElementFactory;

    private TextBoundsEstimator textBoundsEstimator;

    public BoxedTextSvgFactory(SvgElementFactory svgElementFactory) {
        this.svgElementFactory = svgElementFactory;
        initTextBoundsEstimator(svgElementFactory);
        assert this.textBoundsEstimator != null;
    }

    public BoxedTextSvgElement createBoxedText(String text) {
        SvgElement containerElement = svgElementFactory.createElement(Svg.SVG);

        SvgElement textElement = svgElementFactory.createElement(Svg.TEXT);
        textElement.setTextContent(text);

        SvgElement boxElement = svgElementFactory.createElement(Svg.RECT);
        setDefaultAttributeValues(boxElement);

        textBoundsEstimator.setUp();
        SizeInt textSize = textBoundsEstimator.getSize(text);
        textBoundsEstimator.tearDown();

        boxElement.setAttribute(Svg.WIDTH, textSize.getWidth() + 2
                * TEXT_BUFFER);
        boxElement.setAttribute(Svg.HEIGHT, textSize.getHeight() + 2
                * TEXT_BUFFER);

        textElement.setAttribute(Svg.X, TEXT_BUFFER);
        // the y-position of the text refers to the bottom of the text
        textElement.setAttribute(Svg.Y, TEXT_BUFFER + textSize.getHeight());

        containerElement.appendChild(boxElement);
        containerElement.appendChild(textElement);

        return new BoxedTextSvgElement(containerElement, textElement,
                boxElement);
    }

    private void initTextBoundsEstimator(SvgElementFactory svgElementFactory) {
        if (svgElementFactory instanceof JsDomSvgElementFactory) {
            this.textBoundsEstimator = new SvgBBoxTextBoundsEstimator(
                    svgElementFactory);
        } else if (svgElementFactory instanceof TextSvgElementFactory) {
            this.textBoundsEstimator = new TestTextBoundsEstimator(10, 20);
        }
    }

    private void setDefaultAttributeValues(SvgElement boxElement) {
        boxElement.setAttribute(Svg.FILL, Colors.WHITE);
        boxElement.setAttribute(Svg.STROKE, Colors.BLACK);
        boxElement.setAttribute(Svg.X, 0.0);
        boxElement.setAttribute(Svg.Y, 0.0);
    }

}
