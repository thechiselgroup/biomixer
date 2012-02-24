package org.thechiselgroup.biomixer.client.core.util.text;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeInt;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.ui.CSS;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

public class SvgBBoxTextBoundsEstimator implements TextBoundsEstimator {

    private SvgElementFactory elementFactory;

    private final Element rootElement;

    private SvgElement svgElement;

    private SvgElement textElement;

    private SizeDouble textElementBBox;

    public SvgBBoxTextBoundsEstimator(SvgElementFactory elementFactory) {
        assert elementFactory != null;

        this.elementFactory = elementFactory;
        this.rootElement = RootPanel.get().getElement();
    }

    @Override
    public void configureFontFamily(String fontFamily) {
        CSS.setFontFamily((Element) svgElement, fontFamily);
    }

    @Override
    public void configureFontSize(String fontSize) {
        CSS.setFontSize((Element) svgElement, fontSize);
    }

    @Override
    public void configureFontStyle(String fontStyle) {
        CSS.setFontStyle((Element) svgElement, fontStyle);
    }

    @Override
    public void configureFontWeight(String fontWeight) {
        CSS.setFontWeight((Element) svgElement, fontWeight);
    }

    @Override
    public int getHeight(String text) {
        setText(text);
        return (int) textElementBBox.getHeight();
    }

    @Override
    public SizeInt getSize(String text) {
        setText(text);
        return new DefaultSizeInt((int) textElementBBox.getWidth(),
                (int) textElementBBox.getHeight());
    }

    @Override
    public int getWidth(String text) {
        setText(text);
        return (int) textElementBBox.getWidth();
    }

    private void setText(String text) {
        textElement.setTextContent(text);
    }

    @Override
    public void setUp() {
        svgElement = elementFactory.createElement(Svg.SVG);
        textElement = elementFactory.createElement(Svg.TEXT);
        textElementBBox = textElement.getBBox();
        svgElement.appendChild(textElement);
        rootElement.appendChild((Element) svgElement);
    }

    @Override
    public void tearDown() {
        rootElement.removeChild((Element) svgElement);
    }
}
