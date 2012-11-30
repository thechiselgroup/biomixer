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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes;

import static org.thechiselgroup.biomixer.shared.svg.SvgTransforms.rotate;
import static org.thechiselgroup.biomixer.shared.svg.SvgTransforms.translate;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.IsSvg;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

/**
 * An SVG element with text. Based off of {@link SvgBoxedText}.
 * 
 * @author everbeek
 * 
 */
public class SvgBareText implements IsSvg {

    protected static final double THRESHOLD_TEXT_LENGTH = 150.0;

    public static final String DEFAULT_FONT_FAMILY = "Arial, sans-serif";

    public static final String DEFAULT_FONT_SIZE_PIXELS = "12px";

    public static final String DEFAULT_FONT_STYLE = "normal";

    public static final String DEFAULT_FONT_WEIGHT = "normal";

    public static final double TEXT_BUFFER = 10.0;

    protected SvgElement textElement;

    protected TextBoundsEstimator textBoundsEstimator;

    protected SizeInt cachedTextSize = null;

    protected SvgElementFactory svgElementFactory;

    protected String fontFamily = DEFAULT_FONT_FAMILY;

    protected String fontSize = DEFAULT_FONT_SIZE_PIXELS;

    protected String fontStyle = DEFAULT_FONT_STYLE;

    protected String fontWeight = DEFAULT_FONT_WEIGHT;

    protected final String text;

    protected int numberOfLines = 0;

    /*
     * Stores the tspan elements for multiple lines of text. Will be empty if
     * the text fits on one line and therefore doesn't need any tspans.
     */
    protected Map<String, SvgElement> tspanElements = CollectionFactory
            .createStringMap();

    protected String longestTextLine;

    protected SvgElement container;

    protected double width;

    protected double height;

    public SvgBareText(String text, TextBoundsEstimator textBoundsEstimator,
            SvgElementFactory svgElementFactory) {
        container = svgElementFactory.createElement(Svg.SVG);
        container.setAttribute(Svg.OVERFLOW, Svg.VISIBLE);
        this.textBoundsEstimator = textBoundsEstimator;
        this.svgElementFactory = svgElementFactory;
        this.text = text;
        init();
    }

    @Override
    public SvgElement asSvgElement() {
        return container;
    }

    protected void init() {
        textElement = svgElementFactory.createElement(Svg.TEXT);
        setTextContent();
        setDefaultFontValues(textElement);

        container.appendChild(textElement);
    }

    protected void finishLine(StringBuilder currentLine, int textHeight,
            int currentLineWidth) {
        SvgElement tspan = svgElementFactory.createElement(Svg.TSPAN);

        String line = currentLine.toString().trim();
        tspan.setTextContent(line);

        tspan.setAttribute(Svg.X, TEXT_BUFFER);
        int dy = numberOfLines == 0 ? 0 : textHeight;
        tspan.setAttribute(Svg.DY, dy);

        textElement.appendChild(tspan);
        tspanElements.put(line, tspan);

        if (currentLineWidth > getWidthOfLongestTextLine()) {
            longestTextLine = line;
        }

        numberOfLines++;
    }

    protected SizeInt getTextSize(String text) {
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
        // return height;
        return getHeightOfLongestTextLine();
    }

    public double getTotalWidth() {
        // TODO use BBox on container?
        // return width;
        return getWidthOfLongestTextLine();
    }

    protected double getWidthOfLongestTextLine() {
        if (longestTextLine == null) {
            return 0.0;
        } else if (null == cachedTextSize) {
            SizeInt cachedTextSize = getTextSize(longestTextLine);
            return cachedTextSize.getWidth();
        } else {
            return cachedTextSize.getWidth();
        }
    }

    protected double getHeightOfLongestTextLine() {
        if (longestTextLine == null) {
            return 0.0;
        } else if (null == cachedTextSize) {
            SizeInt cachedTextSize = getTextSize(longestTextLine);
            return cachedTextSize.getHeight();
        } else {
            return cachedTextSize.getHeight();
        }
    }

    protected void setDefaultFontValues(SvgElement textElement) {
        cachedTextSize = null;
        textElement.setAttribute(Svg.FONT_FAMILY, DEFAULT_FONT_FAMILY);
        textElement.setAttribute(Svg.FONT_SIZE, DEFAULT_FONT_SIZE_PIXELS);

    }

    public void setEventListener(ChooselEventHandler listener) {
        container.setEventListener(listener);
    }

    public void setFontColor(String color) {
        textElement.setAttribute(Svg.FILL, color);
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
        textElement.setAttribute(Svg.FONT_WEIGHT, fontWeight);
        cachedTextSize = null;
    }

    protected void setTextContent() {
        cachedTextSize = null;
        SizeInt textSize = getTextSize(text);
        if (textSize.getWidth() < THRESHOLD_TEXT_LENGTH) {
            textElement.setTextContent(text);
            longestTextLine = text;
            numberOfLines = 1;
        } else {
            // Need to wrap text
            String[] words = text.split(" ");

            int spaceWidth = getTextSize(" ").getWidth();
            int textHeight = textSize.getHeight();

            StringBuilder currentLine = new StringBuilder();
            int currentLineWidth = 0;
            for (String word : words) {
                int wordWidth = getTextSize(word).getWidth();
                if (currentLineWidth > 0) {
                    currentLine.append(" ");
                    currentLineWidth += spaceWidth;
                }

                if (currentLineWidth + wordWidth < THRESHOLD_TEXT_LENGTH) {
                    // the word can fit on current line
                    currentLine.append(word);
                    currentLineWidth += wordWidth;
                } else {
                    // end current line with previous word
                    finishLine(currentLine, textHeight, currentLineWidth);

                    // start new line with current word
                    currentLine = new StringBuilder();
                    currentLine.append(word);
                    currentLineWidth = wordWidth;
                }
            }

            // finish off the last line
            if (currentLineWidth > 0) {
                finishLine(currentLine, textHeight, currentLineWidth);
            }
        }
    }

    public void setY(double y) {
        container.setAttribute(Svg.Y, y);
    }

    public void setTranslateAndRotation(double x, double y, double angle) {
        for (int i = 0; i < container.getChildCount(); i++) {
            container.getChild(i).setAttribute(Svg.TRANSFORM,
                    translate(x, y) + " " + rotate(angle, 0, 0));
        }
    }
}
