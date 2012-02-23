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
package org.thechiselgroup.biomixer.shared.svg;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;

public class SvgUtils {

    public static void setX1Y1(SvgElement svgElement, PointDouble point) {
        svgElement.setAttribute(Svg.X1, point.getX());
        svgElement.setAttribute(Svg.Y1, point.getY());
    }

    public static void setX2Y2(SvgElement svgElement, PointDouble point) {
        svgElement.setAttribute(Svg.X2, point.getX());
        svgElement.setAttribute(Svg.Y2, point.getY());
    }

    public static void setXY(SvgElement svgElement, PointDouble point) {
        svgElement.setAttribute(Svg.X, point.getX());
        svgElement.setAttribute(Svg.Y, point.getY());
    }

}
