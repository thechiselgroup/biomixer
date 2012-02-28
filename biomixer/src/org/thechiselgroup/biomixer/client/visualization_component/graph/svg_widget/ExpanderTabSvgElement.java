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

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

public class ExpanderTabSvgElement extends ContainedSvgComponent {

    private SvgElement rectangle;

    private SvgElement arrow;

    private SvgElement container;

    public ExpanderTabSvgElement(SvgElement container, SvgElement rectangle,
            SvgElement arrow) {
        super(container);
        appendChild(rectangle);
        appendChild(arrow);
        this.container = container;
        this.rectangle = rectangle;
        this.arrow = arrow;
    }

    public SvgElement getArrow() {
        return arrow;
    }

    public SvgElement getContainer() {
        return container;
    }

    public PointDouble getLocation() {
        return new PointDouble(Double.parseDouble(container
                .getAttributeAsString(Svg.X)), Double.parseDouble(container
                .getAttributeAsString(Svg.Y)));
    }

    public SvgElement getRectangle() {
        return rectangle;
    }

    public void setBackgroundColor(String color) {
        rectangle.setAttribute(Svg.FILL, color);
    }

    public void setBorderColor(String color) {
        rectangle.setAttribute(Svg.STROKE, color);
    }

    public void setLocation(double x, double y) {
        container.setAttribute(Svg.X, x);
        container.setAttribute(Svg.Y, y);
    }

}
