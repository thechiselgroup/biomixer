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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

public class ExpanderTabSvgComponent implements IsSvg {

    private SvgElement rectangle;

    private SvgElement arrow;

    private SvgElement container;

    public ExpanderTabSvgComponent(SvgElement container, SvgElement rectangle,
            SvgElement arrow) {
        this.container = container;
        container.appendChild(rectangle);
        container.appendChild(arrow);
        this.rectangle = rectangle;
        this.arrow = arrow;
    }

    @Override
    public SvgElement asSvgElement() {
        return container;
    }

    public SvgElement getArrow() {
        return arrow;
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

    public void setEventListener(ChooselEventHandler listener) {
        container.setEventListener(listener);
    }

    public void setLocation(double x, double y) {
        container.setAttribute(Svg.X, x);
        container.setAttribute(Svg.Y, y);
    }

}
