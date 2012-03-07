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

import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgUtils;

public class ArcSvgComponent extends CompositeSvgComponent implements
        LayoutArc, Identifiable {

    private Arc arc;

    private NodeSvgComponent source;

    private NodeSvgComponent target;

    private final SvgElement arcLine;

    private final SvgArrowHead arrow;

    private LayoutArcType arcType;

    public ArcSvgComponent(Arc arc, LayoutArcType arcType,
            SvgElement container, SvgElement arcLine, SvgArrowHead arrow,
            NodeSvgComponent source, NodeSvgComponent target) {
        super(container);
        this.arc = arc;
        this.arcType = arcType;
        this.arcLine = arcLine;
        this.arrow = arrow;
        this.source = source;
        this.target = target;
    }

    public Arc getArc() {
        return arc;
    }

    @Override
    public String getId() {
        return getArc().getId();
    }

    public NodeSvgComponent getSource() {
        return source;
    }

    @Override
    public LayoutNode getSourceNode() {
        return source;
    }

    public NodeSvgComponent getTarget() {
        return target;
    }

    @Override
    public LayoutNode getTargetNode() {
        return target;
    }

    @Override
    public double getThickness() {
        return Double.parseDouble(arcLine
                .getAttributeAsString(Svg.STROKE_WIDTH));
    }

    @Override
    public LayoutArcType getType() {
        return arcType;
    }

    @Override
    public boolean isDirected() {
        return arc.isDirected();
    }

    public void removeNodeConnections() {
        source.removeConnectedArc(this);
        target.removeConnectedArc(this);
    }

    /**
     * Sets the arc style as either solid or dashed
     * 
     * @param styleValue
     *            Use either ArcSettings.ARC_STYLE_SOLID or
     *            ArcSettings.ARC_STYLE_DASHED
     * 
     */
    public void setArcStyle(String arcStyle) {
        if (arcStyle.equals(ArcSettings.ARC_STYLE_SOLID)
                && arcLine.hasAttribute(Svg.STROKE_DASHARRAY)) {
            arcLine.removeAttribute(Svg.STROKE_DASHARRAY);
        } else if (arcStyle.equals(ArcSettings.ARC_STYLE_DASHED)) {
            // 10px dash, 5px gap
            arcLine.setAttribute(Svg.STROKE_DASHARRAY, "10, 5");
        }
    }

    public void setArcThickness(String thickness) {
        arcLine.setAttribute(Svg.STROKE_WIDTH, thickness);
    }

    public void setColor(String color) {
        arcLine.setAttribute(Svg.STROKE, color);
        if (isDirected()) {
            arrow.asSvgElement().setAttribute(Svg.STROKE, color);
            arrow.asSvgElement().setAttribute(Svg.FILL, color);
        }
    }

    @Override
    public void setEventListener(ChooselEventHandler handler) {
        arcLine.setEventListener(handler);
    }

    private void updateArrow() {
        if (isDirected()) {
            assert arrow != null;
            arrow.alignWithPoints(source.getMidPoint(), target.getMidPoint());
        }
    }

    public void updateSourcePoint() {
        SvgUtils.setX1Y1(arcLine, source.getMidPoint());
        updateArrow();
    }

    public void updateTargetPoint() {
        SvgUtils.setX2Y2(arcLine, target.getMidPoint());
        updateArrow();
    }

}
