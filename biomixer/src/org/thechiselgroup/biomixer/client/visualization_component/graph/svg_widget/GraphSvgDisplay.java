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
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.animation.GwtAnimationRunner;
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
import org.thechiselgroup.biomixer.client.visualization_component.graph.ScrollableSvgWidget;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimation;
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

    private IdentifiablesList<SvgLayoutNode> layoutNodes = new IdentifiablesList<SvgLayoutNode>();

    private SvgWidget svgWidget = null;

    private ScrollableSvgWidget asScrollingWidget = null;

    protected CompositeSvgComponent rootSvgComponent = null;

    private CompositeSvgComponent arcGroup;

    private CompositeSvgComponent nodeGroup;

    protected CompositeSvgComponent popupGroup;

    private EventBus eventBus = new SimpleEventBus();

    private int totalViewWidth;

    private int totalViewHeight;

    private NodeInteractionManager nodeInteractionManager;

    private SvgExpanderPopupFactory expanderPopupFactory;

    protected TextBoundsEstimator textBoundsEstimator;

    private ChooselEventHandler viewWideInteractionListener;

    private GraphBackground background;

    private IdentifiablesList<DefaultLayoutNodeType> nodeTypes = new IdentifiablesList<DefaultLayoutNodeType>();

    private IdentifiablesList<DefaultLayoutArcType> arcTypes = new IdentifiablesList<DefaultLayoutArcType>();

    protected AnimationRunner animationRunner;

    private int animationDuration = 3000;

    // maps node types to their available menu item click handlers and those
    // handlers' associated labels
    private Map<LayoutNodeType, Map<String, NodeMenuItemClickedHandler>> nodeMenuItemClickHandlersByType = new HashMap<LayoutNodeType, Map<String, NodeMenuItemClickedHandler>>();

    public GraphSvgDisplay(int width, int height) {
        this(width, height, new JsDomSvgElementFactory());
    }

    public GraphSvgDisplay(int width, int height,
            SvgElementFactory svgElementFactory) {
        this.totalViewWidth = width;
        this.totalViewHeight = height;
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

        this.animationRunner = initAnimationRunner();
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

        SvgLayoutNode sourceNode = layoutNodes.get(sourceNodeId);
        SvgLayoutNode targetNode = layoutNodes.get(targetNodeId);
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

        sourceNode.getRenderedNode().addConnectedArc(arcComponent);
        targetNode.getRenderedNode().addConnectedArc(arcComponent);

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

        SvgLayoutNode layoutNode = new SvgLayoutNode(nodeComponent,
                layoutNodeType);
        layoutNodes.add(layoutNode);
        layoutNodeType.add(layoutNode);

        // if this isn't the first node, need to position it
        // XXX remove this once FlexVis has been completely replaced
        if (isWidgetInitialized() && nodes.size() > 1) {
            setLocation(node,
                    new Point(totalViewWidth / 2, totalViewHeight / 2));
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
        LayoutNodeAnimation animation = new LayoutNodeAnimation(
                layoutNodes.get(node.getId()), targetLocation.getX(),
                targetLocation.getY());
        animationRunner.run(animation, 2);
    }

    public SvgElement asSvg() {
        return rootSvgComponent.getSvgElement();
    }

    @Override
    public Widget asWidget() {
        if (!isWidgetInitialized()) {
            svgWidget = new SvgWidget();
            rootSvgComponent = new CompositeSvgComponent(
                    svgWidget.getSvgElement(), rootSvgComponent,
                    viewWideInteractionListener);
            asScrollingWidget = new ScrollableSvgWidget(svgWidget,
                    totalViewWidth, totalViewHeight);
        }
        return asScrollingWidget;
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
        List<LayoutNode> downcastLayoutNodes = new ArrayList<LayoutNode>();
        for (LayoutNode layoutNode : layoutNodes) {
            downcastLayoutNodes.add(layoutNode);
        }
        return downcastLayoutNodes;
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
        return new DefaultBoundsDouble(0, 0, totalViewWidth, totalViewHeight);
    }

    protected int getGraphAbsoluteLeft() {
        return svgWidget.getAbsoluteLeft();
    }

    protected int getGraphAbsoluteTop() {
        return svgWidget.getAbsoluteTop();
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

    /**
     * 
     * @return the maximum x value used by a node on the graph.
     */
    private double getMaxNodeX() {
        double maxNodeX = 0;
        for (LayoutNode node : getAllNodes()) {
            double nodeRightX = node.getX() + node.getSize().getWidth();
            if (nodeRightX > maxNodeX) {
                maxNodeX = nodeRightX;
            }
        }
        return maxNodeX;
    }

    /**
     * 
     * @return the maximum x value used by a node on the graph.
     */
    private double getMaxNodeY() {
        double maxNodeY = 0;
        for (LayoutNode node : getAllNodes()) {
            double nodeBottomY = node.getY() + node.getSize().getHeight();
            if (nodeBottomY > maxNodeY) {
                maxNodeY = nodeBottomY;
            }
        }
        return maxNodeY;
    }

    @Override
    public Node getNode(String nodeId) {
        assert nodeId != null;
        assert nodes.contains(nodeId);
        return nodes.get(nodeId).getNode();
    }

    @Override
    public BoundsDouble getNodeBounds() {
        double minX = Double.MAX_VALUE;
        double maxX = 0;
        double minY = Double.MAX_VALUE;
        double maxY = 0;
        for (LayoutNode layoutNode : getAllNodes()) {
            SizeDouble size = layoutNode.getSize();
            double nodeLeftX = layoutNode.getX();
            if (nodeLeftX < minX) {
                minX = nodeLeftX;
            }
            double nodeRightX = layoutNode.getX() + size.getWidth();
            if (nodeRightX > maxX) {
                maxX = nodeRightX;
            }
            double nodeTopY = layoutNode.getY();
            if (nodeTopY < minY) {
                minY = nodeTopY;
            }
            double nodeBottomY = layoutNode.getY() + size.getHeight();
            if (nodeBottomY > maxY) {
                maxY = nodeBottomY;
            }
        }
        return new DefaultBoundsDouble(minX, minY, maxX - minX, maxY - minY);
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

    protected AnimationRunner initAnimationRunner() {
        return new GwtAnimationRunner();
    }

    private void initBackground(int width, int height) {
        background = new GraphBackground(width, height, svgElementFactory);

        background.setEventListener(new DragAndClickHandler() {
            @Override
            public void handleClick(ClickEvent clickEvent) {
                onBackgroundClick(clickEvent.getClickX(),
                        clickEvent.getClickY());
            }

            @Override
            public void handleDrag(DragEvent dragEvent) {
                panBackground(dragEvent.getDeltaX(), dragEvent.getDeltaY());
            }
        });

        rootSvgComponent.appendChild(background.asSvg());
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
        return svgWidget != null;
    }

    public void onArcMouseOver(ArcSvgComponent arcComponent) {
        // bring connected nodes to front
        nodeGroup.appendChild(arcComponent.getRenderedSource());
        nodeGroup.appendChild(arcComponent.getRenderedTarget());
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

        setLocation(nodes.get(nodeId).getNode(), new Point(endX, endY));
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
        totalViewWidth = resizeEvent.getWidth();
        totalViewHeight = resizeEvent.getHeight();

        /*
         * Make sure nodes that go off screen can still be scrolled to
         */
        if (totalViewWidth > getMaxNodeX()) {
            background.setWidth(totalViewWidth);
            asScrollingWidget.updateWidth(totalViewWidth);
        }
        if (totalViewHeight > getMaxNodeY()) {
            background.setHeight(totalViewHeight);
            asScrollingWidget.updateHeight(totalViewHeight);
        }
    }

    public void onViewMouseMove(int mouseX, int mouseY) {
        nodeInteractionManager.onMouseMove(mouseX, mouseY);
    }

    private void onWidgetReady() {
        eventBus.fireEvent(new GraphDisplayReadyEvent(this));
    }

    public void panBackground(int deltaX, int deltaY) {
        /*
         * Only allow panning to the left if it will not push any node off the
         * left hand side, and only allow panning up if it will not push any
         * node off the top of the graph. Panning right or down may push nodes
         * off the screen.
         */
        BoundsDouble nodeBounds = getNodeBounds();
        if (nodeBounds.getLeftX() + deltaX > 0) {
            /*
             * Don't let background width become less than view width.
             */
            double newBackgroundWidth = background.getWidth() + deltaX;
            if (newBackgroundWidth >= totalViewWidth) {
                background.setWidth(newBackgroundWidth);
                asScrollingWidget.updateWidth((int) newBackgroundWidth);
            }
            shiftGraphContentsHorizontally(deltaX);
        }
        if (nodeBounds.getTopY() + deltaY > 0) {
            /*
             * Don't let background height become less than view height.
             */
            double newBackgroundHeight = background.getHeight() + deltaY;
            if (newBackgroundHeight >= totalViewHeight) {
                background.setHeight(background.getHeight() + deltaY);
                asScrollingWidget.updateHeight((int) newBackgroundHeight);
            }
            shiftGraphContentsVertically(deltaY);
        }
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
        layoutNodes.remove(node.getId());
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
        layoutNodes.get(node.getId()).setPosition(location.getX(),
                location.getY());
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

    private void shiftGraphContentsHorizontally(int deltaX) {
        for (LayoutNode layoutNode : getAllNodes()) {
            layoutNode.setX(layoutNode.getX() + deltaX);
        }
    }

    private void shiftGraphContentsVertically(int deltaY) {
        for (LayoutNode layoutNode : getAllNodes()) {
            layoutNode.setY(layoutNode.getY() + deltaY);
        }

    }

}
