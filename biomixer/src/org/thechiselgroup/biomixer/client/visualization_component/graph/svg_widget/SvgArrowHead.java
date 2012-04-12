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
import org.thechiselgroup.biomixer.client.core.geometry.PointUtils;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.shared.svg.PathExpressionBuilder;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.SvgTransforms;

/**
 * An SVG element that forms an arrowhead which is to be placed on a line to
 * give it direction
 * 
 * @author drusk
 * 
 */
public class SvgArrowHead {

    public final static double ARROW_WIDTH = 10.0;

    public final static double ARROW_HEIGHT = 10.0;

    private SvgElement arrow;

    private PointDouble centre;

    public SvgArrowHead(SvgElementFactory svgElementFactory,
            PointDouble sourceLocation, PointDouble targetLocation) {

        this.arrow = svgElementFactory.createElement(Svg.PATH);
        arrow.setAttribute(Svg.STROKE, Colors.BLACK);
        alignWithPoints(sourceLocation, targetLocation);
    }

    /**
     * Repositions the arrow at the centre point between sourcePoint and
     * targetPoint, then rotates it to point in the same direction as a line
     * drawn from sourcePoint to targetPoint
     * 
     * @param sourcePoint
     *            Arrow points away from source
     * @param targetPoint
     *            Arrow points towards target
     */
    public void alignWithPoints(PointDouble sourcePoint, PointDouble targetPoint) {
        centre = PointUtils.getMidPoint(sourcePoint, targetPoint);
        setPathData();
        setRotation(sourcePoint, targetPoint);
    }

    public SvgElement asSvgElement() {
        return arrow;
    }

    private String getPathData() {
        double baseX = centre.getX() - ARROW_WIDTH / 2;
        PathExpressionBuilder pathBuilder = new PathExpressionBuilder();
        return pathBuilder.moveTo(baseX, centre.getY() - ARROW_HEIGHT / 2)
                .lineTo(baseX, centre.getY() + ARROW_HEIGHT / 2)
                .lineTo(centre.getX() + ARROW_WIDTH / 2, centre.getY()).close()
                .toString();
    }

    private void setPathData() {
        arrow.setAttribute(Svg.D, getPathData());
    }

    private void setRotation(PointDouble sourcePoint, PointDouble targetPoint) {
        arrow.setAttribute(Svg.TRANSFORM, SvgTransforms.rotate(
                PointUtils.getRotationAngle(sourcePoint, targetPoint),
                centre.getX(), centre.getY()));
    }

}
