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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.ArcSvgComponent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.CompositeSvgComponent;
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
public class NodeSvgComponent extends CompositeSvgComponent implements
        Identifiable, RenderedNode {

    private Node node;

    private List<ArcSvgComponent> arcsConnectedToThisNode = new ArrayList<ArcSvgComponent>();

    private final ExpanderTabSvgComponent expanderTab;

    private final BoxedTextSvgComponent boxedText;

    private LayoutNodeType nodeType;

    public NodeSvgComponent(Node node, LayoutNodeType nodeType,
            SvgElement baseContainer, BoxedTextSvgComponent boxedText,
            ExpanderTabSvgComponent expanderTab) {
        super(baseContainer);
        appendChild(boxedText);
        appendChild(expanderTab);
        this.node = node;
        this.nodeType = nodeType;
        this.boxedText = boxedText;
        this.expanderTab = expanderTab;
    }

    public void addConnectedArc(ArcSvgComponent arc) {
        arcsConnectedToThisNode.add(arc);
    }

    public List<ArcSvgComponent> getConnectedArcComponents() {
        return arcsConnectedToThisNode;
    }

    public List<LayoutArc> getConnectedArcs() {
        List<LayoutArc> connectedArcs = new ArrayList<LayoutArc>();
        for (LayoutArc layoutArc : arcsConnectedToThisNode) {
            connectedArcs.add(layoutArc);
        }
        return connectedArcs;
    }

    public ExpanderTabSvgComponent getExpanderTab() {
        return expanderTab;
    }

    public PointDouble getExpanderTabAbsoluteLocation() {
        return getLocation().plus(expanderTab.getLocation());
    }

    @Override
    public String getId() {
        return getNode().getId();
    }

    @Override
    public double getLeftX() {
        return Double.parseDouble(compositeElement.getAttributeAsString(Svg.X));
    }

    /**
     * 
     * @return the coordinates of the top left corner of the node, using the
     *         base svg element's coordinate system
     */
    public PointDouble getLocation() {
        return new PointDouble(
                Double.parseDouble(compositeElement.getAttributeAsString(Svg.X)),
                Double.parseDouble(compositeElement.getAttributeAsString(Svg.Y)));
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
        return boxedText.getSvgElement();
    }

    @Override
    public SizeDouble getSize() {
        return new DefaultSizeDouble(boxedText.getTotalWidth(),
                boxedText.getTotalHeight());
    }

    @Override
    public double getTopY() {
        return Double.parseDouble(compositeElement.getAttributeAsString(Svg.Y));
    }

    public LayoutNodeType getType() {
        return nodeType;
    }

    public void removeConnectedArc(ArcSvgComponent arc) {
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

    public void setExpanderTabEventListener(ChooselEventHandler handler) {
        expanderTab.setEventListener(handler);
    }

    public void setFontColor(String color) {
        boxedText.setFontColor(color);
    }

    public void setFontWeight(String styleValue) {
        if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_NORMAL)) {
            boxedText.setFontWeight(Svg.NORMAL);
        } else if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_BOLD)) {
            boxedText.setFontWeight(Svg.BOLD);
        }
    }

    @Override
    public void setLeftX(double x) {
        compositeElement.setAttribute(Svg.X, x);
        updateConnectedArcs();
    }

    public void setNodeEventListener(ChooselEventHandler handler) {
        boxedText.setEventListener(handler);
    }

    @Override
    public void setTopY(double y) {
        compositeElement.setAttribute(Svg.Y, y);
        updateConnectedArcs();
    }

    private void updateConnectedArcs() {
        for (ArcSvgComponent arcComponent : arcsConnectedToThisNode) {
            if (arcComponent.getArc().getSourceNodeId().equals(node.getId())) {
                arcComponent.updateSourcePoint();
            } else {
                arcComponent.updateTargetPoint();
            }
        }
    }

}
