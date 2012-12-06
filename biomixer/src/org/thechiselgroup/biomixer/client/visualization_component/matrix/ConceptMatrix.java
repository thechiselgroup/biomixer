/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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

import org.thechiselgroup.biomixer.client.DataTypeValidator;
import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeInt;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.UnionResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.biomixer.client.core.ui.SidePanelSection;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.NoSuchAdapterException;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.behaviors.CompositeVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.AbstractViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplayCallback;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.RequiresAutomaticResourceSet;
import org.thechiselgroup.biomixer.client.graph.ConceptMappingNeighbourhoodLoader;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutSupport;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphNodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.GraphDisplayController;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer.VisualItemBehaviorFactory;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

// TODO separate out ncbo specific stuff and service calls
// TODO register listener for double click on node --> change expansion state
public class ConceptMatrix extends AbstractViewContentDisplay implements
        RequiresAutomaticResourceSet
// , GraphLayoutSupport,
// GraphLayoutCallback
{

    private final class MatrixExpansionCallback implements
            GraphNodeExpansionCallback {
        @Override
        public void addAutomaticResource(Resource resource) {
            ConceptMatrix.this.addAutomaticResource(resource);
        }

        @Override
        public boolean containsResourceWithUri(String resourceUri) {
            return ConceptMatrix.this.containsResourceWithUri(resourceUri);
        }

        @Override
        public String getCategory(Resource resource) {
            return ConceptMatrix.this.getCategory(resource);
        }

        @Override
        public GraphDisplayController getDisplay() {
            // return ConceptMatrix.this.getDisplay();
            return null;
        }

        @Override
        public Resource getResourceByUri(String value) {
            return ConceptMatrix.this.getResourceByUri(value);
        }

        @Override
        public ResourceManager getResourceManager() {
            return ConceptMatrix.this.getResourceManager();
        }

        @Override
        public LightweightCollection<VisualItem> getVisualItems(
                Iterable<Resource> resources) {
            return ConceptMatrix.this.getVisualItems(resources);
        }

        @Override
        public boolean isInitialized() {
            return ConceptMatrix.this.isInitialized();
        }

        @Override
        public boolean isRestoring() {
            return ConceptMatrix.this.isRestoring();
        }

        @Override
        public void updateArcsForResources(Iterable<Resource> resources) {
            // OntologyMatrix.this.updateArcsForResources(resources);
            // TODO This needs to call the method that triggers updates in D3,
            // after it has added the new mappings
            matrixDisplay.updateView();
        }

        @Override
        public void updateArcsForVisuaItems(
                LightweightCollection<VisualItem> visualItems) {
            // OntologyMatrix.this.updateArcsForVisuaItems(visualItems);
            // TODO This needs to call the method that triggers updates in D3,
            // after it has added the new mappings
            matrixDisplay.updateView();
        }
    }

    public static final Slot BORDER_COLOR_SLOT = new Slot(
            "ontologyMatrixBorderColor", "Ontology Border Color",
            DataType.COLOR);

    public static final Slot COLOR_SLOT = new Slot(
            "ontologyMatrixBackgroundColor", "Ontology Color", DataType.COLOR);

    public static final Slot LABEL_SLOT = new Slot("nodeLabel",
            "Ontology Label", DataType.TEXT);

    private static final Slot[] SLOTS = new Slot[] { BORDER_COLOR_SLOT,
            COLOR_SLOT, LABEL_SLOT };

    private static final String MEMENTO_ONTOLOGY_ITEM_CONTAINER = "ontologyContainer";

    // private static final String MEMENTO_NODE_LOCATIONS_CHILD =
    // "nodeLocations";

    // private static final String MEMENTO_X = "x";
    //
    // private static final String MEMENTO_Y = "y";

    // public static class DefaultDisplay extends GraphDisplayController {
    //
    // static final int defaultHeight = 400;
    //
    // static final int defaultWidth = 300;
    //
    // // static final GraphRendererConceptGraphFactory factory = new
    // // GraphRendererConceptGraphFactory();
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
    //
    // }

    // private class GraphEventHandler implements NodeMouseOverHandler,
    // NodeMouseOutHandler, NodeMouseClickHandler, MouseMoveHandler,
    // NodeDragHandler, NodeDragHandleMouseDownHandler,
    // NodeDragHandleMouseMoveHandler {
    //
    // /**
    // * Node that the mouse is currently over. Set by mouse out and mouse
    // * over. Not over a node if null.
    // */
    // private Node currentNode = null;
    //
    // @Override
    // public void onDrag(NodeDragEvent event) {
    // commandManager.execute(new MoveNodeCommand(graphDisplay, event
    // .getNode(),
    // new Point(event.getStartX(), event.getStartY()), new Point(
    // event.getEndX(), event.getEndY())));
    //
    // // Leverage the registered VisualItem framework with Popups. This
    // // helps hide the popup when we drag the node, so it doesn't
    // // interfere.
    // if (event.getNode() != null
    // && (event.getEndX() != event.getStartX())
    // || (event.getEndY() != event.getStartY())) {
    // // The DRAG_START event is used in the PopupVisualItemBehavior
    // // to hide the popup...
    // getVisualItem(event.getNode()).reportInteraction(
    // new VisualItemInteraction(event.getChooselEvent()
    // .getBrowserEvent(), Type.DRAG_START, event
    // .getEndX(), event.getEndY()));
    //
    // // ... but I need to pass the browser native event in order to
    // // cancel the text highlighting (which might happen through the
    // // createDefaultDragVisualItemBehavior() thing.
    // getVisualItem(event.getNode()).reportInteraction(
    // new VisualItemInteraction(event.getChooselEvent()
    // .getBrowserEvent()));
    // }
    //
    // }
    //
    // @Override
    // public void onMouseClick(NodeMouseClickEvent event) {
    // reportInteraction(Type.CLICK, event);
    // }
    //
    // @Override
    // public void onMouseDown(NodeDragHandleMouseDownEvent event) {
    // reportInteraction(Type.MOUSE_DOWN, event);
    // }
    //
    // @Override
    // public void onMouseMove(MouseMoveEvent event) {
    // // TODO This doesn't get called currently. Is the code valuable for
    // // later?
    // // May not get called since some funny redispatching involving the
    // // DRAG_START event type occurs.
    // if (currentNode != null) {
    // VisualItemInteraction interaction = new VisualItemInteraction(
    // event.getNativeEvent());
    // getVisualItem(currentNode).reportInteraction(interaction);
    // }
    // }
    //
    // @Override
    // public void onMouseMove(NodeDragHandleMouseMoveEvent event) {
    // // TODO This doesn't get called currently. Is the code valuable for
    // // later?
    // reportInteraction(Type.MOUSE_MOVE, event);
    // }
    //
    // @Override
    // public void onMouseOut(NodeMouseOutEvent event) {
    // currentNode = null;
    // reportInteraction(Type.MOUSE_OUT, event);
    // }
    //
    // @Override
    // public void onMouseOver(NodeMouseOverEvent event) {
    // currentNode = event.getNode();
    // reportInteraction(Type.MOUSE_OVER, event);
    // }
    //
    // private void reportInteraction(Type eventType, NodeEvent<?> event) {
    // getVisualItem(event).reportInteraction(
    // new VisualItemInteraction(event.getChooselEvent()
    // .getBrowserEvent()));
    // }
    //
    // }

    // public class GraphLayoutAction implements ViewContentDisplayAction {
    //
    // private final LayoutAlgorithm layoutAlgorithm;
    //
    // private final String layoutLabel;
    //
    // public GraphLayoutAction(String layoutLabel,
    // LayoutAlgorithm layoutAlgorithm) {
    // this.layoutLabel = layoutLabel;
    // this.layoutAlgorithm = layoutAlgorithm;
    // }
    //
    // @Override
    // public void execute() {
    // commandManager.execute(new GraphLayoutCommand(graphDisplay,
    // layoutAlgorithm, layoutLabel, getAllNodes()));
    // }
    //
    // @Override
    // public String getLabel() {
    // return layoutLabel;
    // }
    // }

    // public static final Slot NODE_BORDER_COLOR = new Slot("nodeBorderColor",
    // "Node Border Color", DataType.COLOR);
    //
    // public static final Slot NODE_BACKGROUND_COLOR = new Slot(
    // "nodeBackgroundColor", "Node Color", DataType.COLOR);
    //
    // public static final Slot NODE_LABEL_SLOT = new Slot("nodeLabel",
    // "Node Label", DataType.TEXT);
    //
    // private static final String MEMENTO_ARC_ITEM_CONTAINERS_CHILD =
    // "arcItemContainers";
    //
    // private static final String MEMENTO_NODE_LOCATIONS_CHILD =
    // "nodeLocations";
    //
    // private static final String MEMENTO_X = "x";
    //
    // private static final String MEMENTO_Y = "y";
    //
    // // TODO move
    // public static String getArcId(String arcType, String sourceId,
    // String targetId) {
    // // FIXME this needs escaping of special characters to work properly
    // return arcType + ":" + sourceId + "_" + targetId;
    // }
    //
    // private final ArcTypeProvider arcStyleProvider;

    private final CommandManager commandManager;

    // // advanced node class: (incoming, outgoing, expanded: state machine)

    private final NeoD3ConceptMatrixDisplayController matrixDisplay;

    ConceptMappingNeighbourhoodLoader mappingLoader;

    private final ResourceCategorizer resourceCategorizer;

    private final ResourceManager resourceManager;

    private final UnionResourceSet conceptResources = new UnionResourceSet(
            new DefaultResourceSet());

    // private final Map<String, ArcItemContainer> arcItemContainersByArcTypeID
    // = CollectionFactory
    // .createStringMap();

    private ResourceSet automaticResources;

    /*
     * TODO The callback is meant to check whether the graph is initialized (and
     * not disposed) when methods are called (to prevent errors in asynchronous
     * callbacks that return after the graph has been disposed or before it has
     * been initialized).
     */
    private final GraphNodeExpansionCallback expansionCallback = new MatrixExpansionCallback();

    private ErrorHandler errorHandler;

    @Inject
    public ConceptMatrix(
            NeoD3ConceptMatrixDisplayController matrixDisplayController,
            CommandManager commandManager, ResourceManager resourceManager,
            ResourceCategorizer resourceCategorizer,
            // ArcTypeProvider arcStyleProvider,
            ConceptMappingNeighbourhoodLoader mappingLoader,
            ErrorHandler errorHandler, DataTypeValidator dataTypeValidator) {
        super(dataTypeValidator);

        assert matrixDisplayController != null;
        assert commandManager != null;
        assert resourceManager != null;
        assert resourceCategorizer != null;
        // assert arcStyleProvider != null;
        assert mappingLoader != null;
        assert errorHandler != null;

        // this.arcStyleProvider = arcStyleProvider;
        this.resourceCategorizer = resourceCategorizer;
        matrixDisplay = matrixDisplayController;
        // didn't want to change GraphDisplay's interface yet
        if (matrixDisplay instanceof NeoD3ConceptMatrixDisplayController) {
            addResizeListener(matrixDisplay);
        }
        this.commandManager = commandManager;
        this.resourceManager = resourceManager;
        this.mappingLoader = mappingLoader;

        /*
         * we init the arc type containers early so they are available for UI
         * customization in Choosel applications.
         */
        // initArcTypeContainers();
        this.errorHandler = errorHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T adaptTo(Class<T> clazz) throws NoSuchAdapterException {
        if (GraphLayoutSupport.class.equals(clazz)) {
            return (T) this;
        }

        return super.adaptTo(clazz);
    }

    private void addAutomaticResource(Resource resource) {
        automaticResources.add(resource);
    }

    private boolean containsResourceWithUri(String resourceUri) {
        return conceptResources.containsResourceWithUri(resourceUri);
    }

    // private NodeItem createGraphNodeItem(VisualItem visualItem) {
    // // TODO get from group id
    // String type = getCategory(visualItem.getResources().getFirstElement());
    // NodeItem nodeItem = new NodeItem(visualItem, type, matrixDisplay);
    //
    // /*
    // * NOTE: When the node is added, a LayoutGraphContentChangedEvent will
    // * be fired (see DefaultLayoutGraph), causing the current layout
    // * algorithm to be run.
    // */
    // matrixDisplay.addNode(nodeItem.getNode());
    // this.addNodeGraphItem(nodeItem);
    //
    // // TODO re-enable
    // // TODO remove once new drag and drop mechanism works...
    // matrixDisplay
    // .setNodeStyle(nodeItem.getNode(), "showDragImage", "false");
    //
    // matrixDisplay.setNodeStyle(nodeItem.getNode(), "showArrow", registry
    // .getNodeMenuEntries(type).isEmpty() ? "false" : "true");
    //
    // nodeResources.addResourceSet(visualItem.getResources());
    // visualItem.setDisplayObject(nodeItem);
    //
    // /*
    // * NOTE: all node configuration should be done when calling the
    // * automatic expanders, since they rely on returning the correct graph
    // * contents etc.
    // *
    // * NOTE: we do not execute the expanders if we are restoring the graph
    // */
    // registry.getAutomaticExpander(type).expand(visualItem,
    // expansionCallback);
    //
    // return nodeItem;
    // }

    // TODO Not sure about this method...will I actually use VisualItems for
    // Ontologies? Yes? Work from the bottom up until I get back here before
    // pushing further on this.
    private NodeItem createConceptItem(VisualItem visualItem) {
        // TODO get from group id
        String type = getCategory(visualItem.getResources().getFirstElement());
        // NodeItem nodeItem = new NodeItem(visualItem, type, matrixDisplay);

        /*
         * NOTE: When the node is added, a LayoutGraphContentChangedEvent will
         * be fired (see DefaultLayoutGraph), causing the current layout
         * algorithm to be run.
         */
        // matrixDisplay.addOntology(nodeItem.getNode());
        // this.addNodeGraphItem(nodeItem);

        // TODO re-enable
        // TODO remove once new drag and drop mechanism works...
        // matrixDisplay
        // .setNodeStyle(nodeItem.getNode(), "showDragImage", "false");
        //
        // matrixDisplay.setNodeStyle(nodeItem.getNode(), "showArrow", registry
        // .getNodeMenuEntries(type).isEmpty() ? "false" : "true");

        // nodeResources.addResourceSet(visualItem.getResources());
        // visualItem.setDisplayObject(nodeItem);

        /*
         * NOTE: all node configuration should be done when calling the
         * automatic expanders, since they rely on returning the correct graph
         * contents etc.
         * 
         * NOTE: we do not execute the expanders if we are restoring the graph
         */
        // registry.getAutomaticExpander(type).expand(visualItem,
        // expansionCallback);
        //
        // return nodeItem;
        return null;
    }

    // private final Set<NodeItem> nodeItems = new HashSet<NodeItem>();

    // /**
    // * Needed additional tracking, so that we could access visual items of all
    // * Nodes in the graph.
    // *
    // * @param graphItem
    // */
    // private void addNodeGraphItem(NodeItem graphItem) {
    // this.nodeItems.add(graphItem);
    // }

    // /**
    // * Needed additional tracking, so that we could access visual items of all
    // * Nodes in the graph.
    // *
    // * @param graphItem
    // */
    // private void removeNodeGraphItem(NodeItem graphItem) {
    // this.nodeItems.remove(graphItem);
    // }

    // TODO encapsulate in display, use dependency injection
    @Override
    protected Widget createWidget() {
        return matrixDisplay.asWidget();
    }

    // // TODO better caching?
    // // default visibility for test case use
    // List<Node> getAllNodes() {
    // List<Node> result = new ArrayList<Node>();
    // for (VisualItem visualItem : getVisualItems()) {
    // result.add(getNode(visualItem));
    // }
    // return result;
    // }

    public ResourceSet getAllResources() {
        return conceptResources;
    }

    // public ArcItemContainer getArcItemContainer(String arcTypeID) {
    // assert arcTypeID != null;
    // assert arcItemContainersByArcTypeID.containsKey(arcTypeID);
    //
    // return arcItemContainersByArcTypeID.get(arcTypeID);
    // }

    // public Iterable<ArcItemContainer> getArcItemContainers() {
    // return arcItemContainersByArcTypeID.values();
    // }

    // // TODO remove if not used
    // private ArcItem[] getArcItems() {
    // Iterable<ArcItemContainer> arcItemContainers = getArcItemContainers();
    // int size = 0;
    // for (ArcItemContainer arcItemContainer : arcItemContainers) {
    // size += arcItemContainer.getArcItems().size();
    // }
    // ArcItem[] arcs = new ArcItem[size];
    // int i = 0;
    // for (ArcItemContainer arcItemContainer : arcItemContainers) {
    // for (ArcItem arcItem : arcItemContainer.getArcItems()) {
    // arcs[i++] = arcItem;
    // }
    // }
    // return arcs;
    // }

    private String getCategory(Resource resource) {
        return resourceCategorizer.getCategory(resource);
    }

    private NeoD3ConceptMatrixDisplayController getDisplay() {
        return matrixDisplay;
    }

    // @Override
    public SizeInt getDisplayArea() {
        Widget displayWidget = matrixDisplay.asWidget();
        int height = displayWidget.getOffsetHeight();
        int width = displayWidget.getOffsetWidth();
        return new DefaultSizeInt(width, height);
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    // @Override
    // public Point getLocation(NodeItem nodeItem) {
    // return matrixDisplay.getLocation(nodeItem.getNode());
    // }

    @Override
    public String getName() {
        return this.matrixDisplay.getGraphViewName();
    }

    // private Node getNode(VisualItem visualItem) {
    // return visualItem.<NodeItem> getDisplayObject().getNode();
    // }

    // @Override
    // public NodeAnimator getNodeAnimator() {
    // return matrixDisplay.getNodeAnimator();
    // }

    // // TODO remove if not used
    // private NodeItem[] getNodeItems() {
    // LightweightCollection<VisualItem> visualItems = getVisualItems();
    // NodeItem[] nodeItems = new NodeItem[visualItems.size()];
    // int i = 0;
    // for (VisualItem visualItem : visualItems) {
    // nodeItems[i++] = visualItem.getDisplayObject();
    // }
    // return nodeItems;
    // }

    public Resource getResourceByUri(String value) {
        return conceptResources.getByUri(value);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    @Override
    public SidePanelSection[] getSidePanelSections() {
        // TODO Not sure if there is anything we need to put here

        // List<ViewContentDisplayAction> actions = new
        // ArrayList<ViewContentDisplayAction>();
        // // TODO cleanup
        //
        // NodeAnimator nodeAnimator = getNodeAnimator();
        // actions.add(new GraphLayoutAction(GraphLayouts.CIRCLE_LAYOUT,
        // new CircleLayoutAlgorithm(errorHandler, nodeAnimator)));
        // actions.add(new
        // GraphLayoutAction(GraphLayouts.HORIZONTAL_TREE_LAYOUT,
        // new HorizontalTreeLayoutAlgorithm(true, errorHandler,
        // nodeAnimator)));
        // actions.add(new GraphLayoutAction(GraphLayouts.VERTICAL_TREE_LAYOUT,
        // new VerticalTreeLayoutAlgorithm(true, errorHandler,
        // nodeAnimator)));
        // actions.add(new GraphLayoutAction(GraphLayouts.FORCE_DIRECTED_LAYOUT,
        // new ForceDirectedLayoutAlgorithm(new CompositeForceCalculator(
        // new BoundsAwareAttractionCalculator(matrixDisplay
        // .getLayoutGraph()),
        // new BoundsAwareRepulsionCalculator(matrixDisplay
        // .getLayoutGraph())), 0.9, nodeAnimator,
        // new GwtDelayedExecutor(), errorHandler)));
        //
        // VerticalPanel layoutPanel = new VerticalPanel();
        // for (final ViewContentDisplayAction action : actions) {
        // Button w = new Button(action.getLabel());
        // w.addClickHandler(new ClickHandler() {
        // @Override
        // public void onClick(ClickEvent event) {
        // action.execute();
        // }
        // });
        // layoutPanel.add(w);
        // }

        return new SidePanelSection[] {
        // new SidePanelSection("Layouts",layoutPanel),
        };
    }

    @Override
    public Slot[] getSlots() {
        // TODO This is still relevant despite using D3, right?
        return SLOTS;
    }

    // public VisualItem getVisualItem(Node node) {
    // return getVisualItem(node.getId());
    // }

    // private VisualItem getVisualItem(NodeEvent<?> event) {
    // return getVisualItem(event.getNode());
    // }

    @Override
    public void init(VisualItemContainer container,
            ViewContentDisplayCallback callback) {

        super.init(container, callback);

        initStateChangeHandlers();
    }

    // private void initArcTypeContainers() {
    // for (ArcType arcType : arcStyleProvider.getArcTypes()) {
    // arcItemContainersByArcTypeID.put(arcType.getArcTypeID(),
    // new ArcItemContainer(arcType, matrixDisplay, this));
    // }
    // }

    // private void initNodeMenuItems() {
    // for (Entry<String, List<NodeMenuEntry>> entry : registry
    // .getNodeMenuEntriesByCategory()) {
    //
    // String category = entry.getKey();
    // for (NodeMenuEntry nodeMenuEntry : entry.getValue()) {
    // registerNodeMenuItem(category, nodeMenuEntry.getLabel(),
    // nodeMenuEntry.getExpander());
    // }
    // }
    // }

    private void initStateChangeHandlers() {
        // GraphEventHandler handler = new GraphEventHandler();
        // matrixDisplay.addEventHandler(NodeDragHandleMouseDownEvent.TYPE,
        // handler);
        // matrixDisplay.addEventHandler(NodeMouseOverEvent.TYPE, handler);
        // matrixDisplay.addEventHandler(NodeMouseOutEvent.TYPE, handler);
        // matrixDisplay.addEventHandler(NodeMouseClickEvent.TYPE, handler);
        // matrixDisplay.addEventHandler(NodeDragEvent.TYPE, handler);
        // matrixDisplay.addEventHandler(MouseMoveEvent.getType(), handler);
        //
        // initNodeMenuItems();
    }

    @Override
    public boolean isAdaptableTo(Class<?> clazz) {
        if (GraphLayoutSupport.class.equals(clazz)) {
            return true;
        }

        return super.isAdaptableTo(clazz);
    }

    // @Override
    // public void registerDefaultLayout(LayoutAlgorithm layoutAlgorithm) {
    // matrixDisplay.registerDefaultLayoutAlgorithm(layoutAlgorithm);
    // }

    // private void registerNodeMenuItem(String category, String menuLabel,
    // final GraphNodeExpander nodeExpander) {
    //
    // matrixDisplay.addNodeMenuItemHandler(menuLabel,
    // new NodeMenuItemClickedHandler() {
    // @Override
    // public void onNodeMenuItemClicked(Node node) {
    // nodeExpander.expand(getVisualItem(node),
    // expansionCallback);
    // }
    // }, category);
    // }

    // private void removeGraphNode(VisualItem visualItem) {
    // assert visualItem != null;
    //
    // nodeResources.removeResourceSet(visualItem.getResources());
    // for (ArcItemContainer arcItemContainer : arcItemContainersByArcTypeID
    // .values()) {
    // arcItemContainer.removeVisualItem(visualItem);
    // }
    // matrixDisplay.removeNode(getNode(visualItem));
    // this.removeNodeGraphItem((NodeItem) (visualItem.getDisplayObject()));
    // }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        // restoreArcItemContainers(restorationService, accessor,
        // state.getChild(MEMENTO_ARC_ITEM_CONTAINERS_CHILD));
        // restoreNodeLocations(state.getChild(MEMENTO_NODE_LOCATIONS_CHILD));
        // TODO I really haven't worked with memento and restore stuff yet, so I
        // have no idea what this should be like.
        restoreOntologyContainer(restorationService, accessor,
                state.getChild(MEMENTO_ONTOLOGY_ITEM_CONTAINER));
    }

    private void restoreOntologyContainer(
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor, Memento child) {
        // for (Entry<String, Memento> entry : child.getChildren().entrySet()) {
        // arcItemContainersByArcTypeID.get(entry.getKey()).restore(
        // entry.getValue(), restorationService, accessor);
        // }
    }

    // private void restoreNodeLocations(Memento state) {
    // for (VisualItem visualItem : getVisualItems()) {
    // NodeItem item = visualItem.getDisplayObject();
    // Memento nodeMemento = state.getChild(visualItem.getId());
    // Point location = new Point(
    // (Integer) nodeMemento.getValue(MEMENTO_X),
    // (Integer) nodeMemento.getValue(MEMENTO_Y));
    //
    // setLocation(item, location);
    // }
    // }

    // @Override
    // public void runLayout() {
    // matrixDisplay.runLayout();
    // }

    // @Override
    // public void runLayout(LayoutAlgorithm layoutAlgorithm) {
    // matrixDisplay.runLayout(layoutAlgorithm);
    // }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        Memento result = new Memento();

        // result.addChild(MEMENTO_NODE_LOCATIONS_CHILD, saveNodeLocations());
        // result.addChild(MEMENTO_ARC_ITEM_CONTAINERS_CHILD,
        // saveArcTypeContainers(resourceSetCollector));

        result.addChild(MEMENTO_ONTOLOGY_ITEM_CONTAINER,
                saveOntologyContainer(resourceSetCollector));

        return result;
    }

    private Memento saveOntologyContainer(
            ResourceSetCollector resourceSetCollector) {

        // Memento memento = new Memento();
        // for (Entry<String, ArcItemContainer> entry :
        // arcItemContainersByArcTypeID
        // .entrySet()) {
        // memento.addChild(entry.getKey(),
        // entry.getValue().save(resourceSetCollector));
        // }
        // return memento;
        return null;
    }

    // private Memento saveNodeLocations() {
    // Memento state = new Memento();
    //
    // for (VisualItem visualItem : getVisualItems()) {
    // NodeItem nodeItem = visualItem.getDisplayObject();
    //
    // Point location = matrixDisplay.getLocation(nodeItem.getNode());
    //
    // Memento nodeMemento = new Memento();
    // nodeMemento.setValue(MEMENTO_X, location.getX());
    // nodeMemento.setValue(MEMENTO_Y, location.getY());
    //
    // state.addChild(visualItem.getId(), nodeMemento);
    // }
    // return state;
    // }

    // /**
    // * If the arc type becomes invisible, all arcs of this arcType from the
    // view
    // * and arcs of this arc type are not shown any more. If the arc types
    // * becomes visible, all arcs of this type are added.
    // */
    // // TODO expose arc type configurations and use listener mechanism
    // public void setArcTypeVisible(String arcTypeId, boolean visible) {
    // assert arcTypeId != null;
    // assert arcItemContainersByArcTypeID.containsKey(arcTypeId);
    //
    // arcItemContainersByArcTypeID.get(arcTypeId).setVisible(visible);
    // }

    @Override
    public void setAutomaticResources(ResourceSet automaticResources) {
        this.automaticResources = automaticResources;
    }

    // @Override
    // public void setLocation(NodeItem node, Point location) {
    // matrixDisplay.setLocation(node.getNode(), location);
    // }

    private void updateNode(VisualItem visualItem) {
        visualItem.<NodeItem> getDisplayObject().updateNode();
    }

    private void removeGraphNode(VisualItem visualItem) {
        assert visualItem != null;

        conceptResources.removeResourceSet(visualItem.getResources());
        // for (ArcItemContainer arcItemContainer : arcItemContainersByArcTypeID
        // .values()) {
        // arcItemContainer.removeVisualItem(visualItem);
        // }
        matrixDisplay
                .removeConcept(visualItem.getResources().getFirstElement());
        // this.removeNodeGraphItem((NodeItem) (visualItem.getDisplayObject()));
    }

    /**
     * 
     * 
     * @param visualItem
     * @return
     */
    private void createMatrixConceptItem(VisualItem visualItem) {
        // private NodeItem createGraphNodeItem(VisualItem visualItem) {
        // TODO Rename to register concept matrix item (unless we make a
        // MatrixItem class)

        // TODO get from group id
        String type = getCategory(visualItem.getResources().getFirstElement());

        // TODO Do I need something like a NodeItem? I might in terms of
        // mimicking the Graph design, but in terms of actual design here, I am
        // not so sure...
        // NodeItem nodeItem = new NodeItem(visualItem, type, matrixDisplay);

        // matrixDisplay.addNode(nodeItem.getNode());
        // this.addNodeGraphItem(nodeItem);

        matrixDisplay.addConcept(visualItem.getResources().getFirstElement());
        // this.addNodeGraphItem(nodeItem);

        // // TODO re-enable
        // // TODO remove once new drag and drop mechanism works...
        // matrixDisplay.setNodeStyle(nodeItem.getNode(), "showDragImage",
        // "false");
        //
        // matrixDisplay.setNodeStyle(nodeItem.getNode(), "showArrow", registry
        // .getNodeMenuEntries(type).isEmpty() ? "false" : "true");

        conceptResources.addResourceSet(visualItem.getResources());
        // TODO This might be a real problem , *not* setting the display object.
        // But I also notice that the different views set the display object to
        // different things. That seems really odd!
        // visualItem.setDisplayObject(nodeItem);

        /*
         * NOTE: all node configuration should be done when calling the
         * automatic expanders, since they rely on returning the correct graph
         * contents etc.
         * 
         * NOTE: we do not execute the expanders if we are restoring the graph
         */
        // registry.getAutomaticExpander(type).expand(visualItem,
        // expansionCallback);
        mappingLoader.expand(visualItem, expansionCallback);

        // return nodeItem;
    }

    // TODO Do I want a specialized class for these, when D3 does all the work?
    // I probably need to link colors up for ontologies across views, so likely
    // yes. And I will need to force D3 to make use of the colors that are
    // thusly determined.
    // private final ArcItemContainer arcItemContainer = new ArcItemContainer(
    // arcType, graphDisplay, context);

    public void updateArcsForVisuaItems(
            LightweightCollection<VisualItem> visualItems) {
        // TODO Get the mapping arcs for all the items, since we use the matrix
        // view for visualizing mappings
        assert visualItems != null;
        // for (ArcItemContainer container :
        // arcItemContainersByArcTypeID.values()) {
        // container.update(visualItems);
        // }
    }

    @Override
    public void update(Delta<VisualItem> delta,
            LightweightCollection<Slot> updatedSlots) {

        for (VisualItem addedItem : delta.getAddedElements()) {
            // TODO VisualItems may not be relevant to the D3 based view! This
            // is my first hint that they aren't.

            // TODO For starters, add the ontology (in another method) and have
            // this one add the relevant visual items...

            createMatrixConceptItem(addedItem);
            updateNode(addedItem);
            matrixDisplay
                    .addConcept(addedItem.getResources().getFirstElement());
        }

        updateArcsForVisuaItems(delta.getAddedElements());

        for (VisualItem updatedItem : delta.getUpdatedElements()) {
            updateNode(updatedItem);
        }

        for (VisualItem visualItem : delta.getRemovedElements()) {
            removeGraphNode(visualItem);
            matrixDisplay.removeConcept(visualItem.getResources()
                    .getFirstElement());
        }

        if (!updatedSlots.isEmpty()) {
            for (VisualItem visualItem : getVisualItems()) {
                updateNode(visualItem);
            }
        }

        /*
         * Call any batch expanders (e.g. I needed to get mapping arcs from a
         * service that could accept a full network as an argument)
         * 
         * See other expanders elsewhere like:
         * registry.getAutomaticExpander(type)
         * 
         * Gather types, and run the bulk expanders. With this design, bulk
         * expanders cannot work with multiple types, so they are bulk with
         * regard to the type they are associated with. This was originally
         * created to support ontology node mappings to eachother, since parsing
         * those arcs in the original ontology parsing would result in too many
         * arcs, and doing non-bulk was inefficient in terms of REST calls.
         */
        // Map<String, LightweightList<VisualItem>> types = new HashMap<String,
        // LightweightList<VisualItem>>();
        // for (NodeItem nodeItem : nodeItems) {
        // // Perhaps this could be more efficient if we stored the nodeItems
        // // keyed by type, but it's probably preferable to have this computed
        // // than to have the storage overhead.
        // VisualItem visualItem = nodeItem.getVisualItem();
        // String type = getCategory(visualItem.getResources()
        // .getFirstElement());
        // if (null == types.get(type)) {
        // types.put(type, LightweightCollections.<VisualItem> toList());
        // }
        // types.get(type).add(visualItem);
        // }
        // if (!delta.getAddedElements().isEmpty()
        // || !delta.getRemovedElements().isEmpty()) {
        // for (String type : types.keySet()) {
        // registry.getAutomaticBulkExpander(type).expand(types.get(type),
        // expansionCallback);
        // }
        // }

        // TODO I call update on the matrix view here? Is there where I pass the
        // data (to be internally converted to JSON for D3 usage)?
        matrixDisplay.updateView();

    }

    // public void updateArcsForResources(Iterable<Resource> resources) {
    // updateArcsForVisuaItems(getVisualItems(resources));
    // }

    // public void updateArcsForVisuaItems(
    // LightweightCollection<VisualItem> visualItems) {
    // assert visualItems != null;
    // for (ArcItemContainer container : arcItemContainersByArcTypeID.values())
    // {
    // container.update(visualItems);
    // }
    // }

    // private void updateNode(VisualItem visualItem) {
    // visualItem.<NodeItem> getDisplayObject().updateNode();
    // }

    @Override
    public CompositeVisualItemBehavior createVisualItemBehaviors(
            VisualItemBehaviorFactory behaviorFactory) {

        CompositeVisualItemBehavior composite = behaviorFactory
                .createEmptyCompositeVisualItemBehavior();

        // This both triggers shading, disables text drag, and gets rid of
        // popups. Extend/replace.
        // It does so because it triggers a DRAG_START event, which I need to do
        // in a different way.
        // composite.add(behaviorFactory.createDefaultDragVisualItemBehavior());

        // It seems like the presence of the defaultHighlighting one affects the
        // calling of the defaultPopupWithHighlighting...
        // Why would they interact within the...
        // Seems like the DRAG_START event is not triggered without the drag
        // controller! That's the issue! A secret dependency!!!
        // I can change the popup manager to detect drags in another way
        // perhaps,
        // rather than having this secret dependency. Or have the DRAG_START
        // event fired off in a different place (as well)
        // In any case, I have the popup-hide working and text select disabled.
        composite.add(behaviorFactory
                .createDefaultPopupWithHighlightingVisualItemBehavior());

        return composite;
    }
}