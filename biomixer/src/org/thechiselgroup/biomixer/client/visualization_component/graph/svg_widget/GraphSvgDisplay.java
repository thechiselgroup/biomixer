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
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
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
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.ScrollableSvgWidget;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.SvgWidget;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultLayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultLayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.SvgLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.ArcSvgComponent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.BoxedTextSvgComponent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.CompositeSvgComponent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSvgComponent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.PopupExpanderSvgComponent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.SvgExpanderPopupFactory;
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

public class GraphSvgDisplay implements GraphDisplay, ViewResizeEventListener {

    private SvgElementFactory svgElementFactory;

    private ArcComponentFactory arcComponentFactory;

    private NodeComponentFactory nodeComponentFactory;

    private IdentifiablesList<NodeSvgComponent> nodes = new IdentifiablesList<NodeSvgComponent>();

    private IdentifiablesList<ArcSvgComponent> arcs = new IdentifiablesList<ArcSvgComponent>();

    private SvgWidget svgWidget = null;

    private ScrollableSvgWidget asScrollingWidget = null;

    protected CompositeSvgComponent rootSvgComponent = null;

    private CompositeSvgComponent arcGroup;

    private CompositeSvgComponent nodeGroup;

    protected CompositeSvgComponent popupGroup;

    private EventBus eventBus = new SimpleEventBus();

    private int viewWidth;

    private int viewHeight;

    private SvgLayoutGraph layoutGraph;

    private NodeInteractionManager nodeInteractionManager;

    private SvgExpanderPopupFactory expanderPopupFactory;

    protected TextBoundsEstimator textBoundsEstimator;

    private ChooselEventHandler viewWideInteractionListener;

    private GraphBackground background;

    private IdentifiablesList<DefaultLayoutNodeType> nodeTypes = new IdentifiablesList<DefaultLayoutNodeType>();

    private IdentifiablesList<DefaultLayoutArcType> arcTypes = new IdentifiablesList<DefaultLayoutArcType>();

    protected AnimationRunner animationRunner;

    // maps node types to their available menu item click handlers and those
    // handlers' associated labels
    private Map<LayoutNodeType, Map<String, NodeMenuItemClickedHandler>> nodeMenuItemClickHandlersByType = new HashMap<LayoutNodeType, Map<String, NodeMenuItemClickedHandler>>();

    public GraphSvgDisplay(int width, int height) {
        this(width, height, new JsDomSvgElementFactory());
    }

    public GraphSvgDisplay(int width, int height,
            SvgElementFactory svgElementFactory) {
        this.viewWidth = width;
        this.viewHeight = height;
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

        this.layoutGraph = new SvgLayoutGraph(width, height);
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

        SvgLayoutNode sourceNode = layoutGraph.getSvgLayoutNode(sourceNodeId);
        SvgLayoutNode targetNode = layoutGraph.getSvgLayoutNode(targetNodeId);
        final ArcSvgComponent arcComponent = arcComponentFactory
                .createArcComponent(arc, layoutArcType, sourceNode, targetNode);

        arcComponent.setEventListener(new ChooselEventHandler() {

            @Override
            public void onEvent(ChooselEvent event) {
                if (event.getEventType().equals(ChooselEvent.Type.MOUSE_OVER)) {
                    onArcMouseOver(arcComponent);
                }
            }
        });

        arcs.add(arcComponent);

        SvgLayoutArc layoutArc = new SvgLayoutArc(arcComponent, layoutArcType);
        layoutGraph.addSvgLayoutArc(layoutArc);

        sourceNode.addConnectedArc(layoutArc);
        targetNode.addConnectedArc(layoutArc);

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
        setDefaultPosition(layoutNode);
        layoutGraph.addSvgLayoutNode(layoutNode);

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
                layoutGraph.getSvgLayoutNode(node.getId()),
                targetLocation.getX(), targetLocation.getY());
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
            asScrollingWidget = new ScrollableSvgWidget(svgWidget, viewWidth,
                    viewHeight);
            asScrollingWidget.setTextUnselectable();
            asScrollingWidget.getElement().getStyle()
                    .setBackgroundColor("white");
        }
        return asScrollingWidget;
    }

    /**
     * Clear the node expander popup if there is one. This does not get rid of
     * the on mouse-over node details though, which is done using HTML not SVG.
     */
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

    protected int getGraphAbsoluteLeft() {
        return svgWidget.getAbsoluteLeft();
    }

    protected int getGraphAbsoluteTop() {
        return svgWidget.getAbsoluteTop();
    }

    /**
     * 
     * @return the offset distance from the absolute left of the view to the
     *         left of the visible view. This is non-zero if the view has been
     *         panned.
     */
    private double getHorizontalScrollDistance() {
        return background.getWidth() - viewWidth;
    }

    @Override
    public LayoutGraph getLayoutGraph() {
        return layoutGraph;
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

    /**
     * 
     * @return the offset distance from the absolute top of the view to the top
     *         of the visible view. This is non-zero if the view has been
     *         panned.
     */
    private double getVerticalScrollDistance() {
        return background.getHeight() - viewHeight;
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
        int x = mouseX - getGraphAbsoluteLeft()
                - (int) getHorizontalScrollDistance();
        int y = mouseY - getGraphAbsoluteTop()
                - (int) getVerticalScrollDistance();

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
        viewWidth = resizeEvent.getWidth();
        viewHeight = resizeEvent.getHeight();
        layoutGraph.setWidth(viewWidth);
        layoutGraph.setHeight(viewHeight);

        /*
         * Make sure nodes that go off screen can still be scrolled to
         */
        if (viewWidth > layoutGraph.getMaxNodeX()) {
            background.setWidth(viewWidth);
            asScrollingWidget.setScrollableContentWidth(viewWidth);
        }
        if (viewHeight > layoutGraph.getMaxNodeY()) {
            background.setHeight(viewHeight);
            asScrollingWidget.setScrollableContentHeight(viewHeight);
        }

        // need this in case scrollable content size is not changed but the
        // available
        asScrollingWidget.checkIfScrollbarsNeeded();
    }

    public void onViewMouseMove(int mouseX, int mouseY) {
        nodeInteractionManager.onMouseMove(mouseX, mouseY);
    }

    private void onWidgetReady() {
        eventBus.fireEvent(new GraphDisplayReadyEvent(this));
    }

    public void panBackground(int deltaX, int deltaY) {
        clearPopups();

        BoundsDouble nodeBounds = layoutGraph.getNodeBounds();
        /*
         * Only allow panning to the left if it will not push any node off the
         * left hand side, and only allow panning up if it will not push any
         * node off the top of the graph. Panning right or down may push nodes
         * off the screen.
         */
        if (nodeBounds.getLeftX() + deltaX > 0) {
            /*
             * Only extend background if a node would be pushed off the screen.
             */
            if (deltaX < 0
                    || layoutGraph.getMaxNodeX() + deltaX > background
                            .getWidth()) {
                double newBackgroundWidth = background.getWidth() + deltaX;
                /*
                 * Don't let background width become less than view width.
                 */
                if (newBackgroundWidth < viewWidth) {
                    newBackgroundWidth = viewWidth;
                }
                background.setWidth(newBackgroundWidth);
                asScrollingWidget
                        .setScrollableContentWidth((int) newBackgroundWidth);
            }
            /*
             * Still shift the nodes even if the background was not adjusted.
             * The outer if statement makes sure this wouldn't push them in a
             * negative direction.
             */
            layoutGraph.shiftContentsHorizontally(deltaX);
        }

        if (nodeBounds.getTopY() + deltaY > 0) {
            /*
             * Only extend background if a node would be pushed off the screen.
             */
            if (deltaY < 0
                    || layoutGraph.getMaxNodeY() + deltaY > background
                            .getHeight()) {
                double newBackgroundHeight = background.getHeight() + deltaY;
                /*
                 * Don't let background height become less than view height.
                 */
                if (newBackgroundHeight < viewHeight) {
                    newBackgroundHeight = viewHeight;
                }
                background.setHeight(newBackgroundHeight);
                asScrollingWidget
                        .setScrollableContentHeight((int) newBackgroundHeight);
            }
            /*
             * Still shift the nodes even if the background was not adjusted.
             * The outer if statement makes sure this wouldn't push them in a
             * negative direction.
             */
            layoutGraph.shiftContentsVertically(deltaY);
        }
    }

    @Override
    public void removeArc(Arc arc) {
        assert arc != null;
        String id = arc.getId();
        assert arcs.contains(id);
        arcs.get(id).removeNodeConnections();
        arcs.remove(id);
        layoutGraph.removeSvgLayoutArc(id);
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
        layoutGraph.removeSvgLayoutNode(node.getId());
        nodeGroup.removeChild(node.getId());
    }

    @Override
    public void runLayout() throws LayoutException {
        // XXX layouts run from Graph only? That is where the layout execution
        // manager is.
    }

    @Override
    public LayoutComputation runLayout(LayoutAlgorithm layoutAlgorithm) {
        // XXX layouts run from Graph only? That is where the layout execution
        // manager is.
        return layoutAlgorithm.computeLayout(layoutGraph);
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
            arcComponent.setThickness(styleValue);
        }
    }

    /**
     * Positions a node at the default position which is the centre of the
     * graph.
     * 
     * @param node
     *            the node to be positioned
     */
    private void setDefaultPosition(LayoutNode node) {
        if (isWidgetInitialized()) {
            PointDouble topLeft = node.getTopLeftForCentreAt(layoutGraph
                    .getBounds().getCentre());
            node.setPosition(topLeft);
        }
    }

    @Override
    public void setLocation(Node node, Point location) {
        assert nodes.contains(node.getId());
        layoutGraph.getSvgLayoutNode(node.getId()).setPosition(location.getX(),
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

}
