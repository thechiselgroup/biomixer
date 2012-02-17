/*******************************************************************************
 * Copyright 2011 Lars Grammel. All rights reserved.
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.svg.javascript_renderer;

import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgStyle;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 * <p>
 * Performance and memory optimization: we use JavaScript overlays instead of
 * wrapper objects.
 * </p>
 */
public class JsDomSvgElement extends JavaScriptObject implements SvgElement {

    private static class JsDomBBoxWrapper implements SizeDouble {

        private final JsDomSvgElement element;

        public JsDomBBoxWrapper(JsDomSvgElement element) {
            this.element = element;
        }

        @Override
        public double getHeight() {
            return element.getBBoxHeight();
        }

        @Override
        public double getWidth() {
            return element.getBBoxWidth();
        }

    }

    protected JsDomSvgElement() {
    }

    @Override
    public final native SvgElement appendChild(SvgElement newChild) /*-{
		return this.appendChild(newChild);
    }-*/;

    @Override
    public final native String getAttributeAsString(String name)/*-{
		return this.getAttribute(name);
    }-*/;

    @Override
    public final SizeDouble getBBox() {
        if (getBBoxWrapper() == null) {
            setBBoxWrapper(new JsDomBBoxWrapper(this));
        }

        return getBBoxWrapper();
    }

    private final native int getBBoxHeight() /*-{
		return this.getBBox().height;
    }-*/;

    private final native int getBBoxWidth() /*-{
		return this.getBBox().width;
    }-*/;

    private final native JsDomBBoxWrapper getBBoxWrapper() /*-{
		return this._bboxWrapper;
    }-*/;

    @Override
    public final native SvgElement getChild(int childIndex) /*-{
		return this.childNodes[childIndex];
    }-*/;

    @Override
    public final native int getChildCount() /*-{
		return this.childNodes.length;
    }-*/;

    @Override
    public final native SvgStyle getStyle() /*-{
		return this.style;
    }-*/;

    @Override
    public final native boolean hasAttribute(String attribute) /*-{
		return this.hasAttribute(attribute);
    }-*/;

    @Override
    public final native SvgElement insertBefore(SvgElement newChild,
            SvgElement refChild) /*-{
		return this.insertBefore(newChild, refChild);
    }-*/;

    // @formatter:off
    @Override
    public final native void removeAllChildren()/*-{
		while (this.hasChildNodes()) {
			this.removeChild(this.firstChild);
		}
    }-*/;
    // @formatter:on

    @Override
    public final native void removeAttribute(String attribute) /*-{
		if (this.hasAttribute(attribute)) {
			this.removeAttribute(attribute);
		}
    }-*/;

    @Override
    public final native SvgElement removeChild(SvgElement oldChild) /*-{
		return this.removeChild(oldChild);
    }-*/;

    @Override
    public final native void setAttribute(String name, double value) /*-{
		this.setAttribute(name, value);
    }-*/;

    @Override
    public final native void setAttribute(String name, String value) /*-{
		this.setAttribute(name, value);
    }-*/;

    private final native void setBBoxWrapper(JsDomBBoxWrapper wrapper) /*-{
		this._bboxWrapper = wrapper;
    }-*/;

    @Override
    public final void setEventListener(final ChooselEventHandler handler) {
        /*
         * This class cast hack works because this class is only used as
         * compiled JavaScript.
         */
        Element asElement = (Element) ((SvgElement) this);
        DOM.sinkEvents(asElement, Event.MOUSEEVENTS | Event.ONMOUSEWHEEL
                | Event.TOUCHEVENTS | Event.GESTUREEVENTS | Event.ONCLICK
                | Event.ONDBLCLICK | Event.ONCONTEXTMENU);
        DOM.setEventListener(asElement, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                handler.onEvent(new ChooselEvent(event));
            }
        });
    }

    @Override
    public final native void setTextContent(String text) /*-{
		this.textContent = text;
    }-*/;

}