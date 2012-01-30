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
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

public class ArcElement {

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

    public String getArcId() {
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
