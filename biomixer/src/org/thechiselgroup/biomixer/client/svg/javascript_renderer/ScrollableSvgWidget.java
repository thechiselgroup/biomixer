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
package org.thechiselgroup.biomixer.client.svg.javascript_renderer;


import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides scrolling for a {@link SvgWidget}.
 * 
 * @author drusk
 * 
 */
public class ScrollableSvgWidget extends Widget {

    private Element innerWrapper;

    public ScrollableSvgWidget(SvgWidget containedWidget, int initialWidth,
            int initialHeight) {
        /*
         * NOTE: SvgWidget already has a wrapper div which css gets applied to.
         * To provide scrolling, we add another wrapping div, which gets the css
         * property to cause scrolling when the inner div is larger than it.
         */
        Element outerWrapper = DOM.createDiv();
        outerWrapper.appendChild(containedWidget.getElement());
        setElement(outerWrapper);

        /*
         * SvgWidget already has a wrapper div for css
         */
        innerWrapper = DOM.getChild(outerWrapper, 0);

        /*
         * Inner wrapper is set to be the same size as the SVG document so that
         * the outer div can provide scrolling. Svg element's size doesn't seem
         * to trigger auto scrolling (maybe with the width and height values set
         * explicitly it would).
         */
        Style innerWrapperstyle = innerWrapper.getStyle();
        innerWrapperstyle.setWidth(initialWidth, Unit.PX);
        innerWrapperstyle.setHeight(initialHeight, Unit.PX);

        /*
         * Scroll bars should only appear when the inner wrapper is larger than
         * the outer wrapper
         */
        outerWrapper.getStyle().setOverflow(Overflow.AUTO);
    }

    /*
     * Doing this to avoid GWT assertion about camel case...
     */
    private native void setStyleProperty(Style style, String property,
            String value) /*-{
		style[property] = value;
    }-*/;

    /**
     * Sets styles for making text unselectable on the inner wrapper.
     */
    public void setTextUnselectable() {
        Style style = innerWrapper.getStyle();
        setStyleProperty(style, "-webkit-touch-callout", "none");
        setStyleProperty(style, "-webkit-user-select", "none");
        setStyleProperty(style, "-khtml-user-select", "none");
        setStyleProperty(style, "-moz-user-select", "none");
        setStyleProperty(style, "-ms-user-select", "none");
        setStyleProperty(style, "user-select", "none");
    }

    /**
     * Updates the height of the inner wrapping div around the SVG document.
     * 
     * @param height
     *            the new height of the SVG document
     */
    public void updateHeight(int height) {
        innerWrapper.getStyle().setHeight(height, Unit.PX);
    }

    /**
     * Updates the width of the inner wrapping div around the SVG document.
     * 
     * @param width
     *            the new width of the SVG document
     */
    public void updateWidth(int width) {
        innerWrapper.getStyle().setWidth(width, Unit.PX);
    }

}
