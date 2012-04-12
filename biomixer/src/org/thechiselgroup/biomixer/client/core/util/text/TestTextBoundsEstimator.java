package org.thechiselgroup.biomixer.client.core.util.text;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeInt;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;

public class TestTextBoundsEstimator implements TextBoundsEstimator {

    private int height;

    private int widthFactor;

    public TestTextBoundsEstimator(int widthFactor, int height) {
        this.widthFactor = widthFactor;
        this.height = height;
    }

    @Override
    public void configureFontFamily(String fontFamily) {
    }

    @Override
    public void configureFontSize(String fontSize) {
    }

    @Override
    public void configureFontStyle(String fontStyle) {
    }

    @Override
    public void configureFontWeight(String fontWeight) {
    }

    @Override
    public int getHeight(String text) {
        return height;
    }

    @Override
    public SizeInt getSize(String text) {
        return new DefaultSizeInt(getWidth(text), getHeight(text));
    }

    @Override
    public int getWidth(String text) {
        return text.length() * widthFactor;
    }

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
    }
}