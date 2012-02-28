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
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgUtils;

public class ArcElement implements Identifiable {

    private Arc arc;

    /*
     * The SVG representation of the arc
     */
    private SvgElement arcSvgElement;

    private NodeSvgComponent source;

    private NodeSvgComponent target;

    public ArcElement(Arc arc, SvgElement arcSvgElement, NodeSvgComponent source,
            NodeSvgComponent target) {
        this.arc = arc;
        this.arcSvgElement = arcSvgElement;
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

    public SvgElement getSvgElement() {
        return arcSvgElement;
    }

    public NodeSvgComponent getTarget() {
        return target;
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
                && arcSvgElement.hasAttribute(Svg.STROKE_DASHARRAY)) {
            arcSvgElement.removeAttribute(Svg.STROKE_DASHARRAY);
        } else if (arcStyle.equals(ArcSettings.ARC_STYLE_DASHED)) {
            // 10px dash, 5px gap
            arcSvgElement.setAttribute(Svg.STROKE_DASHARRAY, "10, 5");
        }
    }

    public void setArcThickness(String thickness) {
        arcSvgElement.setAttribute(Svg.STROKE_WIDTH, thickness);
    }

    public void setColor(String color) {
        arcSvgElement.setAttribute(Svg.STROKE, color);
    }

    public void updateSourcePoint() {
        SvgUtils.setX1Y1(arcSvgElement, source.getMidPoint());
    }

    public void updateTargetPoint() {
        SvgUtils.setX2Y2(arcSvgElement, target.getMidPoint());
    }

}
