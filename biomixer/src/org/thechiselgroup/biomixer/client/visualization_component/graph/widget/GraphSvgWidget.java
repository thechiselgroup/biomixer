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
import java.util.Collection;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.JsDomSvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

public class GraphSvgWidget implements GraphDisplay {

    private SvgElementFactory svgElementFactory;

    private ArcElementFactory arcElementFactory;

    private NodeElementFactory nodeElementFactory;

    private NodeElementList nodes = new NodeElementList();

    private ArcElementList arcs = new ArcElementList();

    public GraphSvgWidget(int width, int height) {
        this(new JsDomSvgElementFactory());
    }

    public GraphSvgWidget(SvgElementFactory svgElementFactory) {
        this.svgElementFactory = svgElementFactory;
        this.arcElementFactory = new ArcElementFactory(svgElementFactory);
        this.nodeElementFactory = new NodeElementFactory(svgElementFactory);
    }

    @Override
    public void addArc(Arc arc) {
        assert arc != null;
        assert !arcs.containsArcWithId(arc.getId()) : "arc '" + arc.getId()
                + "'must not be already contained";

        String sourceNodeId = arc.getSourceNodeId();
        String targetNodeId = arc.getTargetNodeId();

        assert nodes.containsNodeWithId(sourceNodeId) : "source node '"
                + sourceNodeId + "' must be available";
        assert nodes.containsNodeWithId(targetNodeId) : "target node '"
                + targetNodeId + "' must be available";

        NodeElement sourceNode = nodes.getNodeElement(sourceNodeId);
        NodeElement targetNode = nodes.getNodeElement(targetNodeId);
        ArcElement arcElement = arcElementFactory.createArcElement(arc,
                sourceNode, targetNode);
        arcs.add(arcElement);
        sourceNode.addConnectedArc(arcElement);
        targetNode.addConnectedArc(arcElement);
    }

    @Override
    public <T extends EventHandler> HandlerRegistration addEventHandler(
            Type<T> type, T handler) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HandlerRegistration addGraphDisplayLoadingFailureHandler(
            GraphDisplayLoadingFailureEventHandler handler) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HandlerRegistration addGraphDisplayReadyHandler(
            GraphDisplayReadyEventHandler handler) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addNode(Node node) {
        assert !nodes.containsNodeWithId(node.getId()) : "node must not be contained";
        nodes.add(nodeElementFactory.createNodeElement(node));
    }

    @Override
    public void addNodeMenuItemHandler(String menuLabel,
            NodeMenuItemClickedHandler handler, String nodeClass) {
        // TODO Auto-generated method stub

    }

    @Override
    public void animateMoveTo(Node node, Point targetLocation) {
        // TODO
    }

    public SvgElement asSvg() {
        SvgElement rootElement = svgElementFactory.createElement(Svg.SVG);
        rootElement.setAttribute("xmlns", Svg.NAMESPACE);
        rootElement.setAttribute("version", "1.1");

        for (ArcElement arcElement : arcs) {
            rootElement.appendChild(arcElement.getSvgElement());
        }

        // Nodes should be added after arcs so that they are drawn on top
        for (NodeElement nodeElement : nodes) {
            rootElement.appendChild(nodeElement.getContainer());
        }
        return rootElement;
    }

    @Override
    public Widget asWidget() {
        // extend Widget and return this?
        return null;
    }

    @Override
    public boolean containsArc(String arcId) {
        assert arcId != null;
        return arcs.containsArcWithId(arcId);
    }

    @Override
    public boolean containsNode(String nodeId) {
        assert nodeId != null;
        return nodes.containsNodeWithId(nodeId);
    }

    @Override
    public Arc getArc(String arcId) {
        assert arcId != null;
        assert arcs.containsArcWithId(arcId);
        return arcs.getArcElement(arcId).getArc();
    }

    @Override
    public Point getLocation(Node node) {
        assert node != null;
        assert nodes.containsNodeWithId(node.getId());
        return nodes.getNodeElement(node.getId()).getLocation();
    }

    @Override
    public Node getNode(String nodeId) {
        assert nodeId != null;
        assert nodes.containsNodeWithId(nodeId);
        return nodes.getNodeElement(nodeId).getNode();
    }

    @Override
    public void removeArc(Arc arc) {
        assert arc != null;
        assert arcs.containsArcWithId(arc.getId());
        arcs.remove(arc.getId());
    }

    @Override
    public void removeNode(Node node) {
        assert node != null;
        assert nodes.containsNodeWithId(node.getId());

        List<ArcElement> connectedArcElements = new ArrayList<ArcElement>();
        connectedArcElements.addAll(nodes.getNodeElement(node.getId())
                .getConnectedArcElements());

        for (ArcElement arcElement : connectedArcElements) {
            removeArc(arcElement.getArc());
        }
        nodes.remove(node.getId());
    }

    @Override
    public void runLayout() throws LayoutException {
        // TODO
    }

    @Override
    public void runLayout(String layout) throws LayoutException {
        // TODO
    }

    @Override
    public void runLayoutOnNodes(Collection<Node> nodes) throws LayoutException {
        // TODO
    }

    @Override
    public void setArcStyle(Arc arc, String styleProperty, String styleValue) {
        // TODO
    }

    @Override
    public void setLocation(Node node, Point location) {
        assert nodes.containsNodeWithId(node.getId());
        nodes.getNodeElement(node.getId()).setLocation(location);
    }

    @Override
    public void setNodeStyle(Node node, String styleProperty, String styleValue) {
        // TODO
    }

}
