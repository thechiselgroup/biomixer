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
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class ArcElementFactory {

    private SvgElementFactory svgElementFactory;

    public ArcElementFactory(SvgElementFactory svgElementFactory) {
        this.svgElementFactory = svgElementFactory;
    }

    public ArcElement createArcElement(Arc arc, NodeElement sourceNode,
            NodeElement targetNode) {
        Point sourceNodeMidPoint = sourceNode.getMidPoint();
        Point targetNodeMidPoint = targetNode.getMidPoint();

        SvgElement line = svgElementFactory.createElement(Svg.LINE);
        // TODO find out how to set properties
        line.setAttribute("style", "stroke:black;");
        line.setAttribute(Svg.X1, sourceNodeMidPoint.getX());
        line.setAttribute(Svg.X2, targetNodeMidPoint.getX());
        line.setAttribute(Svg.Y1, sourceNodeMidPoint.getY());
        line.setAttribute(Svg.Y2, targetNodeMidPoint.getY());

        return new ArcElement(arc, line, sourceNode, targetNode);
    }

}
