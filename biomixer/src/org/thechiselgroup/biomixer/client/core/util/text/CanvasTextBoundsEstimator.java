/*******************************************************************************
 * Copyright 2011 Lars Grammel. All rights reserved.
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.core.util.text;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeInt;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;

/**
 * Uses Canvas text metrics for width and delegate for height.
 * 
 * @author Lars Grammel
 */
public class CanvasTextBoundsEstimator implements TextBoundsEstimator {

    // TODO extract Font class

    private TextBoundsEstimator heightEstimator;

    private String fontFamily;

    private String fontSize;

    private Context2d context;

    private String fontStyle;

    private String fontWeight;

    public CanvasTextBoundsEstimator(TextBoundsEstimator heightEstimator) {
        this.heightEstimator = heightEstimator;
    }

    private void configureFont() {
        /*
         * NOTE order important, should be CSS font order:
         * 
         * "font-style font-variant font-weight font-size/line-height font-family"
         * 
         * http://www.w3schools.com/cssref/pr_font_font.asp
         */
        StringBuilder sb = new StringBuilder();

        if (fontStyle != null) {
            sb.append(fontStyle).append(' ');
        }

        if (fontWeight != null) {
            sb.append(fontWeight).append(' ');
        }

        if (fontSize != null) {
            sb.append(fontSize).append(' ');
        }

        if (fontFamily != null) {
            sb.append("'").append(fontFamily).append("'");
        }

        context.setFont(sb.toString());
    }

    @Override
    public void configureFontFamily(String fontFamily) {
        heightEstimator.configureFontFamily(fontFamily);
        this.fontFamily = fontFamily;
        configureFont();
    }

    @Override
    public void configureFontSize(String fontSize) {
        heightEstimator.configureFontSize(fontSize);
        this.fontSize = fontSize;
        configureFont();
    }

    @Override
    public void configureFontStyle(String fontStyle) {
        heightEstimator.configureFontStyle(fontStyle);
        this.fontStyle = fontStyle;
        configureFont();
    }

    @Override
    public void configureFontWeight(String fontWeight) {
        heightEstimator.configureFontWeight(fontWeight);
        this.fontWeight = fontWeight;
        configureFont();
    }

    @Override
    public int getHeight(String text) {
        return heightEstimator.getHeight(text);
    }

    @Override
    public SizeInt getSize(String text) {
        return new DefaultSizeInt(getWidth(text), getHeight(text));
    }

    @Override
    public int getWidth(String text) {
        return (int) context.measureText(text).getWidth();
    }

    @Override
    public void setUp() {
        heightEstimator.setUp();
        context = Canvas.createIfSupported().getContext2d();
    }

    @Override
    public void tearDown() {
        heightEstimator.tearDown();
    }

}