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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesSet;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.core.util.text.CanvasTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.SvgBBoxTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
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

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;

public class GraphSvgDisplay implements GraphDisplay {

    private SvgElementFactory svgElementFactory;

    private ArcElementFactory arcElementFactory;

    private NodeElementFactory nodeElementFactory;

    private IdentifiablesSet<NodeSvgComponent> nodes = new IdentifiablesSet<NodeSvgComponent>();

    private IdentifiablesSet<ArcElement> arcs = new IdentifiablesSet<ArcElement>();

    private SvgWidget asWidget = null;

    protected CompositeSvgComponent rootSvgComponent = null;

    private CompositeSvgComponent arcGroup;

    private CompositeSvgComponent nodeGroup;

    protected CompositeSvgComponent popupGroup;

    private EventBus eventBus = new SimpleEventBus();

    private int width;

    private int height;

    private NodeInteractionManager nodeInteractionManager;

    private SvgExpanderPopupFactory expanderPopupFactory;

    protected TextBoundsEstimator textBoundsEstimator;

    private ChooselEventHandler viewWideInteractionListener;

    // maps node types to their available menu item click handlers and those
    // handlers' associated labels
    private Map<String, Map<String, NodeMenuItemClickedHandler>> nodeMenuItemClickHandlersByType = CollectionFactory
            .createStringMap();

    public GraphSvgDisplay(int width, int height) {
        this(width, height, new JsDomSvgElementFactory());
    }

    public GraphSvgDisplay(int width, int height,
            SvgElementFactory svgElementFactory) {
        this.width = width;
        this.height = height;
        assert svgElementFactory != null;
        this.svgElementFactory = svgElementFactory;

        initRootSvgComponent();
        initBackground(width, height);
        initCompositeGroupingComponents();

        this.arcElementFactory = new ArcElementFactory(svgElementFactory);
        initTextBoundsEstimator();
        this.nodeElementFactory = new NodeElementFactory(svgElementFactory,
                textBoundsEstimator);
        this.expanderPopupFactory = new SvgExpanderPopupFactory(
                svgElementFactory, textBoundsEstimator);

        nodeInteractionManager = new NodeInteractionManager(this);
        initViewWideInteractionHandler();
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

        NodeSvgComponent sourceNode = nodes.get(sourceNodeId);
        NodeSvgComponent targetNode = nodes.get(targetNodeId);
        ArcElement arcElement = arcElementFactory.createArcElement(arc,
                sourceNode, targetNode);
        arcs.put(arcElement);
        sourceNode.addConnectedArc(arcElement);
        targetNode.addConnectedArc(arcElement);

        arcGroup.appendChild(arcElement.getSvgElement());
    }

    @Override
    public <T extends EventHandler> HandlerRegistration addEventHandler(
            Type<T> type, T handler) {
        assert type != null;
        assert handler != null;

        return eventBus.addHandler(type, handler);
        // if (type instanceof DomEvent.Type) {
        // return asWidget.addDomHandler(handler, (DomEvent.Type<T>) type);
        // } else {
        // return asWidget.addHandler(handler, type);
        // }
    }

    // TODO remove
    @Override
    public HandlerRegistration addGraphDisplayLoadingFailureHandler(
            GraphDisplayLoadingFailureEventHandler handler) {

        assert handler != null;

        return eventBus.addHandler(GraphDisplayLoadingFailureEvent.TYPE,
                handler);
    }

    // TODO remove
    @Override
    public HandlerRegistration addGraphDisplayReadyHandler(
            GraphDisplayReadyEventHandler handler) {

        assert handler != null;

        HandlerRegistration handlerRegistration = eventBus.addHandler(
                GraphDisplayReadyEvent.TYPE, handler);
        onWidgetReady();
        return handlerRegistration;
    }

    @Override
    public void addNode(Node node) {
        assert !nodes.contains(node.getId()) : node.toString()
                + " must not be contained";
        final NodeSvgComponent nodeElement = nodeElementFactory
                .createNodeElement(node);

        nodeElement.setNodeEventListener(new SvgNodeEventHandler(nodeElement,
                this, nodeInteractionManager));
        nodeElement.setExpanderTabEventListener(new ChooselEventHandler() {

            @Override
            public void onEvent(ChooselEvent event) {
                if (event.getEventType().equals(ChooselEvent.Type.CLICK)) {
                    onNodeTabClick(nodeElement);
                }
            }

        });

        nodes.put(nodeElement);

        // if this isn't the first node, need to position it
        // XXX remove this once FlexVis has been completely replaced
        if (isWidgetInitialized() && nodes.size() > 1) {
            setLocation(node, new Point(width / 2, height / 2));
        }

        nodeGroup.appendChild(nodeElement);
    }

    @Override
    public void addNodeMenuItemHandler(String menuLabel,
            NodeMenuItemClickedHandler handler, String nodeType) {

        assert menuLabel != null;
        assert handler != null;
        assert nodeType != null;

        if (!nodeMenuItemClickHandlersByType.containsKey(nodeType)) {
            nodeMenuItemClickHandlersByType.put(nodeType,
                    new HashMap<String, NodeMenuItemClickedHandler>());
        }
        nodeMenuItemClickHandlersByType.get(nodeType).put(menuLabel, handler);
    }

    @Override
    public void animateMoveTo(Node node, Point targetLocation) {
        // TODO animate by finding intermediate positions along path to
        // targetLocations and using setLocation on each of them in turn?
        setLocation(node, targetLocation);
    }

    public SvgElement asSvg() {
        return rootSvgComponent.getSvgElement();
    }

    @Override
    public Widget asWidget() {
        if (!isWidgetInitialized()) {
            asWidget = new SvgWidget();
            rootSvgComponent = new CompositeSvgComponent(
                    asWidget.getSvgElement(), rootSvgComponent,
                    viewWideInteractionListener);
            asWidget.setPixelSize(width, height);
        }
        return asWidget;
    }

    public void clearPopups() {
        popupGroup.removeAllChildren();
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

    private CompositeSvgComponent createCompositeGroupingComponent(String id) {
        SvgElement groupingElement = svgElementFactory.createElement(Svg.G);
        groupingElement.setAttribute(Svg.ID, id);
        return new CompositeSvgComponent(groupingElement);
    }

    @Override
    public Arc getArc(String arcId) {
        assert arcId != null;
        assert arcs.contains(arcId);
        return arcs.get(arcId).getArc();
    }

    protected int getGraphAbsoluteLeft() {
        return asWidget.getAbsoluteLeft();
    }

    protected int getGraphAbsoluteTop() {
        return asWidget.getAbsoluteTop();
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

    public NodeSvgComponent getNodeElement(Node node) {
        return nodes.get(node.getId());
    }

    private void initBackground(int width, int height) {
        SvgElement background = svgElementFactory.createElement(Svg.RECT);
        background.setAttribute(Svg.WIDTH, width);
        background.setAttribute(Svg.HEIGHT, height);
        background.setAttribute(Svg.FILL, Colors.WHITE);
        background.setEventListener(new ChooselEventHandler() {

            @Override
            public void onEvent(ChooselEvent event) {
                if (event.getEventType().equals(ChooselEvent.Type.CLICK)) {
                    onBackgroundClick(event.getClientX(), event.getClientY());
                }

            }

        });
        rootSvgComponent.appendChild(background);
    }

    private void initCompositeGroupingComponents() {
        arcGroup = createCompositeGroupingComponent("arcGroup");
        nodeGroup = createCompositeGroupingComponent("nodeGroup");
        popupGroup = createCompositeGroupingComponent("popupGroup");
        // order is important here - want arcs behind nodes and popups
        rootSvgComponent.appendChild(arcGroup);
        rootSvgComponent.appendChild(nodeGroup);
        rootSvgComponent.appendChild(popupGroup);
    }

    private void initRootSvgComponent() {
        SvgElement root = svgElementFactory.createElement(Svg.SVG);
        root.setAttribute("xmlns", Svg.NAMESPACE);
        root.setAttribute("version", "1.1");
        rootSvgComponent = new CompositeSvgComponent(root);
    }

    protected void initTextBoundsEstimator() {
        this.textBoundsEstimator = new CanvasTextBoundsEstimator(
                new SvgBBoxTextBoundsEstimator(svgElementFactory));
    }

    private void initViewWideInteractionHandler() {
        viewWideInteractionListener = new ChooselEventHandler() {

            @Override
            public void onEvent(ChooselEvent event) {
                if (event.getEventType().equals(ChooselEvent.Type.MOUSE_MOVE)) {
                    onViewMouseMove(event.getClientX(), event.getClientY());
                }

            }
        };
        rootSvgComponent.setEventListener(viewWideInteractionListener);
    }

    private boolean isWidgetInitialized() {
        return asWidget != null;
    }

    public void onBackgroundClick(int mouseX, int mouseY) {
        clearPopups();
    }

    public void onNodeDrag(String nodeId, int deltaX, int deltaY) {
        Point startLocation = nodes.get(nodeId).getLocation().toPointInt();
        int startX = startLocation.getX();
        int startY = startLocation.getY();
        int endX = startX + deltaX;
        int endY = startY + deltaY;

        animateMoveTo(nodes.get(nodeId).getNode(), new Point(endX, endY));
        eventBus.fireEvent(new NodeDragEvent(nodes.get(nodeId).getNode(),
                startX, startY, endX, endY));
        clearPopups();
    }

    public void onNodeDragHandleMouseMove(String nodeID, int mouseX, int mouseY) {
        eventBus.fireEvent(new NodeDragHandleMouseMoveEvent(getNode(nodeID),
                mouseX, mouseY));
    }

    public void onNodeMouseClick(String nodeId, int mouseX, int mouseY) {
        int x = mouseX - getGraphAbsoluteLeft();
        int y = mouseY - getGraphAbsoluteTop();

        eventBus.fireEvent(new NodeMouseClickEvent(getNode(nodeId), x, y));
        clearPopups();
    }

    public void onNodeMouseDoubleClick(String nodeId, int mouseX, int mouseY) {
        int x = getGraphAbsoluteLeft() + mouseX;
        int y = getGraphAbsoluteTop() + mouseY;

        eventBus.fireEvent(new NodeMouseDoubleClickEvent(getNode(nodeId), x, y));
    }

    public void onNodeMouseOut(String nodeID, int mouseX, int mouseY) {
        int x = mouseX - getGraphAbsoluteLeft();
        int y = mouseY - getGraphAbsoluteTop();

        eventBus.fireEvent(new NodeMouseOutEvent(getNode(nodeID), x, y));
    }

    public void onNodeMouseOver(String nodeId, int mouseX, int mouseY) {
        int x = mouseX - getGraphAbsoluteLeft();
        int y = mouseY - getGraphAbsoluteTop();

        eventBus.fireEvent(new NodeMouseOverEvent(nodes.get(nodeId).getNode(),
                x, y));
        nodeGroup.appendChild(nodes.get(nodeId));
    }

    public void onNodeTabClick(final NodeSvgComponent nodeElement) {
        Map<String, NodeMenuItemClickedHandler> nodeMenuItemClickHandlers = nodeMenuItemClickHandlersByType
                .get(nodeElement.getNodeType());
        PopupExpanderSvgComponent popupExpanderList = expanderPopupFactory
                .createExpanderPopupList(
                        nodeElement.getExpanderTabAbsoluteLocation(),
                        nodeMenuItemClickHandlers.keySet());

        for (Entry<String, NodeMenuItemClickedHandler> entry : nodeMenuItemClickHandlers
                .entrySet()) {
            String expanderId = entry.getKey();
            final NodeMenuItemClickedHandler handler = entry.getValue();
            final BoxedTextSvgComponent expanderEntry = popupExpanderList
                    .getEntryByExpanderLabel(expanderId);
            expanderEntry.setEventListener(new ChooselEventHandler() {

                @Override
                public void onEvent(ChooselEvent event) {
                    switch (event.getEventType()) {
                    case MOUSE_OVER:
                        // give rect highlight color
                        expanderEntry.setBackgroundColor(Colors.BLUE_1);
                        break;

                    case MOUSE_OUT:
                        // give rect default color
                        expanderEntry.setBackgroundColor(Colors.WHITE);
                        break;

                    case CLICK:
                        handler.onNodeMenuItemClicked(nodeElement.getNode());
                        clearPopups();
                        break;

                    default:
                        break;
                    }
                }
            });
        }

        clearPopups();
        popupGroup.appendChild(popupExpanderList);
    }

    public void onViewMouseMove(int mouseX, int mouseY) {
        nodeInteractionManager.onMouseMove(mouseX, mouseY);
    }

    private void onWidgetReady() {
        eventBus.fireEvent(new GraphDisplayReadyEvent(this));
    }

    @Override
    public void removeArc(Arc arc) {
        assert arc != null;
        String id = arc.getId();
        assert arcs.contains(id);
        arcs.get(id).removeNodeConnections();
        arcs.remove(id);
        arcGroup.removeChild(arc.getId());
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
        nodeGroup.removeChild(node.getId());
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
        NodeSvgComponent nodeElement = nodes.get(node.getId());

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
