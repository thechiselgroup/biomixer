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

public class BoxedTextSvgComponent extends CompositeSvgComponent {

    private SvgElement text;

    private SvgElement box;

    public BoxedTextSvgComponent(SvgElement container, SvgElement text,
            SvgElement box) {
        super(container);
        appendChild(box);
        appendChild(text);
        this.text = text;
        this.box = box;
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

    public void setBoxWidth(double width) {
        box.setAttribute(Svg.WIDTH, width);
    }

    public void setCornerCurveHeight(double cornerCurveHeight) {
        box.setAttribute(Svg.RY, cornerCurveHeight);
    }

    public void setCornerCurveWidth(double cornerCurveWidth) {
        box.setAttribute(Svg.RX, cornerCurveWidth);
    }

    public void setFontColor(String color) {
        text.setAttribute(Svg.FILL, color);
    }

    public void setFontWeight(String fontWeight) {
        text.setAttribute(Svg.FONT_WEIGHT, fontWeight);
    }

    public void setY(double y) {
        compositeElement.setAttribute(Svg.Y, y);
    }

}
