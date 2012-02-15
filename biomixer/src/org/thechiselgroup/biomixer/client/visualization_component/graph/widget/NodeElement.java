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

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

/**
 * Contains references to components of a node
 * 
 * @author drusk
 * 
 */
public class NodeElement implements Identifiable {

    private Node node;

    private SvgElement container;

    private SvgElement rectangle;

    private SvgElement text;

    private List<ArcElement> arcsConnectedToThisNode = new ArrayList<ArcElement>();

    public NodeElement(Node node, SvgElement container, SvgElement rectangle,
            SvgElement text) {
        this.node = node;
        this.container = container;
        this.rectangle = rectangle;
        this.text = text;
    }

    public void addConnectedArc(ArcElement arc) {
        arcsConnectedToThisNode.add(arc);
    }

    public List<ArcElement> getConnectedArcElements() {
        return arcsConnectedToThisNode;
    }

    public SvgElement getContainer() {
        return container;
    }

    @Override
    public String getId() {
        return getNode().getId();
    }

    public Point getLocation() {
        return new Point((int) Double.parseDouble(container
                .getAttributeAsString(Svg.X)),
                (int) Double.parseDouble(container.getAttributeAsString(Svg.Y)));

    }

    public Point getMidPoint() {
        double x = Double.parseDouble(container.getAttributeAsString(Svg.X))
                + Double.parseDouble(rectangle.getAttributeAsString(Svg.WIDTH))
                / 2;

        double y = Double.parseDouble(container.getAttributeAsString(Svg.Y))
                + Double.parseDouble(rectangle.getAttributeAsString(Svg.HEIGHT))
                / 2;

        return new Point(x, y);
    }

    public Node getNode() {
        return node;
    }

    public SvgElement getRectangle() {
        return rectangle;
    }

    public SvgElement getText() {
        return text;
    }

    public void removeConnectedArc(ArcElement arc) {
        arcsConnectedToThisNode.remove(arc);
    }

    public void setLocation(Point location) {
        container.setAttribute(Svg.X, location.getX());
        container.setAttribute(Svg.Y, location.getY());
        updateConnectedArcs(location);
    }

    private void updateConnectedArcs(Point location) {
        for (ArcElement arcElement : arcsConnectedToThisNode) {
            if (arcElement.getArc().getSourceNodeId().equals(node.getId())) {
                arcElement.updateSourcePoint();
            } else {
                arcElement.updateTargetPoint();
            }
        }
    }
}
