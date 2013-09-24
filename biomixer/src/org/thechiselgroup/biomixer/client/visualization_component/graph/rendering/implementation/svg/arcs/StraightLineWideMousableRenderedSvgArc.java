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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.arcs;

import java.util.HashMap;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.PointUtils;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.graph.CompositionArcType;
import org.thechiselgroup.biomixer.client.graph.ConceptArcType;
import org.thechiselgroup.biomixer.client.graph.DirectConceptMappingArcType;
import org.thechiselgroup.biomixer.client.graph.MappingArcType;
import org.thechiselgroup.biomixer.client.graph.OntologyMappingArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes.SvgBareText;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.SvgUtils;

/**
 * An SVG arc rendered as a straight line, but with another line on top that is
 * transparent. This allows the mouse to be positioned over the arc more easily,
 * without making the line visibly wider. This facilitates viewing popups for
 * arcs.
 * 
 * 
 */
public class StraightLineWideMousableRenderedSvgArc extends
        AbstractSvgRenderedArc implements Identifiable {

    private final SvgElement arcLine;

    private final SvgElement arcLineTransparentOverlay;

    private final SvgArrowHead arrow;

    private SvgElement container;

    private SvgBareText label;

    private Boolean labelRendering = null;

    private Double MIN_THICKNESS_PAD = 8.0;

    static HashMap<String, Integer> nodePositionOffsetMap = new HashMap<String, Integer>();
    {
        nodePositionOffsetMap.put(ConceptArcType.ID, -6);
        nodePositionOffsetMap.put(CompositionArcType.ID, 6);
        nodePositionOffsetMap.put(DirectConceptMappingArcType.ID, 3);
        nodePositionOffsetMap.put(MappingArcType.ID, -3);
        // Small ontology nodes prevent us from offsetting.
        nodePositionOffsetMap.put(OntologyMappingArcType.ID, 0);
    }

    public StraightLineWideMousableRenderedSvgArc(Arc arc, boolean renderLabel,
            RenderedNode source, RenderedNode target,
            SvgElementFactory svgElementFactory,
            TextBoundsEstimator textBoundsEstimator) {
        super(arc, source, target);

        this.container = svgElementFactory.createElement(Svg.G);
        container.setAttribute(Svg.ID, arc.getId());

        PointDouble tempSource = source.getNodeShapeCentre();
        PointDouble sourceNodeLocation = this
                .preventArcOverlap(source.getNodeShapeCentre(),
                        target.getNodeShapeCentre(), false);
        PointDouble targetNodeLocation = this.preventArcOverlap(
                target.getNodeShapeCentre(), tempSource, true);

        this.arcLine = svgElementFactory.createElement(Svg.LINE);
        arcLine.setAttribute(Svg.X1, sourceNodeLocation.getX());
        arcLine.setAttribute(Svg.Y1, sourceNodeLocation.getY());
        arcLine.setAttribute(Svg.X2, targetNodeLocation.getX());
        arcLine.setAttribute(Svg.Y2, targetNodeLocation.getY());
        arcLine.setAttribute(Svg.STROKE, Colors.BLACK);
        container.appendChild(arcLine);

        // used to skip undirected arc heads
        this.arrow = new SvgArrowHead(svgElementFactory, sourceNodeLocation,
                targetNodeLocation);
        container.appendChild(arrow.asSvgElement());

        // Create label
        this.label = new SvgBareText(arc.getLabel(), textBoundsEstimator,
                svgElementFactory);

        this.setLabelRendering(renderLabel);

        // Make the two transparent border arcs, and make their parent that of
        // the main arc.
        this.arcLineTransparentOverlay = svgElementFactory
                .createElement(Svg.LINE);
        this.arcLineTransparentOverlay.setAttribute(Svg.X1,
                arcLine.getAttributeAsString(Svg.X1));
        this.arcLineTransparentOverlay.setAttribute(Svg.Y1,
                arcLine.getAttributeAsString(Svg.X1));
        this.arcLineTransparentOverlay.setAttribute(Svg.X2,
                arcLine.getAttributeAsString(Svg.X1));
        this.arcLineTransparentOverlay.setAttribute(Svg.Y2,
                arcLine.getAttributeAsString(Svg.X1));
        this.arcLineTransparentOverlay.setAttribute(Svg.STROKE, Colors.ORANGE);
        container.appendChild(this.arcLineTransparentOverlay);

        // Make fill opaque and stroke not opaque so that
        // the visible portion is smaller than the mouse
        // area for the thinnest arcs.
        this.arcLine.setAttribute(Svg.STROKE_OPACITY, 100);
        this.arcLineTransparentOverlay.setAttribute(Svg.STROKE_OPACITY, 0);

    }

    private PointDouble preventArcOverlap(PointDouble sourceNodeLocation,
            PointDouble referenceLocation, boolean otherSide) {
        Integer offset = nodePositionOffsetMap.get(this.getArc().getType());

        int flipFactor = 1;
        if (otherSide) {
            flipFactor = -1;
        }

        double a = Math.abs(sourceNodeLocation.getY()
                - referenceLocation.getY());
        double b = Math.abs(sourceNodeLocation.getX()
                - referenceLocation.getX());
        double h = Math.sqrt(a * a + b * b);
        if (h == 0) {
            h = 1;
        }

        double yDist = flipFactor
                * (sourceNodeLocation.getY() - referenceLocation.getY()) / h;
        double xDist = flipFactor
                * (sourceNodeLocation.getX() - referenceLocation.getX()) / h;
        // double normalizedSlope = (yDist / xDist);

        return sourceNodeLocation.plus(new PointDouble((-yDist) * offset,
                (xDist) * offset));
    }

    @Override
    public void setLabelRendering(boolean newValue) {
        if (newValue) {
            this.container.appendChild(this.label.asSvgElement());
        } else {
            // If in constructor, don't try to remove it...
            if (null != labelRendering) {
                this.container.removeChild(this.label.asSvgElement());
            }
        }
        labelRendering = newValue;
    }

    @Override
    public boolean getLabelRendering() {
        return labelRendering;
    }

    @Override
    public SvgElement asSvgElement() {
        return container;
    }

    @Override
    public String getId() {
        return getArc().getId();
    }

    @Override
    public double getThickness() {
        return Double.parseDouble(arcLine
                .getAttributeAsString(Svg.STROKE_WIDTH));
    }

    /**
     * Sets the arc style as either solid or dashed
     * 
     * @param styleValue
     *            Use either ArcSettings.ARC_STYLE_SOLID or
     *            ArcSettings.ARC_STYLE_DASHED
     * 
     */
    @Override
    public void setArcStyle(String arcStyle) {
        if (arcStyle.equals(ArcSettings.ARC_STYLE_SOLID)
                && arcLine.hasAttribute(Svg.STROKE_DASHARRAY)) {
            arcLine.removeAttribute(Svg.STROKE_DASHARRAY);
        } else if (arcStyle.equals(ArcSettings.ARC_STYLE_DASHED)) {
            // 10px dash, 5px gap
            arcLine.setAttribute(Svg.STROKE_DASHARRAY, "10, 5");
        } else if (arcStyle.equals(ArcSettings.ARC_STYLE_DOTTED)) {
            // 10px dash, 5px gap
            arcLine.setAttribute(Svg.STROKE_DASHARRAY, "2, 2");
        }
    }

    /**
     * Sets the arc head as either full or empty triangle
     * 
     * @param arcHead
     *            Use either ArcSettings.ARC_HEAD_TRIANGLE_EMPTY or
     *            ArcSettings.ARC_HEAD_TRIANGLE_FULL
     * 
     */
    @Override
    public void setArcHead(String arcHead) {
        if (arcHead.equals(ArcSettings.ARC_HEAD_TRIANGLE_FULL)) {
            arrow.asSvgElement().setAttribute(Svg.FILL_OPACITY, "1.0");
            arrow.asSvgElement().setAttribute(Svg.STROKE_OPACITY, "1.0");
        } else if (arcHead.equals(ArcSettings.ARC_HEAD_TRIANGLE_EMPTY)) {
            arrow.asSvgElement().setAttribute(Svg.FILL_OPACITY, "0.0");
            arrow.asSvgElement().setAttribute(Svg.STROKE_OPACITY, "1.0");
        } else {
            arrow.asSvgElement().setAttribute(Svg.FILL_OPACITY, "0.0");
            arrow.asSvgElement().setAttribute(Svg.STROKE_OPACITY, "0.0");
        }
    }

    @Override
    public void setColor(String color) {
        arcLine.setAttribute(Svg.STROKE, color);
        arcLine.setAttribute(Svg.FILL, "#FFFFFF");
        // Used to skip undirected arc heads
        arrow.asSvgElement().setAttribute(Svg.STROKE, color);
        arrow.asSvgElement().setAttribute(Svg.FILL, color);
    }

    @Override
    public void setEventListener(ChooselEventHandler handler) {
        arcLine.setEventListener(handler);
        label.setEventListener(handler);
        arrow.asSvgElement().setEventListener(handler);
        arcLineTransparentOverlay.setEventListener(handler);
    }

    @Override
    public void setThickness(Double thickness) {
        arcLine.setAttribute(Svg.STROKE_WIDTH, thickness);
        // // Update transparent line thickness, so that the effective
        // // size for mouse over purposes is larger than the visible in
        // // the case of thin lines
        arcLineTransparentOverlay.setAttribute(Svg.STROKE_WIDTH,
                MIN_THICKNESS_PAD);
    }

    @Override
    public void update() {
        PointDouble sourceCentre = this
                .preventArcOverlap(source.getNodeShapeCentre(),
                        target.getNodeShapeCentre(), false); // cache
        PointDouble targetCentre = this.preventArcOverlap(
                target.getNodeShapeCentre(), source.getNodeShapeCentre(), true); // cache
        SvgUtils.setX1Y1(arcLine, sourceCentre);
        SvgUtils.setX2Y2(arcLine, targetCentre);

        SvgUtils.setX1Y1(arcLineTransparentOverlay, sourceCentre);
        SvgUtils.setX2Y2(arcLineTransparentOverlay, targetCentre);

        // Used to skip undirected arc heads
        assert arrow != null;
        arrow.alignWithPoints(sourceCentre, targetCentre);

        assert label != null;
        if (labelRendering) {
            alignLabelPoints(sourceCentre, targetCentre);
        }
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
    public void alignLabelPoints(PointDouble sourcePoint,
            PointDouble targetPoint) {
        PointDouble arcCentre = PointUtils
                .getMidPoint(sourcePoint, targetPoint);
        boolean sourceXGreater = sourcePoint.getX() >= targetPoint.getX();
        boolean sourceYGreater = sourcePoint.getY() >= targetPoint.getY();
        PointDouble rightPoint = sourceXGreater ? sourcePoint : targetPoint;
        PointDouble leftPoint = sourceXGreater ? targetPoint : sourcePoint;
        double angle = PointUtils.getRotationAngle(leftPoint, rightPoint);

        // Need to figure out how much of the text width and height to move with
        // regard to both X and Y. As text rotates, it changes from needing a
        // pure width placement adjustment to needing a pure height
        // adjustment. When it is horizontal, it need the width adjustment fully
        // applied to the X coordinate. As it becomes vertical, it needs none of
        // that width adjustment applied to the X coordinate, and instead all of
        // it applied to the Y coordinate. So, we add it to both X and Y, but
        // multiply that by the relevant trig results. The same applies to the
        // text height, but we invert the relations on X and Y.

        double xFactor = Math.cos(Math.toRadians(angle));
        double yFactor = Math.sin(Math.toRadians(angle));

        double labelShiftX = label.getTotalWidth() / 2;
        double labelShiftY = label.getTotalHeight() / 2;

        double x = arcCentre.getX();
        x -= xFactor * labelShiftX; // Correct
        x += yFactor * labelShiftY; // Correct

        double y = arcCentre.getY();
        y -= yFactor * labelShiftX; // Correct
        y -= xFactor * labelShiftY; // Correct

        label.setTranslateAndRotation(x, y, angle);
    }
}
