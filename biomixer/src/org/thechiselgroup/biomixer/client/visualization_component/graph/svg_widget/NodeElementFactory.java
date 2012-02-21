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
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class NodeElementFactory {

    public static final double TEXT_BUFFER = 10.0;

    public static final double RX_DEFAULT = 10.0;

    public static final double RY_DEFAULT = 10.0;

    public static final double TAB_WIDTH = 30.0;

    public static final double TAB_HEIGHT = 8.0;

    public static final double ARROW_PERCENT_OF_TAB_WIDTH = 0.25;

    public static final double ARROW_PERCENT_OF_TAB_HEIGHT = 0.5;

    public static final double DEFAULT_NODE_BOX_WIDTH = 100.0;

    public static final double DEFAULT_NODE_BOX_HEIGHT = 40.0;

    private SvgElementFactory svgElementFactory;

    public NodeElementFactory(SvgElementFactory svgElementFactory) {
        this.svgElementFactory = svgElementFactory;
    }

    private SvgElement createExpanderTab() {
        // container for tab-related elements
        SvgElement tab = svgElementFactory.createElement(Svg.SVG);

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

        tab.appendChild(rectangle);
        tab.appendChild(arrow);

        return tab;
    }

    public NodeElement createNodeElement(final Node node) {
        assert node != null;

        SvgElement baseContainer = svgElementFactory.createElement(Svg.SVG);
        baseContainer.setAttribute(Svg.ID, node.getId());
        baseContainer.setAttribute(Svg.X, 0.0);
        baseContainer.setAttribute(Svg.Y, 0.0);

        SvgElement text = svgElementFactory.createElement(Svg.TEXT);
        text.setTextContent(node.getLabel());
        text.setAttribute(Svg.X, 0.0);
        text.setAttribute(Svg.Y, 0.0);

        // XXX causing errors. Because not rendered yet?
        // SizeDouble textBBox = text.getBBox();
        // double textWidth = textBBox.getWidth();
        // double textHeight = textBBox.getHeight();

        SvgElement nodeBox = svgElementFactory.createElement(Svg.RECT);
        nodeBox.setAttribute(Svg.FILL, "white");
        // TODO proper colors
        nodeBox.setAttribute(Svg.STROKE, "black");
        nodeBox.setAttribute(Svg.RX, RX_DEFAULT);
        nodeBox.setAttribute(Svg.RY, RY_DEFAULT);

        // TODO currently using default width and height. Should be determined
        // based on text size.
        // rectangle.setAttribute(Svg.WIDTH, textWidth + 2 * TEXT_BUFFER);
        // rectangle.setAttribute(Svg.HEIGHT, textHeight + 2 * TEXT_BUFFER);
        nodeBox.setAttribute(Svg.WIDTH, DEFAULT_NODE_BOX_WIDTH);
        nodeBox.setAttribute(Svg.HEIGHT, DEFAULT_NODE_BOX_HEIGHT);
        nodeBox.setAttribute(Svg.X, 0.0);
        nodeBox.setAttribute(Svg.Y, 0.0);

        // text.setAttribute(Svg.X, TEXT_BUFFER);
        // text.setAttribute(Svg.Y, TEXT_BUFFER);
        text.setAttribute(Svg.X, 10.0);
        text.setAttribute(Svg.Y, 20.0);

        SvgElement nodeContainer = svgElementFactory.createElement(Svg.SVG);
        nodeContainer.appendChild(nodeBox);
        nodeContainer.appendChild(text);

        SvgElement expanderTab = createExpanderTab();
        // XXX find bounds of nodeBox (getBBox? not working) and use them
        expanderTab.setAttribute(Svg.X,
                (DEFAULT_NODE_BOX_WIDTH - TAB_WIDTH) / 2);
        expanderTab.setAttribute(Svg.Y, DEFAULT_NODE_BOX_HEIGHT);

        baseContainer.appendChild(nodeContainer);
        baseContainer.appendChild(expanderTab);

        return new NodeElement(node, baseContainer, nodeContainer, nodeBox,
                text, expanderTab);
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
