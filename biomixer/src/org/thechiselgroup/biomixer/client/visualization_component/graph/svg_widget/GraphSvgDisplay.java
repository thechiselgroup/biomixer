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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesSet;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.JsDomSvgElementFactory;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.SvgWidget;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplayLoadingFailureEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplayLoadingFailureEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplayReadyEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplayReadyEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.LayoutException;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragHandleMouseMoveEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMenuItemClickedHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseClickEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseDoubleClickEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseOutEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseOverEvent;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

public class GraphSvgDisplay implements GraphDisplay {

    private SvgElementFactory svgElementFactory;

    private ArcElementFactory arcElementFactory;

    private NodeElementFactory nodeElementFactory;

    private IdentifiablesSet<NodeElement> nodes = new IdentifiablesSet<NodeElement>();

    private IdentifiablesSet<ArcElement> arcs = new IdentifiablesSet<ArcElement>();

    private SvgElement background;

    private SvgWidget asWidget;

    private int width;

    private int height;

    private int nodeMenuItemIdCounter = 0;

    private final Map<String, NodeMenuItemClickedHandler> nodeMenuItemClickHandlers = CollectionFactory
            .createStringMap();

    private NodeInteractionManager nodeInteractionManager;

    private SvgExpanderPopupFactory expanderPopupFactory;

    public GraphSvgDisplay(int width, int height) {
        this(width, height, new JsDomSvgElementFactory());
        asWidget = new SvgWidget();
        asWidget.getSvgElement().appendChild(background);
        asWidget.setPixelSize(width, height);
        initViewInteractionListener();
    }

    public GraphSvgDisplay(int width, int height,
            SvgElementFactory svgElementFactory) {
        this.width = width;
        this.height = height;
        assert svgElementFactory != null;
        this.svgElementFactory = svgElementFactory;
        this.arcElementFactory = new ArcElementFactory(svgElementFactory);
        this.nodeElementFactory = new NodeElementFactory(svgElementFactory);
        this.expanderPopupFactory = new SvgExpanderPopupFactory(
                svgElementFactory);
        initBackground(width, height);
        nodeInteractionManager = new NodeInteractionManager(this);
    }

    @Override
    public void addArc(Arc arc) {
        assert arc != null;
        assert !arcs.contains(arc.getId()) : "arc '" + arc.getId()
                + "'must not be already contained";

        String sourceNodeId = arc.getSourceNodeId();
        String targetNodeId = arc.getTargetNodeId();

        assert nodes.contains(sourceNodeId) : "source node '" + sourceNodeId
                + "' must be available";
        assert nodes.contains(targetNodeId) : "target node '" + targetNodeId
                + "' must be available";

        NodeElement sourceNode = nodes.get(sourceNodeId);
        NodeElement targetNode = nodes.get(targetNodeId);
        ArcElement arcElement = arcElementFactory.createArcElement(arc,
                sourceNode, targetNode);
        arcs.put(arcElement);
        sourceNode.addConnectedArc(arcElement);
        targetNode.addConnectedArc(arcElement);

        if (asWidget != null) {
            SvgElement rootElement = asWidget.getSvgElement();
            // put arc right after background element so that nodes will be
            // drawn on top
            if (rootElement.getChildCount() == 1) {
                rootElement.appendChild(arcElement.getSvgElement());
            } else {
                rootElement.insertBefore(arcElement.getSvgElement(),
                        rootElement.getChild(1));
            }
        }
    }

    @Override
    public <T extends EventHandler> HandlerRegistration addEventHandler(
            Type<T> type, T handler) {
        assert type != null;
        assert handler != null;

        if (type instanceof DomEvent.Type) {
            return asWidget.addDomHandler(handler, (DomEvent.Type<T>) type);
        } else {
            return asWidget.addHandler(handler, type);
        }
    }

    // TODO remove
    @Override
    public HandlerRegistration addGraphDisplayLoadingFailureHandler(
            GraphDisplayLoadingFailureEventHandler handler) {

        assert handler != null;

        return asWidget.addHandler(handler,
                GraphDisplayLoadingFailureEvent.TYPE);
    }

    // TODO remove
    @Override
    public HandlerRegistration addGraphDisplayReadyHandler(
            GraphDisplayReadyEventHandler handler) {

        assert handler != null;

        HandlerRegistration handlerRegistration = asWidget.addHandler(handler,
                GraphDisplayReadyEvent.TYPE);
        onWidgetReady();
        return handlerRegistration;
    }

    @Override
    public void addNode(Node node) {
        assert !nodes.contains(node.getId()) : node.toString()
                + " must not be contained";
        NodeElement nodeElement = nodeElementFactory.createNodeElement(node);

        nodeElement.getNodeContainer().setEventListener(
                new SvgNodeEventHandler(node.getId(), this,
                        nodeInteractionManager));
        nodeElement
                .getExpanderTab()
                .getContainer()
                .setEventListener(
                        new SvgNodeTabEventHandler(node.getId(), this));

        nodes.put(nodeElement);
        // if this isn't the first node, need to position it
        // XXX remove this once FlexVis has been completely replaced
        if (asWidget != null && nodes.size() > 1) {
            setLocation(node, new Point(width / 2, height / 2));
        }

        if (asWidget != null) {
            asWidget.getSvgElement()
                    .appendChild(nodeElement.getBaseContainer());
        }

    }

    @Override
    public void addNodeMenuItemHandler(String menuLabel,
            NodeMenuItemClickedHandler handler, String nodeType) {

        assert menuLabel != null;
        assert handler != null;
        assert nodeType != null;

        String id = "menuItemId-" + (nodeMenuItemIdCounter++);
        nodeMenuItemClickHandlers.put(id, handler);
    }

    @Override
    public void animateMoveTo(Node node, Point targetLocation) {
        // TODO animate by finding intermediate positions along path to
        // targetLocations and using setLocation on each of them in turn?
        setLocation(node, targetLocation);
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
            rootElement.appendChild(nodeElement.getBaseContainer());
        }
        return rootElement;
    }

    @Override
    public Widget asWidget() {
        return asWidget;
    }

    @Override
    public boolean containsArc(String arcId) {
        assert arcId != null;
        return arcs.contains(arcId);
    }

    @Override
    public boolean containsNode(String nodeId) {
        assert nodeId != null;
        return nodes.contains(nodeId);
    }

    @Override
    public Arc getArc(String arcId) {
        assert arcId != null;
        assert arcs.contains(arcId);
        return arcs.get(arcId).getArc();
    }

    @Override
    public Point getLocation(Node node) {
        assert node != null;
        assert nodes.contains(node.getId());
        return nodes.get(node.getId()).getLocation().toPointInt();
    }

    @Override
    public Node getNode(String nodeId) {
        assert nodeId != null;
        assert nodes.contains(nodeId);
        return nodes.get(nodeId).getNode();
    }

    private void initBackground(int width, int height) {
        background = svgElementFactory.createElement(Svg.RECT);
        background.setAttribute(Svg.WIDTH, width);
        background.setAttribute(Svg.HEIGHT, height);
        background.setAttribute(Svg.FILL, "white");
    }

    private void initViewInteractionListener() {
        asWidget.getSvgElement().setEventListener(
                new ViewWideInteractionListener(nodeInteractionManager));
    }

    public void onNodeDrag(String nodeId, int deltaX, int deltaY) {
        Point startLocation = nodes.get(nodeId).getLocation().toPointInt();
        int startX = startLocation.getX();
        int startY = startLocation.getY();
        int endX = startX + deltaX;
        int endY = startY + deltaY;

        animateMoveTo(nodes.get(nodeId).getNode(), new Point(endX, endY));
        asWidget.fireEvent(new NodeDragEvent(nodes.get(nodeId).getNode(),
                startX, startY, endX, endY));
    }

    public void onNodeDragHandleMouseMove(String nodeID, int mouseX, int mouseY) {
        asWidget.fireEvent(new NodeDragHandleMouseMoveEvent(getNode(nodeID),
                mouseX, mouseY));
    }

    public void onNodeMouseClick(String nodeId, int mouseX, int mouseY) {
        int x = asWidget.getAbsoluteLeft() + mouseX;
        int y = asWidget.getAbsoluteTop() + mouseY;

        asWidget.fireEvent(new NodeMouseClickEvent(getNode(nodeId), x, y));
    }

    public void onNodeMouseDoubleClick(String nodeId, int mouseX, int mouseY) {
        int x = asWidget.getAbsoluteLeft() + mouseX;
        int y = asWidget.getAbsoluteTop() + mouseY;

        asWidget.fireEvent(new NodeMouseDoubleClickEvent(getNode(nodeId), x, y));
    }

    public void onNodeMouseOut(String nodeID, int mouseX, int mouseY) {
        int x = asWidget.getAbsoluteLeft() + mouseX;
        int y = asWidget.getAbsoluteTop() + mouseY;

        asWidget.fireEvent(new NodeMouseOutEvent(getNode(nodeID), x, y));
    }

    public void onNodeMouseOver(String nodeId, int mouseX, int mouseY) {
        int x = asWidget.getAbsoluteLeft() + mouseX;
        int y = asWidget.getAbsoluteTop() + mouseY;

        asWidget.fireEvent(new NodeMouseOverEvent(nodes.get(nodeId).getNode(),
                x, y));
    }

    public void onNodeTabClick(final String nodeId) {
        SvgPopupExpanders popupExpanderList = expanderPopupFactory
                .createExpanderPopupList(nodes.get(nodeId)
                        .getTabTopLeftLocation(), nodeMenuItemClickHandlers
                        .keySet());

        Node node = nodes.get(nodeId).getNode();
        // TODO set listeners
        for (Entry<String, NodeMenuItemClickedHandler> entry : nodeMenuItemClickHandlers
                .entrySet()) {
            String expanderId = entry.getKey();
            final NodeMenuItemClickedHandler handler = entry.getValue();
            final BoxedTextSvgElement expanderEntry = popupExpanderList
                    .getEntryByExpanderId(expanderId);
            expanderEntry.getContainer().setEventListener(
                    new NodeMenuItemSvgEventHandler(node, expanderEntry,
                            handler));
        }

        if (asWidget != null) {
            asWidget.getSvgElement().appendChild(
                    popupExpanderList.getContainer());
        }
    }

    private void onWidgetReady() {
        asWidget.fireEvent(new GraphDisplayReadyEvent(this));
    }

    @Override
    public void removeArc(Arc arc) {
        assert arc != null;
        String id = arc.getId();
        assert arcs.contains(id);
        arcs.get(id).removeNodeConnections();
        arcs.remove(id);

        if (asWidget != null) {
            asWidget.getSvgElement().removeChild(arc.getId());
        }
    }

    @Override
    public void removeNode(Node node) {
        assert node != null;
        assert nodes.contains(node.getId());

        List<ArcElement> connectedArcElements = new ArrayList<ArcElement>();
        connectedArcElements.addAll(nodes.get(node.getId())
                .getConnectedArcElements());

        for (ArcElement arcElement : connectedArcElements) {
            removeArc(arcElement.getArc());
        }
        nodes.remove(node.getId());

        if (asWidget != null) {
            asWidget.getSvgElement().removeChild(node.getId());
        }
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
        ArcElement arcElement = arcs.get(arc.getId());

        if (styleProperty.equals(ArcSettings.ARC_COLOR)) {
            arcElement.setColor(styleValue);
        }

        else if (styleProperty.equals(ArcSettings.ARC_STYLE)) {
            arcElement.setArcStyle(styleValue);
        }

        else if (styleProperty.equals(ArcSettings.ARC_THICKNESS)) {
            arcElement.setArcThickness(styleValue);
        }
    }

    @Override
    public void setLocation(Node node, Point location) {
        assert nodes.contains(node.getId());
        nodes.get(node.getId()).setLocation(location);
    }

    @Override
    public void setNodeStyle(Node node, String styleProperty, String styleValue) {
        NodeElement nodeElement = nodes.get(node.getId());

        if (styleProperty.equals(NODE_BACKGROUND_COLOR)) {
            nodeElement.setBackgroundColor(styleValue);
        }

        else if (styleProperty.equals(NODE_FONT_COLOR)) {
            nodeElement.setFontColor(styleValue);
        }

        else if (styleProperty.equals(NODE_FONT_WEIGHT)) {
            nodeElement.setFontWeight(styleValue);
        }

        else if (styleProperty.equals(NODE_BORDER_COLOR)) {
            nodeElement.setBorderColor(styleValue);
        }

    }

}
