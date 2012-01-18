/*******************************************************************************
 * Copyright 2011 Lars Grammel. All rights reserved.
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.svg.javascript_renderer;

import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class JsDomSvgElementFactory implements SvgElementFactory {

    /**
     * Performance and memory optimization: we use JavaScript overlays instead
     * of wrapper objects.
     */
    static native SvgElement createSvgElement(String tagName)/*-{
		return document.createElementNS("http://www.w3.org/2000/svg", tagName);
    }-*/;

    @Override
    public final SvgElement createElement(String tagName) {
        return createSvgElement(tagName);
    }
}
