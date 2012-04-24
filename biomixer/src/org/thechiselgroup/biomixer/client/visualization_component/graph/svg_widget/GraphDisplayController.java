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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.animation.GwtAnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.core.util.executor.DelayedExecutor;
import org.thechiselgroup.biomixer.client.core.util.executor.GwtDelayedExecutor;
import org.thechiselgroup.biomixer.client.core.util.text.CanvasTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.SvgBBoxTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewResizeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewResizeEventListener;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.JsDomSvgElementFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutExecutionManager;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputationFinishedEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputationFinishedHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraphContentChangedEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraphContentChangedListener;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultLayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultLayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.IdentifiableLayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.IdentifiableLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.IdentifiableLayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.BoundsAwareAttractionCalculator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.BoundsAwareRepulsionCalculator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.CompositeForceCalculator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.ForceDirectedLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.GraphRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.SvgGraphRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.arcs.StraightLineSvgArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.expanders.BoxedTextSvgNodeExpanderRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes.BoxedTextSvgNodeRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.LayoutException;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMenuItemClickedHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseClickEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseDoubleClickEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseOutEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseOverEvent;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;

public class GraphDisplayController implements GraphDisplay,
        ViewResizeEventListener {

    private SvgElementFactory svgElementFactory;

    private Map<String, Node> nodes = CollectionFactory.createStringMap();

    private Map<String, Arc> arcs = CollectionFactory.createStringMap();

    private EventBus eventBus = new SimpleEventBus();

    private int viewWidth;

    private int viewHeight;

    protected IdentifiableLayoutGraph layoutGraph;

    protected GraphRenderer graphRenderer;

    private NodeInteractionManager nodeInteractionManager;

    private NodeAnimator nodeAnimator;

    private GraphLayoutExecutionManager layoutManager;

    private static final int DEFAULT_ANIMATION_DURATION = 250;

    /*
     * maps node types to their available menu item click handlers and those
     * handlers' associated labels
     */
    private Map<String, Map<String, NodeMenuItemClickedHandler>> nodeMenuItemClickHandlersByType = CollectionFactory
            .createStringMap();

    protected AnimationRunner animationRunner;

    public GraphDisplayController(int width, int height,
            ErrorHandler errorHandler) {
        this(width, height, new JsDomSvgElementFactory(), errorHandler);
        /* Don't want layouts to be run automatically in unit tests */
        setLayoutGraphContentChangedListener();
    }

    public GraphDisplayController(int width, int height,
            SvgElementFactory svgElementFactory, ErrorHandler errorHandler) {
        this.viewWidth = width;
        this.viewHeight = height;
        assert svgElementFactory != null;
        this.svgElementFactory = svgElementFactory;

        nodeInteractionManager = new NodeInteractionManager(this);

        TextBoundsEstimator textBoundsEstimator = getTextBoundsEstimator();
        this.graphRenderer = new SvgGraphRenderer(width, height,
                svgElementFactory, new BoxedTextSvgNodeRenderer(
                        svgElementFactory, textBoundsEstimator),
                new StraightLineSvgArcRenderer(svgElementFactory),
                new BoxedTextSvgNodeExpanderRenderer(svgElementFactory,
                        textBoundsEstimator));

        initBackgroundListener();
        initViewWideInteractionHandler();

        this.layoutGraph = new IdentifiableLayoutGraph(width, height);

        animationRunner = getAnimationRunner();
        this.nodeAnimator = new NodeAnimator(animationRunner);

        initGraphLayoutManager(errorHandler);
    }

    @Override
    public void addArc(Arc arc) {
        assert arc != null;
        assert !arcs.containsKey(arc.getId()) : "arc '" + arc.getId()
                + "' is already on the graph";
        arcs.put(arc.getId(), arc);

        String sourceNodeId = arc.getSourceNodeId();
        String targetNodeId = arc.getTargetNodeId();

        assert nodes.containsKey(sourceNodeId) : "source node '" + sourceNodeId
                + "' must be available";
        assert nodes.containsKey(targetNodeId) : "target node '" + targetNodeId
                + "' must be available";

        final RenderedArc renderedArc = graphRenderer.renderArc(arc);

        renderedArc.setEventListener(new ChooselEventHandler() {
            @Override
            public void onEvent(ChooselEvent event) {
                if (event.getEventType().equals(ChooselEvent.Type.MOUSE_OVER)) {
                    onArcMouseOver(renderedArc);
                }
            }
        });

        IdentifiableLayoutNode sourceNode = layoutGraph
                .getIdentifiableLayoutNode(sourceNodeId);
        IdentifiableLayoutNode targetNode = layoutGraph
                .getIdentifiableLayoutNode(targetNodeId);

        IdentifiableLayoutArc layoutArc = new IdentifiableLayoutArc(
                arc.getId(), renderedArc, getArcType(arc.getType()),
                sourceNode, targetNode);
        layoutGraph.addIdentifiableLayoutArc(layoutArc);

        sourceNode.addConnectedArc(layoutArc);
        targetNode.addConnectedArc(layoutArc);
    }

    @Override
    public <T extends EventHandler> HandlerRegistration addEventHandler(
            Type<T> type, T handler) {
        assert type != null;
        assert handler != null;

        return eventBus.addHandler(type, handler);
    }

    @Override
    public void addNode(Node node) {
        assert !nodes.containsKey(node.getId()) : node.toString()
                + " is already be on the graph";

        nodes.put(node.getId(), node);
        final RenderedNode renderedNode = graphRenderer.renderNode(node);

        setNodeEventHandlers(renderedNode);

        IdentifiableLayoutNode layoutNode = new IdentifiableLayoutNode(
                node.getId(), renderedNode, getNodeLayoutType(node.getType()));
        setDefaultPosition(layoutNode);
        layoutGraph.addIdentifiableLayoutNode(layoutNode);
    }

    @Override
    public void addNodeMenuItemHandler(String menuLabel,
            NodeMenuItemClickedHandler handler, String nodeType) {

        assert menuLabel != null;
        assert handler != null;
        assert nodeType != null;

        if (!nodeMenuItemClickHandlersByType.containsKey(nodeType)) {
            nodeMenuItemClickHandlersByType.put(nodeType, CollectionFactory
                    .<NodeMenuItemClickedHandler> createStringMap());
        }
        nodeMenuItemClickHandlersByType.get(nodeType).put(menuLabel, handler);

    }

    @Override
    public void animateMoveTo(Node node, Point targetLocation) {
        nodeAnimator.animateNodeTo(
                layoutGraph.getIdentifiableLayoutNode(node.getId()),
                targetLocation, DEFAULT_ANIMATION_DURATION);
    }

    @Override
    public Widget asWidget() {
        return graphRenderer.getGraphWidget();
    }

    @Override
    public boolean containsArc(String arcId) {
        assert arcId != null;
        return arcs.containsKey(arcId);
    }

    @Override
    public boolean containsNode(String nodeId) {
        assert nodeId != null;
        return nodes.containsKey(nodeId);
    }

    /**
     * Override in tests to get a test animation runner.
     * 
     * @return a GwtAnimationRunner which will not work in java unit tests
     *         because it uses Javascript
     */
    protected AnimationRunner getAnimationRunner() {
        return new GwtAnimationRunner();
    }

    @Override
    public Arc getArc(String arcId) {
        assert arcId != null;
        assert arcs.containsKey(arcId);
        return arcs.get(arcId);
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
        if (!layoutGraph.containsArcType(arcType)) {
            layoutArcType = new DefaultLayoutArcType(arcType);
            layoutGraph.addLayoutArcType(layoutArcType);
        } else {
            layoutArcType = layoutGraph.getArcType(arcType);
        }
        return layoutArcType;
    }

    protected DelayedExecutor getDelayedExecutor() {
        return new GwtDelayedExecutor();
    }

    protected int getGraphAbsoluteLeft() {
        return graphRenderer.getGraphWidget().getAbsoluteLeft();
    }

    protected int getGraphAbsoluteTop() {
        return graphRenderer.getGraphWidget().getAbsoluteTop();
    }

    /**
     * 
     * @return the offset distance from the absolute left of the view to the
     *         left of the visible view. This is non-zero if the view has been
     *         panned.
     */
    private double getHorizontalScrollDistance() {
        return graphRenderer.getGraphSize().getWidth() - viewWidth;
    }

    @Override
    public LayoutGraph getLayoutGraph() {
        return layoutGraph;
    }

    @Override
    public Point getLocation(Node node) {
        assert node != null;
        assert nodes.containsKey(node.getId());
        return graphRenderer.getRenderedNode(node).getTopLeft().toPointInt();
    }

    @Override
    public Node getNode(String nodeId) {
        assert nodeId != null;
        assert nodes.containsKey(nodeId);
        return nodes.get(nodeId);
    }

    /**
     * Retrieves the <code>LayoutNodeType</code> for a node. Creates the node
     * type if it doesn't already exist.
     * 
     * @param node
     *            the node whose type is to be determined
     * @return the type of the node
     */
    private DefaultLayoutNodeType getNodeLayoutType(String nodeType) {
        DefaultLayoutNodeType layoutNodeType = null;
        if (!layoutGraph.containsNodeType(nodeType)) {
            layoutNodeType = new DefaultLayoutNodeType(nodeType);
            layoutGraph.addLayoutNodeType(layoutNodeType);
        } else {
            layoutNodeType = layoutGraph.getNodeType(nodeType);
        }
        return layoutNodeType;
    }

    protected TextBoundsEstimator getTextBoundsEstimator() {
        return new CanvasTextBoundsEstimator(new SvgBBoxTextBoundsEstimator(
                svgElementFactory));
    }

    /**
     * 
     * @return the offset distance from the absolute top of the view to the top
     *         of the visible view. This is non-zero if the view has been
     *         panned.
     */
    private double getVerticalScrollDistance() {
        return graphRenderer.getGraphSize().getHeight() - viewHeight;
    }

    private void initBackgroundListener() {
        graphRenderer.setBackgroundEventListener(new DragAndClickHandler() {
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
    }

    private void initGraphLayoutManager(ErrorHandler errorHandler) {
        this.layoutManager = new GraphLayoutExecutionManager(
                new ForceDirectedLayoutAlgorithm(new CompositeForceCalculator(
                        new BoundsAwareAttractionCalculator(getLayoutGraph()),
                        new BoundsAwareRepulsionCalculator(getLayoutGraph())),
                        0.9, animationRunner, getDelayedExecutor(),
                        errorHandler), getLayoutGraph());
    }

    private void initViewWideInteractionHandler() {
        ChooselEventHandler viewWideInteractionListener = new ChooselEventHandler() {
            @Override
            public void onEvent(ChooselEvent event) {
                if (event.getEventType().equals(ChooselEvent.Type.MOUSE_MOVE)) {
                    onViewMouseMove(event.getClientX(), event.getClientY());
                }
            }
        };
        graphRenderer
                .setViewWideInteractionHandler(viewWideInteractionListener);
    }

    public void onArcMouseOver(RenderedArc arc) {
        // bring connected nodes to front
        graphRenderer.bringToForeground(arc.getSource());
        graphRenderer.bringToForeground(arc.getTarget());
    }

    public void onBackgroundClick(int mouseX, int mouseY) {
        graphRenderer.removeAllNodeExpanders();
    }

    public void onNodeDrag(String nodeId, int deltaX, int deltaY) {
        Node node = nodes.get(nodeId);
        Point startLocation = graphRenderer.getRenderedNode(node).getTopLeft()
                .toPointInt();
        int startX = startLocation.getX();
        int startY = startLocation.getY();
        int endX = startX + deltaX;
        int endY = startY + deltaY;

        setLocation(node, new Point(endX, endY));
        eventBus.fireEvent(new NodeDragEvent(node, startX, startY, endX, endY));
        graphRenderer.removeAllNodeExpanders();
    }

    public void onNodeExpanderClick(RenderedNodeExpander expander,
            String optionId) {
        Node node = expander.getNode();
        nodeMenuItemClickHandlersByType.get(node.getType()).get(optionId)
                .onNodeMenuItemClicked(node);
        graphRenderer.removeAllNodeExpanders();
    }

    public void onNodeExpanderMouseOut(RenderedNodeExpander expander,
            String optionId) {
        // set back to default colour
        expander.setOptionBackgroundColor(optionId, Colors.WHITE);
    }

    public void onNodeExpanderMouseOver(RenderedNodeExpander expander,
            String optionId) {
        // give highlighting colour
        expander.setOptionBackgroundColor(optionId, Colors.BLUE_1);
    }

    public void onNodeMouseClick(String nodeId, int mouseX, int mouseY) {
        int x = mouseX - getGraphAbsoluteLeft();
        int y = mouseY - getGraphAbsoluteTop();

        eventBus.fireEvent(new NodeMouseClickEvent(getNode(nodeId), x, y));
        graphRenderer.removeAllNodeExpanders();
    }

    public void onNodeMouseDoubleClick(String nodeId, int mouseX, int mouseY) {
        int x = getGraphAbsoluteLeft() + mouseX;
        int y = getGraphAbsoluteTop() + mouseY;

        eventBus.fireEvent(new NodeMouseDoubleClickEvent(getNode(nodeId), x, y));
    }

    public void onNodeMouseDown(Node node, int clientX, int clientY) {
        nodeInteractionManager.onMouseDown(node.getId(), clientX, clientY);
    }

    public void onNodeMouseOut(RenderedNode renderedNode, int mouseX, int mouseY) {
        int x = mouseX - getGraphAbsoluteLeft();
        int y = mouseY - getGraphAbsoluteTop();

        eventBus.fireEvent(new NodeMouseOutEvent(renderedNode.getNode(), x, y));
    }

    public void onNodeMouseOver(RenderedNode renderedNode, int mouseX,
            int mouseY) {
        int x = mouseX - getGraphAbsoluteLeft()
                - (int) getHorizontalScrollDistance();
        int y = mouseY - getGraphAbsoluteTop()
                - (int) getVerticalScrollDistance();

        eventBus.fireEvent(new NodeMouseOverEvent(renderedNode.getNode(), x, y));
        graphRenderer.bringToForeground(renderedNode);
    }

    public void onNodeMouseUp() {
        nodeInteractionManager.onMouseUp();
    }

    public void onNodeTabClick(final RenderedNode renderedNode) {
        graphRenderer.removeAllNodeExpanders();

        Map<String, NodeMenuItemClickedHandler> nodeMenuItemClickHandlers = nodeMenuItemClickHandlersByType
                .get(renderedNode.getType());
        final RenderedNodeExpander renderNodeExpander = graphRenderer
                .renderNodeExpander(renderedNode.getExpanderPopupLocation(),
                        nodeMenuItemClickHandlers.keySet(),
                        renderedNode.getNode());

        for (final String expanderId : nodeMenuItemClickHandlers.keySet()) {
            renderNodeExpander.setEventHandlerOnOption(expanderId,
                    new ChooselEventHandler() {
                        @Override
                        public void onEvent(ChooselEvent event) {
                            switch (event.getEventType()) {
                            case MOUSE_OVER:
                                onNodeExpanderMouseOver(renderNodeExpander,
                                        expanderId);
                                break;

                            case MOUSE_OUT:
                                onNodeExpanderMouseOut(renderNodeExpander,
                                        expanderId);
                                break;

                            case CLICK:
                                onNodeExpanderClick(renderNodeExpander,
                                        expanderId);
                                break;

                            default:
                                break;
                            }
                        }
                    });
        }

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
            graphRenderer.setGraphWidth(viewWidth);
        }
        if (viewHeight > layoutGraph.getMaxNodeY()) {
            graphRenderer.setGraphHeight(viewHeight);
        }

        /*
         * Need this in case scrollable content size is not changed but the
         * available
         */
        graphRenderer.checkIfScrollbarsNeeded();
    }

    public void onViewMouseMove(int mouseX, int mouseY) {
        nodeInteractionManager.onMouseMove(mouseX, mouseY);
    }

    public void panBackground(int deltaX, int deltaY) {
        graphRenderer.removeAllNodeExpanders();

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
            double visibleWidth = graphRenderer.getGraphSize().getWidth();
            if (deltaX < 0 || layoutGraph.getMaxNodeX() + deltaX > visibleWidth) {
                double newBackgroundWidth = visibleWidth + deltaX;
                /*
                 * Don't let background width become less than view width.
                 */
                if (newBackgroundWidth < viewWidth) {
                    newBackgroundWidth = viewWidth;
                }
                graphRenderer.setGraphWidth((int) newBackgroundWidth);
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
            double visibleHeight = graphRenderer.getGraphSize().getHeight();
            if (deltaY < 0
                    || layoutGraph.getMaxNodeY() + deltaY > visibleHeight) {
                double newBackgroundHeight = visibleHeight + deltaY;
                /*
                 * Don't let background height become less than view height.
                 */
                if (newBackgroundHeight < viewHeight) {
                    newBackgroundHeight = viewHeight;
                }
                graphRenderer.setGraphHeight((int) newBackgroundHeight);
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
    public void registerDefaultLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm) {
        layoutManager.registerDefaultAlgorithm(layoutAlgorithm);
    }

    @Override
    public void removeArc(Arc arc) {
        assert arc != null;
        arcs.remove(arc.getId());
        graphRenderer.removeArc(arc);
        layoutGraph.removeIdentifiableLayoutArc(arc.getId());
    }

    @Override
    public void removeNode(Node node) {
        assert node != null;
        nodes.remove(node.getId());
        graphRenderer.removeNode(node);
        layoutGraph.removeIdentifiableLayoutNode(node.getId());
    }

    @Override
    public void runLayout() throws LayoutException {
        layoutManager.runLayout();
    }

    @Override
    public LayoutComputation runLayout(LayoutAlgorithm layoutAlgorithm) {
        layoutManager.registerAndRunLayoutAlgorithm(layoutAlgorithm);
        // FIXME return LayoutComputations from all runLayout methods
        return null;
    }

    @Override
    public void runLayout(String layout) throws LayoutException {
        // TODO remove when addressing issue104
    }

    @Override
    public void runLayoutOnNodes(final Collection<Node> nodes)
            throws LayoutException {
        /*
         * Get a list of all the nodes which the layout should NOT be run on.
         */
        final List<String> unselectedNodeIds = new ArrayList<String>();
        unselectedNodeIds.addAll(layoutGraph.getAllNodeIds());
        for (Node node : nodes) {
            unselectedNodeIds.remove(node.getId());
        }

        /*
         * Anchor the nodes that should not be laid out
         */
        for (String nodeId : unselectedNodeIds) {
            layoutGraph.getIdentifiableLayoutNode(nodeId).setAnchored(true);
        }

        /*
         * When the layout computation finishes, unanchor the nodes that were
         * anchored
         */
        layoutManager
                .addLayoutComputationFinishedHandler(new LayoutComputationFinishedHandler() {
                    @Override
                    public void onLayoutComputationFinished(
                            LayoutComputationFinishedEvent e) {
                        for (String nodeId : unselectedNodeIds) {
                            layoutGraph.getIdentifiableLayoutNode(nodeId)
                                    .setAnchored(false);
                        }
                    }
                });
        runLayout();
    }

    @Override
    public void setArcStyle(Arc arc, String styleProperty, String styleValue) {
        graphRenderer.setArcStyle(arc, styleProperty, styleValue);
    }

    /**
     * Positions a node at the default position which is the centre of the
     * graph.
     * 
     * @param node
     *            the node to be positioned
     */
    private void setDefaultPosition(LayoutNode node) {
        if (graphRenderer.isWidgetInitialized()) {
            PointDouble topLeft = node.getTopLeftForCentreAt(layoutGraph
                    .getBounds().getCentre());
            node.setPosition(topLeft);
        }
    }

    private void setLayoutGraphContentChangedListener() {
        layoutGraph
                .addContentChangedListener(new LayoutGraphContentChangedListener() {
                    @Override
                    public void onContentChanged(
                            LayoutGraphContentChangedEvent event) {
                        runLayout();
                    }
                });
    }

    @Override
    public void setLocation(Node node, Point location) {
        assert nodes.containsKey(node.getId());
        layoutGraph.getIdentifiableLayoutNode(node.getId()).setPosition(
                location.getX(), location.getY());
    }

    private void setNodeEventHandlers(final RenderedNode renderedNode) {
        renderedNode.setBodyEventHandler(new ChooselEventHandler() {
            @Override
            public void onEvent(ChooselEvent event) {
                int clientX = event.getClientX();
                int clientY = event.getClientY();

                switch (event.getEventType()) {
                case MOUSE_OVER:
                    onNodeMouseOver(renderedNode, clientX, clientY);
                    break;

                case MOUSE_OUT:
                    onNodeMouseOut(renderedNode, clientX, clientY);
                    break;

                case MOUSE_UP:
                    onNodeMouseUp();
                    break;

                case MOUSE_DOWN:
                    onNodeMouseDown(renderedNode.getNode(), clientX, clientY);
                    break;

                default:
                    break;
                }
            }
        });

        renderedNode.setExpansionEventHandler(new ChooselEventHandler() {
            @Override
            public void onEvent(ChooselEvent event) {
                if (event.getEventType().equals(ChooselEvent.Type.CLICK)) {
                    onNodeTabClick(renderedNode);
                }
            }
        });
    }

    @Override
    public void setNodeStyle(Node node, String styleProperty, String styleValue) {
        graphRenderer.setNodeStyle(node, styleProperty, styleValue);
    }

}
