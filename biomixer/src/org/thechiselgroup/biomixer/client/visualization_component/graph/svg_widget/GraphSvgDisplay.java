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
import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesList;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.core.util.text.CanvasTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.SvgBBoxTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewResizeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewResizeEventListener;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.JsDomSvgElementFactory;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.SvgWidget;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultBoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultLayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultLayoutNodeType;
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

public class GraphSvgDisplay extends AbstractLayoutGraph implements
        GraphDisplay, ViewResizeEventListener {

    private SvgElementFactory svgElementFactory;

    private ArcComponentFactory arcComponentFactory;

    private NodeComponentFactory nodeComponentFactory;

    private IdentifiablesList<NodeSvgComponent> nodes = new IdentifiablesList<NodeSvgComponent>();

    private IdentifiablesList<ArcSvgComponent> arcs = new IdentifiablesList<ArcSvgComponent>();

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

    private SvgElement background;

    private IdentifiablesList<DefaultLayoutNodeType> nodeTypes = new IdentifiablesList<DefaultLayoutNodeType>();

    private IdentifiablesList<DefaultLayoutArcType> arcTypes = new IdentifiablesList<DefaultLayoutArcType>();

    // maps node types to their available menu item click handlers and those
    // handlers' associated labels
    private Map<LayoutNodeType, Map<String, NodeMenuItemClickedHandler>> nodeMenuItemClickHandlersByType = new HashMap<LayoutNodeType, Map<String, NodeMenuItemClickedHandler>>();

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

        this.arcComponentFactory = new ArcComponentFactory(svgElementFactory);
        initTextBoundsEstimator();
        this.nodeComponentFactory = new NodeComponentFactory(svgElementFactory,
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

        DefaultLayoutArcType layoutArcType = getArcType(arc.getType());

        String sourceNodeId = arc.getSourceNodeId();
        String targetNodeId = arc.getTargetNodeId();

        assert nodes.contains(sourceNodeId) : "source node '" + sourceNodeId
                + "' must be available";
        assert nodes.contains(targetNodeId) : "target node '" + targetNodeId
                + "' must be available";

        NodeSvgComponent sourceNode = nodes.get(sourceNodeId);
        NodeSvgComponent targetNode = nodes.get(targetNodeId);
        final ArcSvgComponent arcComponent = arcComponentFactory
                .createArcComponent(arc, layoutArcType, sourceNode, targetNode);
        layoutArcType.add(arcComponent);

        arcComponent.setEventListener(new ChooselEventHandler() {

            @Override
            public void onEvent(ChooselEvent event) {
                if (event.getEventType().equals(ChooselEvent.Type.MOUSE_OVER)) {
                    onArcMouseOver(arcComponent);
                }
            }
        });

        arcs.add(arcComponent);

        sourceNode.addConnectedArc(arcComponent);
        targetNode.addConnectedArc(arcComponent);

        arcGroup.appendChild(arcComponent);
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

        DefaultLayoutNodeType layoutNodeType = getNodeType(node.getType());

        final NodeSvgComponent nodeComponent = nodeComponentFactory
                .createNodeComponent(node, layoutNodeType);
        layoutNodeType.add(nodeComponent);

        nodeComponent.setNodeEventListener(new SvgNodeEventHandler(
                nodeComponent, this, nodeInteractionManager));
        nodeComponent.setExpanderTabEventListener(new ChooselEventHandler() {

            @Override
            public void onEvent(ChooselEvent event) {
                if (event.getEventType().equals(ChooselEvent.Type.CLICK)) {
                    onNodeTabClick(nodeComponent);
                }
            }

        });

        nodes.add(nodeComponent);

        // if this isn't the first node, need to position it
        // XXX remove this once FlexVis has been completely replaced
        if (isWidgetInitialized() && nodes.size() > 1) {
            setLocation(node, new Point(width / 2, height / 2));
        }

        nodeGroup.appendChild(nodeComponent);
    }

    @Override
    public void addNodeMenuItemHandler(String menuLabel,
            NodeMenuItemClickedHandler handler, String nodeType) {

        assert menuLabel != null;
        assert handler != null;
        assert nodeType != null;

        DefaultLayoutNodeType layoutNodeType = getNodeType(nodeType);

        if (!nodeMenuItemClickHandlersByType.containsKey(layoutNodeType)) {
            nodeMenuItemClickHandlersByType.put(layoutNodeType,
                    CollectionFactory
                            .<NodeMenuItemClickedHandler> createStringMap());
        }
        nodeMenuItemClickHandlersByType.get(layoutNodeType).put(menuLabel,
                handler);
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
    public List<LayoutArc> getAllArcs() {
        // TODO is there a way to cast an existing List<ArcSvgComponent> down
        // to List<LayoutArc> ? Because then arcs.asList() could be used
        // instead of making new list
        List<LayoutArc> layoutArcs = new ArrayList<LayoutArc>();
        for (ArcSvgComponent layoutArc : arcs) {
            layoutArcs.add(layoutArc);
        }
        return layoutArcs;
    }

    @Override
    public List<LayoutNode> getAllNodes() {
        // TODO is there a way to cast an existing List<NodeSvgComponent> down
        // to List<LayoutNode> ? Because then nodes.asList() could be used
        // instead of making new list
        List<LayoutNode> layoutNodes = new ArrayList<LayoutNode>();
        for (LayoutNode layoutNode : nodes) {
            layoutNodes.add(layoutNode);
        }
        return layoutNodes;
    }

    @Override
    public Arc getArc(String arcId) {
        assert arcId != null;
        assert arcs.contains(arcId);
        return arcs.get(arcId).getArc();
    }

    /**
     * Retrieves the <code>LayoutArcType</code> for an arc. Creates the arc type
     * if it doesn't already exist.
     * 
     * @param node
     *            the node whose type is to be determined
     * @return the type of the node
     */
    private DefaultLayoutArcType getArcType(String arcType) {
        DefaultLayoutArcType layoutArcType = null;
        if (!arcTypes.contains(arcType)) {
            layoutArcType = new DefaultLayoutArcType(arcType);
            arcTypes.add(layoutArcType);
        } else {
            layoutArcType = arcTypes.get(arcType);
        }
        return layoutArcType;
    }

    @Override
    public List<LayoutArcType> getArcTypes() {
        // TODO is there a way to cast an existing List<DefaultLayoutArcType>
        // down
        // to List<LayoutArcType> ? Because then arcTypes.asList() could be used
        // instead of making new list
        List<LayoutArcType> layoutArcTypes = new ArrayList<LayoutArcType>();
        for (DefaultLayoutArcType layoutArcType : arcTypes) {
            layoutArcTypes.add(layoutArcType);
        }
        return layoutArcTypes;
    }

    @Override
    public BoundsDouble getBounds() {
        // TODO x and y always 0?
        return new DefaultBoundsDouble(0, 0, width, height);
    }

    protected int getGraphAbsoluteLeft() {
        return asWidget.getAbsoluteLeft();
    }

    protected int getGraphAbsoluteTop() {
        return asWidget.getAbsoluteTop();
    }

    @Override
    public LayoutGraph getLayoutGraph() {
        return this;
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

    protected NodeSvgComponent getNodeComponent(Node node) {
        return nodes.get(node.getId());
    }

    /**
     * Retrieves the <code>LayoutNodeType</code> for a node. Creates the node
     * type if it doesn't already exist.
     * 
     * @param node
     *            the node whose type is to be determined
     * @return the type of the node
     */
    private DefaultLayoutNodeType getNodeType(String nodeType) {
        DefaultLayoutNodeType layoutNodeType = null;
        if (!nodeTypes.contains(nodeType)) {
            layoutNodeType = new DefaultLayoutNodeType(nodeType);
            nodeTypes.add(layoutNodeType);
        } else {
            layoutNodeType = nodeTypes.get(nodeType);
        }
        return layoutNodeType;
    }

    @Override
    public List<LayoutNodeType> getNodeTypes() {
        // TODO is there a way to cast an existing List<DefaultLayoutNodeType>
        // down to List<LayoutNodeType> ? Because then nodeTypes.asList() could
        // be used instead of making new list
        List<LayoutNodeType> layoutNodeTypes = new ArrayList<LayoutNodeType>();
        for (DefaultLayoutNodeType layoutNodeType : nodeTypes) {
            layoutNodeTypes.add(layoutNodeType);
        }
        return layoutNodeTypes;
    }

    private void initBackground(int width, int height) {
        background = svgElementFactory.createElement(Svg.RECT);
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

    public void onArcMouseOver(ArcSvgComponent arcComponent) {
        // bring connected nodes to front
        nodeGroup.appendChild(arcComponent.getSource());
        nodeGroup.appendChild(arcComponent.getTarget());
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

    public void onNodeTabClick(final NodeSvgComponent nodeComponent) {
        Map<String, NodeMenuItemClickedHandler> nodeMenuItemClickHandlers = nodeMenuItemClickHandlersByType
                .get(nodeComponent.getType());
        PopupExpanderSvgComponent popupExpanderList = expanderPopupFactory
                .createExpanderPopupList(
                        nodeComponent.getExpanderTabAbsoluteLocation(),
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
                        handler.onNodeMenuItemClicked(nodeComponent.getNode());
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

    @Override
    public void onResize(ViewResizeEvent resizeEvent) {
        width = resizeEvent.getWidth();
        height = resizeEvent.getHeight();
        background.setAttribute(Svg.WIDTH, width);
        background.setAttribute(Svg.HEIGHT, height);
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

        List<ArcSvgComponent> connectedArcComponents = new ArrayList<ArcSvgComponent>();
        connectedArcComponents.addAll(nodes.get(node.getId())
                .getConnectedArcComponents());

        for (ArcSvgComponent arcComponent : connectedArcComponents) {
            removeArc(arcComponent.getArc());
        }
        nodes.remove(node.getId());
        nodeGroup.removeChild(node.getId());
    }

    @Override
    public void runLayout() throws LayoutException {
        // TODO remove? or choose some default layout?
    }

    @Override
    public LayoutComputation runLayout(LayoutAlgorithm layoutAlgorithm) {
        return layoutAlgorithm.computeLayout(this);
    }

    @Override
    public void runLayout(String layout) throws LayoutException {
        // TODO remove? Was used for Flash layouts
    }

    @Override
    public void runLayoutOnNodes(Collection<Node> nodes) throws LayoutException {
        // TODO remove? New layout interfaces have a LayoutGraph passed in.
        // Nodes can be anchored if they should not get moved.
    }

    @Override
    public void setArcStyle(Arc arc, String styleProperty, String styleValue) {
        ArcSvgComponent arcComponent = arcs.get(arc.getId());

        if (styleProperty.equals(ArcSettings.ARC_COLOR)) {
            arcComponent.setColor(styleValue);
        }

        else if (styleProperty.equals(ArcSettings.ARC_STYLE)) {
            arcComponent.setArcStyle(styleValue);
        }

        else if (styleProperty.equals(ArcSettings.ARC_THICKNESS)) {
            arcComponent.setArcThickness(styleValue);
        }
    }

    @Override
    public void setLocation(Node node, Point location) {
        assert nodes.contains(node.getId());
        nodes.get(node.getId()).setPosition(location);
    }

    @Override
    public void setNodeStyle(Node node, String styleProperty, String styleValue) {
        NodeSvgComponent nodeComponent = nodes.get(node.getId());

        if (styleProperty.equals(NODE_BACKGROUND_COLOR)) {
            nodeComponent.setBackgroundColor(styleValue);
        }

        else if (styleProperty.equals(NODE_FONT_COLOR)) {
            nodeComponent.setFontColor(styleValue);
        }

        else if (styleProperty.equals(NODE_FONT_WEIGHT)) {
            nodeComponent.setFontWeight(styleValue);
        }

        else if (styleProperty.equals(NODE_BORDER_COLOR)) {
            nodeComponent.setBorderColor(styleValue);
        }

    }

}
