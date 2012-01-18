/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.ui;

import static com.google.gwt.user.client.DOM.getElementById;
import static com.google.gwt.user.client.DOM.getIntStyleAttribute;
import static com.google.gwt.user.client.DOM.setStyleAttribute;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSize;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Library with convenience methods for working with CSS properties of widgets
 * and DOM elements.
 * 
 * @author Lars Grammel
 */
public final class CSS {

    private static final String WEBKIT_BORDER_RADIUS = "WebkitBorderRadius";

    private static final String MOZ_BORDER_RADIUS = "MozBorderRadius";

    private static final String BORDER_RADIUS = "borderRadius";

    public static final String MARGIN_TOP = "marginTop";

    public static final String MARGIN_RIGHT = "marginRight";

    public static final String BACKGROUND_COLOR = "backgroundColor";

    public static final String FLOAT = "cssFloat";

    public static final String LINE_HEIGHT = "lineHeight";

    public static final String WHITE_SPACE = "whiteSpace";

    public static final String NOWRAP = "nowrap";

    public static final String INLINE = "inline";

    public static final String DISPLAY = "display";

    public static final String FONT_SIZE = "fontSize";

    public static final String ABSOLUTE = "absolute";

    public static final String CURSOR = "cursor";

    public static final String CURSOR_DEFAULT = "default";

    public static final String CURSOR_POINTER = "pointer";

    public static final String HEIGHT = "height";

    public static final String HIDDEN = "hidden";

    public static final String LEFT = "left";

    public static final String MAX_WIDTH = "maxWidth";

    public static final String OVERFLOW = "overflow";

    public static final String POSITION = "position";

    public static final String PX = "px";

    public static final String COLOR = "color";

    public static final String RELATIVE = "relative";

    public static final String TOP = "top";

    public static final String WIDTH = "width";

    public static final String Z_INDEX = "zIndex";

    public static final String BORDER_COLOR = "borderColor";

    public static final String BORDER_BOTTOM = "borderBottom";

    public static final String FONT_WEIGHT = "fontWeight";

    public static final String FONT_STYLE = "fontStyle";

    public static final String FONT_FAMILY = "fontFamily";

    public static final String BORDER = "border";

    public static final String PADDING = "padding";

    public static final String MARGIN = "margin";

    public static final String FIXED = "fixed";

    public static final String INLINE_BLOCK = "inline-block";

    public static void addClass(String elementID, String cssClass) {
        Element element = getElementById(elementID);
        /*
         * workaround for bug where class name is null
         */
        if (element.getClassName() == null) {
            element.setClassName("");
        }

        element.addClassName(cssClass);
    }

    public static void clearHeight(Widget widget) {
        widget.getElement().getStyle().clearHeight();
    }

    /**
     * @return size of {@code element} by calling getBoundingClientRect(). Works
     *         in Internet Explorer 8+, Firefox 3+, Google Chrome, Opera 9.5+,
     *         Safari 4+
     * 
     * @see http://help.dottoro.com/ljvmcrrn.php
     */
    public static SizeInt getBoundingClientSize(Element element) {
        JsRect rect = jsGetBoundingClientRect(element);
        return new DefaultSize(rect.getRight() - rect.getLeft(),
                rect.getBottom() - rect.getTop());
    }

    /**
     * Returns the actual (computed) style for an element. This is necessary,
     * because the regular style accessor returns an empty String if the style
     * was not explicitly set on the corresponding element.
     * 
     * @param el
     *            DOM element
     * @param styleProp
     *            Camelized style property (i.e. like 'fontStyle')
     */
    // @formatter:off
    public static native String getComputedStyle(Element el, String styleProp)/*-{
        var dash = function(str){
          return str.replace(/[A-Z]/g, function(str) {
            return "-" + str.toLowerCase();
          });
        }

        if (el.currentStyle) {
          return el.currentStyle[styleProp];
        } else if ($wnd.document.defaultView && $wnd.document.defaultView.getComputedStyle) {
          return $wnd.document.defaultView.getComputedStyle(el, null).getPropertyValue(dash(styleProp));
        } else {
          return el.style[styleProp]; 
        }
    }-*/;
    // @formatter:on

    public static int getZIndex(Element element) {
        return getIntStyleAttribute(element, Z_INDEX);
    }

    // @formatter:off
    private static native JsRect jsGetBoundingClientRect(Element element) /*-{
        return element.getBoundingClientRect();
    }-*/;
    // @formatter:on

    public static void removeClass(String cssClass, String elementID) {
        Element element = getElementById(elementID);
        /*
         * workaround for bug where class name is null
         */
        if (element.getClassName() == null) {
            element.setClassName("");
        }
        element.removeClassName(cssClass);
    }

    public static void setAbsoluteBounds(Element element, int left, int top,
            int width, int height) {

        setPosition(element, ABSOLUTE);
        setLocation(element, left, top);
        setSize(element, width, height);
    }

    public static void setBackgroundColor(Element element, String color) {
        setStyleAttribute(element, BACKGROUND_COLOR, color);
    }

    public static void setBackgroundColor(String elementID, String color) {
        setBackgroundColor(getElementById(elementID), color);
    }

    public static void setBackgroundColor(Widget widget, String color) {
        setBackgroundColor(widget.getElement(), color);
    }

    public static void setBorder(Element element, String border) {
        element.getStyle().setProperty(BORDER, border);
    }

    public static void setBorderBottom(Element element, String borderBottom) {
        setStyleAttribute(element, BORDER_BOTTOM, borderBottom);
    }

    public static void setBorderBottom(String elementID, String borderBottom) {
        setBorderBottom(getElementById(elementID), borderBottom);
    }

    public static void setBorderColor(Element element, String borderColor) {
        setStyleAttribute(element, BORDER_COLOR, borderColor);
    }

    public static void setBorderColor(Widget widget, String borderColor) {
        setBorderColor(widget.getElement(), borderColor);
    }

    public static void setBorderRadius(Element element, int borderRadius) {
        assert element != null;

        String value = borderRadius + PX;
        DOM.setStyleAttribute(element, BORDER_RADIUS, value);
        DOM.setStyleAttribute(element, MOZ_BORDER_RADIUS, value);
        DOM.setStyleAttribute(element, WEBKIT_BORDER_RADIUS, value);
    }

    public static void setColor(Element element, String color) {
        element.getStyle().setProperty(COLOR, color);
    }

    public static void setColor(String id, String color) {
        setColor(getElementById(id), color);
    }

    public static void setColor(Widget widget, String color) {
        setColor(widget.getElement(), color);
    }

    public static void setDisplay(Element element, String value) {
        element.getStyle().setProperty(DISPLAY, value);
    }

    public static void setFloat(Element element, String value) {
        element.getStyle().setProperty(FLOAT, value);
    }

    public static void setFontFamily(Element estimatorElement, String fontFamily) {
        estimatorElement.getStyle().setProperty(FONT_FAMILY, fontFamily);
    }

    public static void setFontSize(Element element, String fontSize) {
        element.getStyle().setProperty(FONT_SIZE, fontSize);
    }

    public static void setFontStyle(Element estimatorElement, String fontStyle) {
        estimatorElement.getStyle().setProperty(FONT_STYLE, fontStyle);
    }

    public static void setFontWeight(Element estimatorElement, String fontWeight) {
        estimatorElement.getStyle().setProperty(FONT_WEIGHT, fontWeight);
    }

    public static void setHeight(Element element, int heightPx) {
        setStyleAttribute(element, HEIGHT, heightPx + PX);
    }

    public static void setHeight(Element element, String height) {
        element.getStyle().setProperty(HEIGHT, height);
    }

    public static void setHeight(Widget widget, int heightPx) {
        setHeight(widget.getElement(), heightPx);
    }

    public static void setLeft(Element element, int left) {
        setStyleAttribute(element, LEFT, left + PX);
    }

    public static void setLineHeight(Element element, int lineHeight) {
        setStyleAttribute(element, LINE_HEIGHT, lineHeight + PX);
    }

    public static void setLocation(Element element, int left, int top) {
        setStyleAttribute(element, LEFT, left + PX);
        setStyleAttribute(element, TOP, top + PX);
    }

    public static void setLocation(Widget widget, int left, int top) {
        setLocation(widget.getElement(), left, top);
    }

    public static void setMargin(Element element, int margin) {
        element.getStyle().setProperty(MARGIN, margin + PX);
    }

    public static void setMarginRightPx(Widget widget, int marginPx) {
        setStyleAttribute(widget.getElement(), MARGIN_RIGHT, marginPx + PX);
    }

    public static void setMarginTopPx(Widget widget, int marginPx) {
        setStyleAttribute(widget.getElement(), MARGIN_TOP, marginPx + PX);
    }

    public static void setMaxWidth(Element element, int maxWidth) {
        setStyleAttribute(element, MAX_WIDTH, maxWidth + PX);
    }

    public static void setMaxWidth(Widget widget, int maxWidth) {
        setMaxWidth(widget.getElement(), maxWidth);
    }

    public static void setPadding(Element element, int padding) {
        element.getStyle().setProperty(PADDING, padding + PX);
    }

    public static void setPosition(Element element, String position) {
        setStyleAttribute(element, POSITION, position);
    }

    public static void setSize(Element element, int width, int height) {
        setWidth(element, width);
        setStyleAttribute(element, HEIGHT, height + PX);
    }

    public static void setTop(Element element, int top) {
        element.getStyle().setProperty(TOP, (top + PX));
    }

    public static void setWhitespace(Element element, String value) {
        element.getStyle().setProperty(WHITE_SPACE, value);
    }

    public static void setWidth(Element element, int width) {
        setWidth(element, (width + PX));
    }

    public static void setWidth(Element element, String width) {
        element.getStyle().setProperty(WIDTH, width);
    }

    public static void setWidth(Widget widget, int width) {
        setWidth(widget.getElement(), width);
    }

    public static void setZIndex(Element element, int zIndex) {
        element.getStyle().setProperty(Z_INDEX, Integer.toString(zIndex));
    }

    public static void setZIndex(String elementID, int zIndex) {
        setZIndex(getElementById(elementID), zIndex);
    }

    public static void setZIndex(Widget widget, int zIndex) {
        setZIndex(widget.getElement(), zIndex);
    }

    private CSS() {
        // library
    }

}