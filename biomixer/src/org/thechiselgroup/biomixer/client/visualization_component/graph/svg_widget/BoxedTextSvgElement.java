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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget;

import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

public class BoxedTextSvgElement {

    private SvgElement container;

    private SvgElement text;

    private SvgElement box;

    public BoxedTextSvgElement(SvgElement container, SvgElement text,
            SvgElement box) {
        this.container = container;
        this.text = text;
        this.box = box;
    }

    public SvgElement getBox() {
        return box;
    }

    public SvgElement getContainer() {
        return container;
    }

    public SvgElement getText() {
        return text;
    }

    public double getTextHeight() {
        return Double.parseDouble(text.getAttributeAsString(Svg.HEIGHT));
    }

    public double getTextWidth() {
        return Double.parseDouble(text.getAttributeAsString(Svg.WIDTH));
    }

    public double getTotalHeight() {
        // TODO use BBox on container?
        return Double.parseDouble(box.getAttributeAsString(Svg.HEIGHT));
    }

    public double getTotalWidth() {
        // TODO use BBox on container?
        return Double.parseDouble(box.getAttributeAsString(Svg.WIDTH));
    }

    public void setBackgroundColor(String color) {
        box.setAttribute(Svg.FILL, color);
    }

    public void setBorderColor(String color) {
        box.setAttribute(Svg.STROKE, color);
    }

    public void setFontColor(String color) {
        text.setAttribute(Svg.FILL, color);
    }

    public void setFontWeight(String fontWeight) {
        text.setAttribute(Svg.FONT_WEIGHT, fontWeight);
    }

}
