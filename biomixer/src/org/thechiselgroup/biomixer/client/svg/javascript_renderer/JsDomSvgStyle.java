/*******************************************************************************
 * Copyright 2011 Lars Grammel. All rights reserved.
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.svg.javascript_renderer;

import org.thechiselgroup.biomixer.shared.svg.SvgStyle;

import com.google.gwt.core.client.JavaScriptObject;

public class JsDomSvgStyle extends JavaScriptObject implements SvgStyle {

    protected JsDomSvgStyle() {
    }

    @Override
    public final native void setProperty(String attribute, String value) /*-{
		this[attribute] = value;
    }-*/;

}