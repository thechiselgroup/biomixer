package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes;

import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class SvgCircleWithText {
    private static final double THRESHOLD_TEXT_LENGTH = 150.0;

    public static final String DEFAULT_FONT_FAMILY = "Arial, sans-serif";

    public static final String DEFAULT_FONT_SIZE_PIXELS = "12px";

    public static final String DEFAULT_FONT_STYLE = "normal";

    public static final String DEFAULT_FONT_WEIGHT = "normal";

    public static final double TEXT_BUFFER_X = 12.0;

    public static final double TEXT_BUFFER_Y = 5.0;

    private SvgElement textElement;

    private SvgElement boxElement;

    private TextBoundsEstimator textBoundsEstimator;

    private SvgElementFactory svgElementFactory;

    private String fontFamily = DEFAULT_FONT_FAMILY;

    private String fontSize = DEFAULT_FONT_SIZE_PIXELS;

    private String fontStyle = DEFAULT_FONT_STYLE;

    private String fontWeight = DEFAULT_FONT_WEIGHT;

    private final String text;

    private int numberOfLines = 0;

    /*
     * Stores the tspan elements for multiple lines of text. Will be empty if
     * the text fits on one line and therefore doesn't need any tspans.
     */
    private Map<String, SvgElement> tspanElements = CollectionFactory
            .createStringMap();

    private String longestTextLine;

    private SvgElement container;

    public SvgCircleWithText(String text,
            TextBoundsEstimator textBoundsEstimator,
            SvgElementFactory svgElementFactory) {
        container = svgElementFactory.createElement(Svg.SVG);
        container.setAttribute(Svg.OVERFLOW, Svg.VISIBLE);
        this.textBoundsEstimator = textBoundsEstimator;
        this.svgElementFactory = svgElementFactory;
        this.text = text;
        createCircleWithText();
    }

    public SvgElement asSvgElement() {
        return container;
    }

    private void centreTextElements() {
        for (Entry<String, SvgElement> entry : tspanElements.entrySet()) {
            double x = getBoxLeftX() + TEXT_BUFFER_X;
            entry.getValue().setAttribute(Svg.X, x);
        }
    }

    private void createCircleWithText() {
        textElement = svgElementFactory.createElement(Svg.TEXT);
        setTextContent();
        setDefaultFontValues(textElement);

        boxElement = svgElementFactory.createElement(Svg.CIRCLE);
        setDefaultBoxValues(boxElement);

        setBoxAroundText();

        container.appendChild(boxElement);
        container.appendChild(textElement);
    }

    private void finishLine(StringBuilder currentLine, int textHeight,
            int currentLineWidth) {
        SvgElement tspan = svgElementFactory.createElement(Svg.TSPAN);

        String line = currentLine.toString().trim();
        tspan.setTextContent(line);

        tspan.setAttribute(Svg.X, TEXT_BUFFER_X);
        int dy = numberOfLines == 0 ? 0 : textHeight;
        tspan.setAttribute(Svg.DY, dy);

        textElement.appendChild(tspan);
        tspanElements.put(line, tspan);

        if (currentLineWidth > getWidthOfLongestTextLine()) {
            longestTextLine = line;
        }

        numberOfLines++;
    }

    private double getBoxHeight() {
        return Double.parseDouble(boxElement.getAttributeAsString(Svg.R));
    }

    private double getBoxLeftX() {
        return Double.parseDouble(boxElement.getAttributeAsString(Svg.CX));
    }

    private double getBoxWidth() {
        return Double.parseDouble(boxElement.getAttributeAsString(Svg.R));
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

    private double getWidthOfLongestTextLine() {
        if (longestTextLine == null) {
            return 0.0;
        } else {
            return getTextSize(longestTextLine).getWidth();
        }
    }

    public void setBackgroundColor(String color) {
        boxElement.setAttribute(Svg.FILL, color);
    }

    public void setBorderColor(String color) {
        boxElement.setAttribute(Svg.STROKE, color);
    }

    private void setBoxAroundText() {
        setCircleRadius(10);
        if (numberOfLines == 1) {
            textElement.setAttribute(Svg.X, getBoxLeftX() + TEXT_BUFFER_X);
        } else {
            centreTextElements();
        }

        /*
         * the y-position of the text refers to the bottom of the FIRST LINE of
         * text
         */
        textElement.setAttribute(Svg.Y, TEXT_BUFFER_Y);
    }

    private void setBoxX(double x) {
        boxElement.setAttribute(Svg.CX, x);
    }

    public void setCircleRadius(double radius) {
        boxElement.setAttribute(Svg.R, radius);
    }

    private void setDefaultBoxValues(SvgElement boxElement) {
        boxElement.setAttribute(Svg.FILL, Colors.WHITE);
        boxElement.setAttribute(Svg.STROKE, Colors.BLACK);
        boxElement.setAttribute(Svg.CX, 0.0);
        boxElement.setAttribute(Svg.CY, 0.0);
    }

    private void setDefaultFontValues(SvgElement textElement) {
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
        double oldWidth = getWidthOfLongestTextLine();
        this.fontWeight = fontWeight;
        textElement.setAttribute(Svg.FONT_WEIGHT, fontWeight);
        double newWidth = getWidthOfLongestTextLine();
        updateBoxWidthAndPositionAroundText(newWidth - oldWidth);
    }

    private void setTextContent() {
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

    private void updateBoxWidthAndPositionAroundText(double deltaWidth) {
        setBoxX(getBoxLeftX() - (deltaWidth / 2));
        setBoxAroundText();
        centreTextElements();
    }
}
