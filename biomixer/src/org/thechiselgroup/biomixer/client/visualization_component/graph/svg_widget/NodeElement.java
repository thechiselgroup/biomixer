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

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
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

    private SvgElement baseContainer;

    private List<ArcElement> arcsConnectedToThisNode = new ArrayList<ArcElement>();

    private final ExpanderTabSvgElement expanderTab;

    private final BoxedTextSvgElement boxedText;

    public NodeElement(Node node, SvgElement baseContainer,
            BoxedTextSvgElement boxedText, ExpanderTabSvgElement expanderTab) {
        this.node = node;
        this.baseContainer = baseContainer;
        this.boxedText = boxedText;
        this.expanderTab = expanderTab;
    }

    public void addConnectedArc(ArcElement arc) {
        arcsConnectedToThisNode.add(arc);
    }

    public SvgElement getBaseContainer() {
        return baseContainer;
    }

    public List<ArcElement> getConnectedArcElements() {
        return arcsConnectedToThisNode;
    }

    public ExpanderTabSvgElement getExpanderTab() {
        return expanderTab;
    }

    @Override
    public String getId() {
        return getNode().getId();
    }

    /**
     * 
     * @return the coordinates of the top left corner of the node, using the
     *         base svg element's coordinate system
     */
    public PointDouble getLocation() {
        return new PointDouble(Double.parseDouble(baseContainer
                .getAttributeAsString(Svg.X)), Double.parseDouble(baseContainer
                .getAttributeAsString(Svg.Y)));
    }

    public PointDouble getMidPoint() {
        PointDouble topLeft = getLocation();
        return new PointDouble(topLeft.getX() + boxedText.getTotalWidth() / 2,
                topLeft.getY() + boxedText.getTotalHeight() / 2);
    }

    public Node getNode() {
        return node;
    }

    public SvgElement getNodeContainer() {
        return boxedText.getContainer();
    }

    public PointDouble getTabTopLeftLocation() {
        return getLocation().plus(expanderTab.getLocation());
    }

    public void removeConnectedArc(ArcElement arc) {
        arcsConnectedToThisNode.remove(arc);
    }

    public void setBackgroundColor(String color) {
        boxedText.setBackgroundColor(color);
        expanderTab.setBackgroundColor(color);
    }

    public void setBorderColor(String color) {
        boxedText.setBorderColor(color);
        expanderTab.setBorderColor(color);
    }

    public void setFontColor(String color) {
        boxedText.setFontColor(color);
    }

    public void setFontWeight(String styleValue) {
        if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_NORMAL)) {
            boxedText.setFontWeight("normal");
        } else if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_BOLD)) {
            boxedText.setFontWeight("bold");
        }
    }

    public void setLocation(Point location) {
        baseContainer.setAttribute(Svg.X, location.getX());
        baseContainer.setAttribute(Svg.Y, location.getY());
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
