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
package org.thechiselgroup.biomixer.client.visualization_component.matrix;

import java.util.Collections;
import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.executor.DelayedExecutor;
import org.thechiselgroup.biomixer.client.core.util.executor.GwtDelayedExecutor;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewResizeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewResizeEventListener;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.GraphDisplayController;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;

/**
 * If mouse handling functionality is to be pulled up out of JS/D3 into Java,
 * see the {@link GraphDisplayController} class for the sort of things we can
 * do. I expect this will involve things such as resource highlighting...which
 * means I need D3 highlighting too.
 * 
 * @author everbeek
 * 
 */
public class NeoD3ConceptMatrixDisplayController implements
        ConceptMatrixDisplay, ViewResizeEventListener {
    // package
    // org.thechiselgroup.biomixer.client.visualization_component.matrix.svg_widget;

    private Map<String, Resource> concepts = CollectionFactory
            .createStringMap();

    // private Map<String, Arc> arcs = CollectionFactory.createStringMap();

    private EventBus eventBus = new SimpleEventBus();

    private int viewWidth;

    private int viewHeight;

    // protected IdentifiableLayoutGraph layoutGraph;

    private NeoD3MatrixWidget matrixWidget;

    // private NodeInteractionManager nodeInteractionManager;

    // private NodeAnimator nodeAnimator;

    // private GraphLayoutExecutionManager layoutManager;

    // private static final int DEFAULT_ANIMATION_DURATION = 250;

    // private static final int DEFAULT_NODE_OFFSET_RANGE = 5;

    private final String viewName;

    // /*
    // * maps node types to their available menu item click handlers and those
    // * handlers' associated labels
    // */
    // private Map<String, Map<String, NodeMenuItemClickedHandler>>
    // nodeMenuItemClickHandlersByType = CollectionFactory
    // .createStringMap();

    // protected AnimationRunner animationRunner;

    /**
     * 
     * @param width
     * @param height
     * @param matrixRenderer
     * @param errorHandler
     * @param runLayoutsAutomatically
     *            Determines whether layouts are run right away. Send in false
     *            for testing.
     */
    public NeoD3ConceptMatrixDisplayController(int width, int height,
            String viewName,
            // ConceptMatrixRendererFactory matrixRendererFactory,
            ErrorHandler errorHandler
    // , boolean runLayoutsAutomatically
    ) {
        this.viewWidth = width;
        this.viewHeight = height;
        this.viewName = viewName;

        // nodeInteractionManager = new NodeInteractionManager(this);

        this.matrixWidget = new NeoD3MatrixWidget();

        // initBackgroundListener();
        initViewWideInteractionHandler();

        // this.layoutGraph = new IdentifiableLayoutGraph(width, height);
        //
        // this.nodeAnimator = new NodeAnimator(getNodeAnimationFactory());
        //
        // initGraphLayoutManager(errorHandler);

        // if (runLayoutsAutomatically) {
        // // Don't want layouts to be run automatically in unit tests
        setMatrixContentChangedListener();
        // }
    }

    @Override
    public void updateView() {
        // I think that this is where all the concept and mapping data is
        // injected...the matrixRenderer should probably *not* know any of that.
        // In fact, maybe this is the *only* method that the renderer knows, in
        // the case of D3!!
        // Or does the matrixRenderer indeed know what data to convert? I think
        // it might...it currently gets cocnept additions and whatnot.
        this.matrixWidget.updateView();
    }

    // static final int defaultHeight = 400;
    //
    // static final int defaultWidth = 300;
    //
    // static final GraphRendererConceptGraphFactory factory = new
    // GraphRendererConceptGraphFactory();
    //
    // // // TODO why is size needed in the first place??
    // public DefaultDisplay(ErrorHandler errorHandler) {
    // super(defaultHeight, defaultWidth, "Concept Graph", factory
    // .createGraphRenderer(defaultHeight, defaultWidth),
    // errorHandler, true);
    // }
    //
    // public DefaultDisplay(int width, int height, ErrorHandler errorHandler) {
    // super(width, height, "Concept Graph", factory.createGraphRenderer(
    // width, height), errorHandler, true);
    // }

    @Override
    public String getGraphViewName() {
        return this.viewName;
    }

    // @Override
    // public void addArc(Arc arc) {
    // assert arc != null;
    // assert !arcs.containsKey(arc.getId()) : "arc '" + arc.getId()
    // + "' is already on the graph";
    // arcs.put(arc.getId(), arc);
    //
    // String sourceNodeId = arc.getSourceNodeId();
    // String targetNodeId = arc.getTargetNodeId();
    //
    // assert nodes.containsKey(sourceNodeId) : "source node '" + sourceNodeId
    // + "' must be available";
    // assert nodes.containsKey(targetNodeId) : "target node '" + targetNodeId
    // + "' must be available";
    //
    // final RenderedArc renderedArc = matrixRenderer.renderArc(arc);
    //
    // renderedArc.setEventListener(new ChooselEventHandler() {
    // @Override
    // public void onEvent(ChooselEvent event) {
    // if (event.getEventType().equals(ChooselEvent.Type.MOUSE_OVER)) {
    // onArcMouseOver(renderedArc);
    // }
    // }
    // });
    //
    // IdentifiableLayoutNode sourceNode = layoutGraph
    // .getIdentifiableLayoutNode(sourceNodeId);
    // IdentifiableLayoutNode targetNode = layoutGraph
    // .getIdentifiableLayoutNode(targetNodeId);
    //
    // IdentifiableLayoutArc layoutArc = new IdentifiableLayoutArc(
    // arc.getId(), renderedArc, getArcType(arc.getType()),
    // sourceNode, targetNode);
    // layoutGraph.addIdentifiableLayoutArc(layoutArc);
    //
    // sourceNode.addConnectedArc(layoutArc);
    // targetNode.addConnectedArc(layoutArc);
    // }

    @Override
    public <T extends EventHandler> HandlerRegistration addEventHandler(
            Type<T> type, T handler) {
        assert type != null;
        assert handler != null;

        return eventBus.addHandler(type, handler);
    }

    @Override
    public void addConcept(Resource concept) {
        String conceptId = Concept.getConceptId(concept);
        assert !concepts.containsKey(conceptId) : conceptId
                + " is already be on the graph";

        // assert !matrixWidget.containsConcept(concept) : concept.toString()
        // + " is already be on the graph";

        concepts.put(conceptId, concept);
        // final RenderedNode renderedNode = matrixRenderer.renderNode(node);
        // matrixWidget.addConcept(concept);

        // setNodeEventHandlers(renderedNode);

        // IdentifiableLayoutNode layoutNode = new IdentifiableLayoutNode(
        // node.getId(), renderedNode, getNodeLayoutType(node.getType()));
        // setDefaultPosition(layoutNode);
        // layoutGraph.addIdentifiableLayoutNode(layoutNode);
    }

    @Override
    public void removeConcept(Resource concept) {
        assert concept != null;
        concepts.remove(Concept.getConceptId(concept));
        // matrixWidget.removeConcept(concept);
        // layoutGraph.removeIdentifiableLayoutNode(node.getId());
    }

    @Override
    public Map<String, Resource> getConcepts() {
        // return Collections.unmodifiableMap(matrixWidget.getConcepts());
        return Collections.unmodifiableMap(concepts);
    }

    @Override
    public boolean containsConcept(Resource concept) {
        assert concept != null;
        // return matrixWidget.containsConcept(concept);
        return concepts.containsKey(Concept.getConceptId(concept));
    }

    @Override
    public boolean containsConcept(String conceptId) {
        assert conceptId != null;
        // return matrixWidget.containsConcept(conceptId);
        return concepts.containsKey(conceptId);
    }

    @Override
    public Resource getConcept(String nodeId) {
        assert nodeId != null;
        // assert matrixWidget.containsConcept(nodeId);
        // return matrixWidget.getConcept(nodeId);
        assert concepts.containsKey(nodeId);
        return concepts.get(nodeId);
    }

    // @Override
    // public void addNode(Node node) {
    // assert !nodes.containsKey(node.getId()) : node.toString()
    // + " is already be on the graph";
    //
    // nodes.put(node.getId(), node);
    // final RenderedNode renderedNode = matrixRenderer.renderNode(node);
    //
    // setNodeEventHandlers(renderedNode);
    //
    // IdentifiableLayoutNode layoutNode = new IdentifiableLayoutNode(
    // node.getId(), renderedNode, getNodeLayoutType(node.getType()));
    // setDefaultPosition(layoutNode);
    // layoutGraph.addIdentifiableLayoutNode(layoutNode);
    // }

    // @Override
    // public Map<String, Node> getNodes() {
    // return Collections.unmodifiableMap(nodes);
    // }

    // @Override
    // public void addNodeMenuItemHandler(String menuLabel,
    // NodeMenuItemClickedHandler handler, String nodeType) {
    //
    // assert menuLabel != null;
    // assert handler != null;
    // assert nodeType != null;
    //
    // if (!nodeMenuItemClickHandlersByType.containsKey(nodeType)) {
    // nodeMenuItemClickHandlersByType.put(nodeType, CollectionFactory
    // .<NodeMenuItemClickedHandler> createStringMap());
    // }
    // nodeMenuItemClickHandlersByType.get(nodeType).put(menuLabel, handler);
    //
    // }

    // @Override
    // public void animateMoveTo(Node node, Point targetLocation) {
    // nodeAnimator.animateNodeTo(
    // layoutGraph.getIdentifiableLayoutNode(node.getId()),
    // targetLocation, DEFAULT_ANIMATION_DURATION);
    // }

    @Override
    public Widget asWidget() {
        return matrixWidget.asWidget();
    }

    // @Override
    // public boolean containsArc(String arcId) {
    // assert arcId != null;
    // return arcs.containsKey(arcId);
    // }

    // @Override
    // public boolean containsNode(String nodeId) {
    // assert nodeId != null;
    // return nodes.containsKey(nodeId);
    // }

    // @Override
    // public Arc getArc(String arcId) {
    // assert arcId != null;
    // assert arcs.containsKey(arcId);
    // return arcs.get(arcId);
    // }

    // /**
    // * Retrieves the <code>LayoutArcType</code> for an arc. Creates the arc
    // type
    // * if it doesn't already exist.
    // *
    // * @param node
    // * the node whose type is to be determined
    // * @return the type of the node
    // */
    // private DefaultLayoutArcType getArcType(String arcType) {
    // DefaultLayoutArcType layoutArcType = null;
    // if (!layoutGraph.containsArcType(arcType)) {
    // layoutArcType = new DefaultLayoutArcType(arcType);
    // layoutGraph.addLayoutArcType(layoutArcType);
    // } else {
    // layoutArcType = layoutGraph.getArcType(arcType);
    // }
    // return layoutArcType;
    // }

    /**
     * Override in tests to get a test delayed executor.
     * 
     * @return a GwtDelayedExecutor which will not work in java unit tests
     *         because it uses Javascript
     */
    protected DelayedExecutor getDelayedExecutor() {
        return new GwtDelayedExecutor();
    }

    protected int getGraphAbsoluteLeft() {
        return matrixWidget.asWidget().getAbsoluteLeft();
    }

    protected int getGraphAbsoluteTop() {
        return matrixWidget.asWidget().getAbsoluteTop();
    }

    // /**
    // *
    // * @return the offset distance from the absolute left of the view to the
    // * left of the visible view. This is non-zero if the view has been
    // * panned.
    // */
    // private double getHorizontalScrollDistance() {
    // return matrixWidget.getViewSize().getWidth() - viewWidth;
    // }

    // @Override
    // public LayoutGraph getLayoutGraph() {
    // return layoutGraph;
    // }

    // @Override
    // public Point getLocation(Node node) {
    // assert node != null;
    // assert nodes.containsKey(node.getId());
    // return matrixRenderer.getRenderedNode(node).getTopLeft().toPointInt();
    // }

    // @Override
    // public Node getNode(String nodeId) {
    // assert nodeId != null;
    // assert nodes.containsKey(nodeId);
    // return nodes.get(nodeId);
    // }

    // /**
    // * Override in tests to get a non-Javascript-based NodeAnimationFactory
    // *
    // * @return a GwtNodeAnimationFactory which will not work in java unit
    // tests
    // * because it uses Javascript
    // */
    // protected NodeAnimationFactory getNodeAnimationFactory() {
    // return new GwtNodeAnimationFactory();
    // }

    // @Override
    // public NodeAnimator getNodeAnimator() {
    // return nodeAnimator;
    // }

    // /**
    // * Retrieves the <code>LayoutNodeType</code> for a node. Creates the node
    // * type if it doesn't already exist.
    // *
    // * @param node
    // * the node whose type is to be determined
    // * @return the type of the node
    // */
    // private DefaultLayoutNodeType getNodeLayoutType(String nodeType) {
    // DefaultLayoutNodeType layoutNodeType = null;
    // if (!layoutGraph.containsNodeType(nodeType)) {
    // layoutNodeType = new DefaultLayoutNodeType(nodeType);
    // layoutGraph.addLayoutNodeType(layoutNodeType);
    // } else {
    // layoutNodeType = layoutGraph.getNodeType(nodeType);
    // }
    // return layoutNodeType;
    // }

    // private int getRandomNodeOffset() {
    // return MathUtils.generateRandomNumber(-DEFAULT_NODE_OFFSET_RANGE,
    // DEFAULT_NODE_OFFSET_RANGE);
    // }

    // /**
    // *
    // * @return the offset distance from the absolute top of the view to the
    // top
    // * of the visible view. This is non-zero if the view has been
    // * panned.
    // */
    // private double getVerticalScrollDistance() {
    // return matrixWidget.getViewSize().getHeight() - viewHeight;
    // }

    // private void initBackgroundListener() {
    // matrixRenderer.setBackgroundEventListener(new DragAndClickHandler() {
    // @Override
    // public void handleClick(ClickEvent clickEvent) {
    // onBackgroundClick(clickEvent.getClickX(),
    // clickEvent.getClickY());
    // }
    //
    // @Override
    // public void handleDrag(DragEvent dragEvent) {
    // panBackground(dragEvent.getDeltaX(), dragEvent.getDeltaY());
    // }
    // });
    // }

    // private void initGraphLayoutManager(ErrorHandler errorHandler) {
    // this.layoutManager = new GraphLayoutExecutionManager(
    // new ForceDirectedLayoutAlgorithm(new CompositeForceCalculator(
    // new BoundsAwareAttractionCalculator(getLayoutGraph()),
    // new BoundsAwareRepulsionCalculator(getLayoutGraph())),
    // 0.9, nodeAnimator, getDelayedExecutor(), errorHandler),
    // getLayoutGraph());
    // }

    private void initViewWideInteractionHandler() {
        // ChooselEventHandler viewWideInteractionListener = new
        // ChooselEventHandler() {
        // @Override
        // public void onEvent(ChooselEvent event) {
        // if (event.getEventType().equals(ChooselEvent.Type.MOUSE_MOVE)) {
        // onViewMouseMove(event, event.getClientX(),
        // event.getClientY());
        // }
        // }
        // };
        // matrixRenderer
        // .setViewWideInteractionHandler(viewWideInteractionListener);
    }

    // public void onArcMouseOver(RenderedArc arc) {
    // // bring connected nodes to front
    // matrixRenderer.bringToForeground(arc.getSource());
    // matrixRenderer.bringToForeground(arc.getTarget());
    // }

    // public void onBackgroundClick(int mouseX, int mouseY) {
    // matrixRenderer.removeAllNodeExpanders();
    // }

    // public void onNodeDrag(ChooselEvent event, String nodeId, int deltaX,
    // int deltaY) {
    // Node node = nodes.get(nodeId);
    // Point startLocation = matrixRenderer.getRenderedNode(node).getTopLeft()
    // .toPointInt();
    // int startX = startLocation.getX();
    // int startY = startLocation.getY();
    // int endX = startX + deltaX;
    // int endY = startY + deltaY;
    //
    // setLocation(node, new Point(endX, endY));
    // eventBus.fireEvent(new NodeDragEvent(node, event, startX, startY, endX,
    // endY));
    // matrixRenderer.removeAllNodeExpanders();
    // }

    // public void onNodeExpanderClick(RenderedNodeExpander expander,
    // String optionId) {
    // Node node = expander.getNode();
    // nodeMenuItemClickHandlersByType.get(node.getType()).get(optionId)
    // .onNodeMenuItemClicked(node);
    // matrixRenderer.removeAllNodeExpanders();
    // }

    // public void onNodeExpanderMouseOut(RenderedNodeExpander expander,
    // String optionId) {
    // // set back to default colour
    // expander.setOptionBackgroundColor(optionId, Colors.WHITE);
    // }

    // public void onNodeExpanderMouseOver(RenderedNodeExpander expander,
    // String optionId) {
    // // give highlighting colour
    // expander.setOptionBackgroundColor(optionId, Colors.BLUE_1);
    // }

    // public void onNodeMouseClick(String nodeId, ChooselEvent chooselEvent,
    // int mouseX, int mouseY) {
    // int x = mouseX - getGraphAbsoluteLeft();
    // int y = mouseY - getGraphAbsoluteTop();
    //
    // eventBus.fireEvent(new NodeMouseClickEvent(getNode(nodeId),
    // chooselEvent, x, y));
    // matrixRenderer.removeAllNodeExpanders();
    // }

    // public void onNodeMouseDoubleClick(String nodeId,
    // ChooselEvent chooselEvent, int mouseX, int mouseY) {
    // int x = getGraphAbsoluteLeft() + mouseX;
    // int y = getGraphAbsoluteTop() + mouseY;
    //
    // eventBus.fireEvent(new NodeMouseDoubleClickEvent(getNode(nodeId),
    // chooselEvent, x, y));
    // }

    // public void onNodeMouseDown(Node node, ChooselEvent event, int clientX,
    // int clientY) {
    // // Why do/did some methods have nodeInteractionManager, others
    // // eventBus.fireEvent()??
    // nodeInteractionManager.onMouseDown(node.getId(), event, clientX,
    // clientY);
    // eventBus.fireEvent(new NodeDragHandleMouseDownEvent(node, event,
    // clientX, clientY));
    // }

    // public void onNodeMouseOut(RenderedNode renderedNode,
    // ChooselEvent chooselEvent, int mouseX, int mouseY) {
    // int x = mouseX - getGraphAbsoluteLeft();
    // int y = mouseY - getGraphAbsoluteTop();
    //
    // eventBus.fireEvent(new NodeMouseOutEvent(renderedNode.getNode(),
    // chooselEvent, x, y));
    // }

    // public void onNodeMouseOver(RenderedNode renderedNode,
    // ChooselEvent chooselEvent, int mouseX, int mouseY) {
    // int x = mouseX - getGraphAbsoluteLeft()
    // - (int) getHorizontalScrollDistance();
    // int y = mouseY - getGraphAbsoluteTop()
    // - (int) getVerticalScrollDistance();
    //
    // eventBus.fireEvent(new NodeMouseOverEvent(renderedNode.getNode(),
    // chooselEvent, x, y));
    // matrixRenderer.bringToForeground(renderedNode);
    // }

    // public void onNodeMouseUp(ChooselEvent event) {
    // nodeInteractionManager.onMouseUp(event);
    // // Whyd idn't this have an eventBus.fireEvent() like commented below?
    // // eventBus.fireEvent(new NodeDragHandleMouseUpEvent(node, event,
    // // startX,
    // // startY, endX, endY));
    // }

    // public void onNodeTabClick(final RenderedNode renderedNode) {
    // matrixRenderer.removeAllNodeExpanders();
    // Map<String, NodeMenuItemClickedHandler> nodeMenuItemClickHandlers =
    // nodeMenuItemClickHandlersByType
    // .get(renderedNode.getType());
    // final RenderedNodeExpander renderNodeExpander = matrixRenderer
    // .renderNodeExpander(renderedNode.getExpanderPopupLocation(),
    // nodeMenuItemClickHandlers.keySet(),
    // renderedNode.getNode());
    //
    // for (final String expanderId : nodeMenuItemClickHandlers.keySet()) {
    // renderNodeExpander.setEventHandlerOnOption(expanderId,
    // new ChooselEventHandler() {
    // @Override
    // public void onEvent(ChooselEvent event) {
    // switch (event.getEventType()) {
    // case MOUSE_OVER:
    // onNodeExpanderMouseOver(renderNodeExpander,
    // expanderId);
    // break;
    //
    // case MOUSE_OUT:
    // onNodeExpanderMouseOut(renderNodeExpander,
    // expanderId);
    // break;
    //
    // case CLICK:
    // onNodeExpanderClick(renderNodeExpander,
    // expanderId);
    // break;
    //
    // default:
    // break;
    // }
    // }
    // });
    // }
    //
    // }

    @Override
    public void onResize(ViewResizeEvent resizeEvent) {
        viewWidth = resizeEvent.getWidth();
        viewHeight = resizeEvent.getHeight();

        // layoutGraph.setWidth(viewWidth);
        // layoutGraph.setHeight(viewHeight);
        //
        // /*
        // * Make sure nodes that go off screen can still be scrolled to
        // */
        // if (viewWidth > layoutGraph.getMaxNodeX()) {
        // matrixRenderer.setMatrixWidth(viewWidth);
        // }
        // if (viewHeight > layoutGraph.getMaxNodeY()) {
        // matrixRenderer.setMatrixHeight(viewHeight);
        // }

        /*
         * Need this in case scrollable content size is not changed but the
         * available
         */
        // TODO Sorting this out for the matrix view...
        // matrixWidget.checkIfScrollbarsNeeded();
    }

    // public void onViewMouseMove(ChooselEvent event, int mouseX, int mouseY) {
    // nodeInteractionManager.onMouseMove(event, mouseX, mouseY);
    // }

    // public void panBackground(int deltaX, int deltaY) {
    // matrixRenderer.removeAllNodeExpanders();
    //
    // BoundsDouble nodeBounds = layoutGraph.getNodeBounds();
    // /*
    // * Only allow panning to the left if it will not push any node off the
    // * left hand side, and only allow panning up if it will not push any
    // * node off the top of the graph. Panning right or down may push nodes
    // * off the screen.
    // */
    // if (nodeBounds.getLeftX() + deltaX > 0) {
    // /*
    // * Only extend background if a node would be pushed off the screen.
    // */
    // double visibleWidth = matrixRenderer.getGraphSize().getWidth();
    // if (deltaX < 0 || layoutGraph.getMaxNodeX() + deltaX > visibleWidth) {
    // double newBackgroundWidth = visibleWidth + deltaX;
    // /*
    // * Don't let background width become less than view width.
    // */
    // if (newBackgroundWidth < viewWidth) {
    // newBackgroundWidth = viewWidth;
    // }
    // matrixRenderer.setGraphWidth((int) newBackgroundWidth);
    // }
    // /*
    // * Still shift the nodes even if the background was not adjusted.
    // * The outer if statement makes sure this wouldn't push them in a
    // * negative direction.
    // */
    // layoutGraph.shiftContentsHorizontally(deltaX);
    // }
    //
    // if (nodeBounds.getTopY() + deltaY > 0) {
    // /*
    // * Only extend background if a node would be pushed off the screen.
    // */
    // double visibleHeight = matrixRenderer.getGraphSize().getHeight();
    // if (deltaY < 0
    // || layoutGraph.getMaxNodeY() + deltaY > visibleHeight) {
    // double newBackgroundHeight = visibleHeight + deltaY;
    // /*
    // * Don't let background height become less than view height.
    // */
    // if (newBackgroundHeight < viewHeight) {
    // newBackgroundHeight = viewHeight;
    // }
    // matrixRenderer.setGraphHeight((int) newBackgroundHeight);
    // }
    // /*
    // * Still shift the nodes even if the background was not adjusted.
    // * The outer if statement makes sure this wouldn't push them in a
    // * negative direction.
    // */
    // layoutGraph.shiftContentsVertically(deltaY);
    // }
    // }

    // @Override
    // public void registerDefaultLayoutAlgorithm(LayoutAlgorithm
    // layoutAlgorithm) {
    // layoutManager.registerDefaultAlgorithm(layoutAlgorithm);
    // }

    // @Override
    // public void removeArc(Arc arc) {
    // assert arc != null;
    // arcs.remove(arc.getId());
    // matrixRenderer.removeArc(arc);
    // layoutGraph.removeIdentifiableLayoutArc(arc.getId());
    // }

    // @Override
    // public void removeNode(Node node) {
    // assert node != null;
    // nodes.remove(node.getId());
    // matrixRenderer.removeNode(node);
    // layoutGraph.removeIdentifiableLayoutNode(node.getId());
    // }

    // @Override
    // public void runLayout() throws LayoutException {
    // layoutManager.runLayout();
    // }

    // @Override
    // public void runLayout(LayoutAlgorithm layoutAlgorithm) {
    // layoutManager.registerAndRunLayoutAlgorithm(layoutAlgorithm);
    // }

    // @Override
    // public void runLayoutOnNodes(final Collection<Node> nodes)
    // throws LayoutException {
    // /*
    // * Get a list of all the nodes which the layout should NOT be run on.
    // */
    // final List<String> unselectedNodeIds = new ArrayList<String>();
    // unselectedNodeIds.addAll(layoutGraph.getAllNodeIds());
    // for (Node node : nodes) {
    // unselectedNodeIds.remove(node.getId());
    // }
    //
    // /*
    // * Anchor the nodes that should not be laid out
    // */
    // for (String nodeId : unselectedNodeIds) {
    // layoutGraph.getIdentifiableLayoutNode(nodeId).setAnchored(true);
    // }
    //
    // /*
    // * When the layout computation finishes, unanchor the nodes that were
    // * anchored
    // */
    // layoutManager
    // .addLayoutComputationFinishedHandler(new
    // LayoutComputationFinishedHandler() {
    // @Override
    // public void onLayoutComputationFinished(
    // LayoutComputationFinishedEvent e) {
    // for (String nodeId : unselectedNodeIds) {
    // layoutGraph.getIdentifiableLayoutNode(nodeId)
    // .setAnchored(false);
    // }
    // }
    // });
    // runLayout();
    // }

    // @Override
    // public void setArcStyle(Arc arc, String styleProperty, String styleValue)
    // {
    // matrixRenderer.setArcStyle(arc, styleProperty, styleValue);
    // }

    // /**
    // * Positions a node at the default position which is near the centre of
    // the
    // * graph but with some small offset.
    // *
    // * @param node
    // * the node to be positioned
    // */
    // private void setDefaultPosition(LayoutNode node) {
    // if (matrixRenderer.isWidgetInitialized()) {
    // PointDouble initialPosition = layoutGraph.getBounds().getCentre();
    // PointDouble offset = new PointDouble(getRandomNodeOffset(),
    // getRandomNodeOffset());
    // PointDouble topLeft = node.getTopLeftForCentreAt(initialPosition
    // .plus(offset));
    // node.setPosition(topLeft);
    // }
    // }

    private void setMatrixContentChangedListener() {
        // TODO I will need something similar to this for the D3 matrix view...
        // layoutGraph
        // .addContentChangedListener(new LayoutGraphContentChangedListener() {
        // @Override
        // public void onContentChanged(
        // LayoutGraphContentChangedEvent event) {
        // runLayout();
        // }
        // });
    }

    // @Override
    // public void setLocation(Node node, Point location) {
    // assert nodes.containsKey(node.getId());
    // layoutGraph.getIdentifiableLayoutNode(node.getId()).setPosition(
    // location.getX(), location.getY());
    // }

    // private void setNodeEventHandlers(final RenderedNode renderedNode) {
    // renderedNode.setBodyEventHandler(new ChooselEventHandler() {
    // @Override
    // public void onEvent(ChooselEvent event) {
    // int clientX = event.getClientX();
    // int clientY = event.getClientY();
    //
    // switch (event.getEventType()) {
    // case MOUSE_OVER:
    // onNodeMouseOver(renderedNode, event, clientX, clientY);
    // break;
    //
    // case MOUSE_OUT:
    // onNodeMouseOut(renderedNode, event, clientX, clientY);
    // break;
    //
    // case MOUSE_UP:
    // onNodeMouseUp(event);
    // break;
    //
    // case MOUSE_DOWN:
    // onNodeMouseDown(renderedNode.getNode(), event, clientX,
    // clientY);
    // break;
    //
    // default:
    // break;
    // }
    // }
    // });
    //
    // renderedNode.setExpansionEventHandler(new ChooselEventHandler() {
    // @Override
    // public void onEvent(ChooselEvent event) {
    // if (event.getEventType().equals(ChooselEvent.Type.CLICK)) {
    // onNodeTabClick(renderedNode);
    // }
    // }
    // });
    // }

    // @Override
    // public void setNodeStyle(Node node, String styleProperty, String
    // styleValue) {
    // matrixRenderer.setNodeStyle(node, styleProperty, styleValue);
    // }

}
