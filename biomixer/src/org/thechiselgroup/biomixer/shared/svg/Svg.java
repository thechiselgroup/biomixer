/*******************************************************************************
 * Copyright 2012 Lars Grammel 
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

/**
 * Contains SVG attribute names, tag names etc as constants.
 * 
 * @author Lars Grammel
 */
public final class Svg {

    /**
     * SVG Namespace
     */
    public final static String NAMESPACE = "http://www.w3.org/2000/svg";

    public static final String HEIGHT = "height";

    public static final String WIDTH = "width";

    public static final String Y = "y";

    public static final String X = "x";

    public static final String STROKE_WIDTH = "stroke-width";

    public static final String STROKE = "stroke";

    public static final String RECT = "rect";

    public static final String FILL = "fill";

    public static final String TRANSFORM = "transform";

    public static final String D = "d";

    public static final String RY = "ry";

    public static final String RX = "rx";

    public static final String CY = "cy";

    public static final String CX = "cx";

    public static final String ELLIPSE = "ellipse";

    public static final String CIRCLE = "circle";

    public static final String G = "g";

    public static final String PATH = "path";

    public static final String SVG = "svg";

    public static final String FILL_OPACITY = "fill-opacity";

    public static final String R = "r";

    public static final String STROKE_OPACITY = "stroke-opacity";

    public static final String TEXT = "text";

    public static final String FONT_WEIGHT = "font-weight";

    public static final String DEFS = "defs";

    public static final String STOP_COLOR = "stop-color";

    public static final String OFFSET = "offset";

    public static final String STOP = "stop";

    public static final String LINEAR_GRADIENT = "linearGradient";

    public static final String ID = "id";

    public static final String LINE = "line";

    public static final String X1 = "x1";

    public static final String X2 = "x2";

    public static final String Y1 = "y1";

    public static final String Y2 = "y2";

    public static final String FONT_SIZE = "font-size";

    public static final String FONT_FAMILY = "font-family";

    public static final String POLYGON = "polygon";

    public static final String POINTS = "points";

    public static String localUrl(String id) {
        return "url(#" + id + ")";
    }

    private Svg() {

    }

}