/*******************************************************************************
 * Copyright 2011 Lars Grammel. All rights reserved.
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.svg.javascript_renderer;

import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget that renders SVG.
 * 
 * @author Lars Grammel
 * 
 * @see SvgElementFactory
 */
public class SvgWidget extends Widget {

    private final SvgElementFactory svgFactory = new JsDomSvgElementFactory();

    private final SvgElement svgElement;

    public SvgWidget() {
        this.svgElement = svgFactory.createElement(Svg.SVG);

        /*
         * NOTE: we use a wrapper div because CSS classes return objects on SVG
         * DOM nodes (compared to Strings for regular DOM nodes). Without the
         * wrapper div, this would cause getClassName() to throw a
         * ClassCastException when the style of the widget is set.
         */
        Element wrapperDiv = DOM.createDiv();
        wrapperDiv.appendChild((Element) svgElement);
        setElement(wrapperDiv);
    }

    public void clear() {
        Element domElement = (Element) svgElement;
        NodeList<Node> childNodes = domElement.getChildNodes();
        for (int i = childNodes.getLength() - 1; i >= 0; i--) {
            Node node = childNodes.getItem(i);
            domElement.removeChild(node);
        }
    }

    public SvgElement getSvgElement() {
        return svgElement;
    }

    public SvgElementFactory getSvgElementFactory() {
        return svgFactory;
    }

}