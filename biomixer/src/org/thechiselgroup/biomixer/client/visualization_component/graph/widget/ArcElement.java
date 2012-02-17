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
package org.thechiselgroup.biomixer.client.visualization_component.graph.widget;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

public class ArcElement implements Identifiable {

    private Arc arc;

    /*
     * The SVG representation of the arc
     */
    private SvgElement arcSvgElement;

    private NodeElement source;

    private NodeElement target;

    public ArcElement(Arc arc, SvgElement arcSvgElement, NodeElement source,
            NodeElement target) {
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

    public NodeElement getSource() {
        return source;
    }

    public SvgElement getSvgElement() {
        return arcSvgElement;
    }

    public NodeElement getTarget() {
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
        if (arcStyle.equals(ArcSettings.ARC_STYLE_SOLID)) {
            // TODO: currently setting gap in dash array to 0. Should really
            // have some way of removing the attribute.
            arcSvgElement.setAttribute(Svg.STROKE_DASHARRAY, "10, 0");
        } else if (arcStyle.equals(ArcSettings.ARC_STYLE_SOLID)) {
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
        Point midPoint = source.getMidPoint();
        arcSvgElement.setAttribute(Svg.X1, midPoint.getX());
        arcSvgElement.setAttribute(Svg.Y1, midPoint.getY());
    }

    public void updateTargetPoint() {
        Point midPoint = target.getMidPoint();
        arcSvgElement.setAttribute(Svg.X2, midPoint.getX());
        arcSvgElement.setAttribute(Svg.Y2, midPoint.getY());
    }

}
