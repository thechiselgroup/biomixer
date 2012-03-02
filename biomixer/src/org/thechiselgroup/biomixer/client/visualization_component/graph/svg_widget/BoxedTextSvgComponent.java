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
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class BoxedTextSvgComponent extends CompositeSvgComponent {

    public static final String DEFAULT_FONT_FAMILY = "Arial, sans-serif";

    public static final String DEFAULT_FONT_SIZE_PIXELS = "12px";

    public static final String DEFAULT_FONT_STYLE = "normal";

    public static final String DEFAULT_FONT_WEIGHT = "normal";

    public static final double TEXT_BUFFER = 10.0;

    private SvgElement textElement;

    private SvgElement boxElement;

    private TextBoundsEstimator textBoundsEstimator;

    private SvgElementFactory svgElementFactory;

    private String fontFamily = DEFAULT_FONT_FAMILY;

    private String fontSize = DEFAULT_FONT_SIZE_PIXELS;

    private String fontStyle = DEFAULT_FONT_STYLE;

    private String fontWeight = DEFAULT_FONT_WEIGHT;

    private final String text;

    public BoxedTextSvgComponent(String text,
            TextBoundsEstimator textBoundsEstimator,
            SvgElementFactory svgElementFactory) {
        super(svgElementFactory.createElement(Svg.SVG));

        this.textBoundsEstimator = textBoundsEstimator;
        this.svgElementFactory = svgElementFactory;
        this.text = text;
        createBoxedText();
    }

    private void createBoxedText() {
        textElement = svgElementFactory.createElement(Svg.TEXT);
        textElement.setTextContent(text);
        setDefaultFontValues(textElement);

        boxElement = svgElementFactory.createElement(Svg.RECT);
        setDefaultBoxValues(boxElement);

        setBoxAroundText();

        appendChild(boxElement);
        appendChild(textElement);
    }

    private double getBoxHeight() {
        return Double.parseDouble(boxElement.getAttributeAsString(Svg.HEIGHT));
    }

    private double getBoxWidth() {
        return Double.parseDouble(boxElement.getAttributeAsString(Svg.WIDTH));
    }

    private double getBoxX() {
        return Double.parseDouble(boxElement.getAttributeAsString(Svg.X));
    }

    private SizeInt getTextSize(String text) {
        try {
            textBoundsEstimator.setUp();
            textBoundsEstimator.configureFontStyle(fontStyle);
            textBoundsEstimator.configureFontWeight(fontWeight);
            textBoundsEstimator.configureFontSize(fontSize);
            textBoundsEstimator.configureFontFamily(fontFamily);
            return textBoundsEstimator.getSize(text);
        } finally {
            textBoundsEstimator.tearDown();
        }
    }

    public double getTotalHeight() {
        // TODO use BBox on container?
        return getBoxHeight();
    }

    public double getTotalWidth() {
        // TODO use BBox on container?
        return getBoxWidth();
    }

    public void setBackgroundColor(String color) {
        boxElement.setAttribute(Svg.FILL, color);
    }

    public void setBorderColor(String color) {
        boxElement.setAttribute(Svg.STROKE, color);
    }

    private void setBoxAroundText() {
        SizeInt textSize = getTextSize(text);

        boxElement.setAttribute(Svg.WIDTH, textSize.getWidth() + 2
                * TEXT_BUFFER);
        boxElement.setAttribute(Svg.HEIGHT, textSize.getHeight() + 2
                * TEXT_BUFFER);

        textElement.setAttribute(Svg.X, TEXT_BUFFER);
        // the y-position of the text refers to the bottom of the text
        textElement.setAttribute(Svg.Y, TEXT_BUFFER + textSize.getHeight());
    }

    public void setBoxWidth(double width) {
        boxElement.setAttribute(Svg.WIDTH, width);
    }

    private void setBoxX(double x) {
        boxElement.setAttribute(Svg.X, x);
    }

    public void setCornerCurveHeight(double cornerCurveHeight) {
        boxElement.setAttribute(Svg.RY, cornerCurveHeight);
    }

    public void setCornerCurveWidth(double cornerCurveWidth) {
        boxElement.setAttribute(Svg.RX, cornerCurveWidth);
    }

    private void setDefaultBoxValues(SvgElement boxElement) {
        boxElement.setAttribute(Svg.FILL, Colors.WHITE);
        boxElement.setAttribute(Svg.STROKE, Colors.BLACK);
        boxElement.setAttribute(Svg.X, 0.0);
        boxElement.setAttribute(Svg.Y, 0.0);
    }

    private void setDefaultFontValues(SvgElement textElement) {
        textElement.setAttribute(Svg.FONT_FAMILY, DEFAULT_FONT_FAMILY);
        textElement.setAttribute(Svg.FONT_SIZE, DEFAULT_FONT_SIZE_PIXELS);
    }

    public void setFontColor(String color) {
        textElement.setAttribute(Svg.FILL, color);
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
        textElement.setAttribute(Svg.FONT_WEIGHT, fontWeight);
        updateBoxWidthAndPositionAroundText();
    }

    public void setY(double y) {
        compositeElement.setAttribute(Svg.Y, y);
    }

    private void updateBoxWidthAndPositionAroundText() {
        double oldBoxWidth = getBoxWidth();
        setBoxAroundText();
        double newBoxWidth = getBoxWidth();
        setBoxX(getBoxX() - (newBoxWidth - oldBoxWidth) / 2);
    }
}
