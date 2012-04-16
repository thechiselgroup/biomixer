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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg;

import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

/**
 * Rendered background of the SVG graph viewer.
 * 
 * @author drusk
 * 
 */
public class SvgGraphBackground {

    private SvgElement svgElement;

    public SvgGraphBackground(int width, int height,
            SvgElementFactory svgElementFactory) {
        svgElement = svgElementFactory.createElement(Svg.RECT);
        svgElement.setAttribute(Svg.WIDTH, width);
        svgElement.setAttribute(Svg.HEIGHT, height);
        svgElement.setAttribute(Svg.FILL, Colors.WHITE);
    }

    public SvgElement asSvg() {
        return svgElement;
    }

    public double getHeight() {
        return Double.parseDouble(svgElement.getAttributeAsString(Svg.HEIGHT));
    }

    public double getWidth() {
        return Double.parseDouble(svgElement.getAttributeAsString(Svg.WIDTH));
    }

    public void setEventListener(ChooselEventHandler handler) {
        svgElement.setEventListener(handler);
    }

    public void setHeight(double height) {
        svgElement.setAttribute(Svg.HEIGHT, height);
    }

    public void setWidth(double width) {
        svgElement.setAttribute(Svg.WIDTH, width);
    }

}
