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

import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.SvgPointsBuilder;

public class ExpanderTabFactory {

    private SvgElementFactory svgElementFactory;

    public static final double ARROW_PERCENT_OF_TAB_HEIGHT = 0.5;

    public static final double ARROW_PERCENT_OF_TAB_WIDTH = 0.25;

    public static final double TAB_HEIGHT = 8.0;

    public static final double TAB_WIDTH = 30.0;

    public ExpanderTabFactory(SvgElementFactory svgElementFactory) {
        this.svgElementFactory = svgElementFactory;
    }

    public ExpanderTabSvgComponent createExpanderTabSvgElement() {
        // container for tab-related elements
        SvgElement tab = svgElementFactory.createElement(Svg.SVG);
        tab.setAttribute(Svg.OVERFLOW, Svg.VISIBLE);

        // main portion of tab
        SvgElement rectangle = svgElementFactory.createElement(Svg.RECT);
        rectangle.setAttribute(Svg.X, 0.0);
        rectangle.setAttribute(Svg.Y, 0.0);
        rectangle.setAttribute(Svg.WIDTH, TAB_WIDTH);
        rectangle.setAttribute(Svg.HEIGHT, TAB_HEIGHT);
        rectangle.setAttribute(Svg.FILL, Colors.WHITE);
        rectangle.setAttribute(Svg.STROKE, Colors.BLACK);

        // downward pointing triangle centered on main portion of tab
        SvgElement arrow = svgElementFactory.createElement(Svg.POLYGON);
        double arrowWidth = TAB_WIDTH * ARROW_PERCENT_OF_TAB_WIDTH;
        double arrowHeight = TAB_HEIGHT * ARROW_PERCENT_OF_TAB_HEIGHT;

        SvgPointsBuilder pointsBuilder = new SvgPointsBuilder();
        pointsBuilder
                .addPoint(getHorizontalTabMargin(), getVerticalTabMargin());
        pointsBuilder.addPoint(getHorizontalTabMargin() + arrowWidth,
                getVerticalTabMargin());
        pointsBuilder.addPoint(TAB_WIDTH / 2, getVerticalTabMargin()
                + arrowHeight);

        arrow.setAttribute(Svg.POINTS, pointsBuilder.toPointsString());
        arrow.setAttribute(Svg.FILL, Colors.BLACK);

        return new ExpanderTabSvgComponent(tab, rectangle, arrow);
    }

    private double getHorizontalTabMargin() {
        return getMargin(ARROW_PERCENT_OF_TAB_WIDTH, TAB_WIDTH);
    }

    private double getMargin(double percentOccupied, double available) {
        return ((1 - percentOccupied) / 2) * available;
    }

    private double getVerticalTabMargin() {
        return getMargin(ARROW_PERCENT_OF_TAB_HEIGHT, TAB_HEIGHT);
    }

}
