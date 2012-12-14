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

import java.util.HashSet;

import org.thechiselgroup.biomixer.client.DataTypeValidator;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.UnionResourceSet;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.behaviors.CompositeVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.AbstractViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.RequiresAutomaticResourceSet;
import org.thechiselgroup.biomixer.client.graph.ConceptMappingNeighbourhoodLoader;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer.VisualItemBehaviorFactory;

import com.google.gwt.user.client.ui.Widget;

public class NeoD3Matrix extends AbstractViewContentDisplay implements
        ViewWithResourceManager, RequiresAutomaticResourceSet {

    public final static Slot COLOR = new Slot("color", "Color", DataType.COLOR);

    public final static Slot BORDER_COLOR = new Slot("borderColor",
            "Border Color", DataType.COLOR);

    public static final Slot LABEL_SLOT = new Slot("nodeLabel", "Label",
            DataType.TEXT);

    private static final Slot[] SLOTS = new Slot[] { BORDER_COLOR, COLOR,
            LABEL_SLOT };

    private static final String MEMENTO_DATE = "date";

    private static final String MEMENTO_ZOOM_PREFIX = "zoom-band-";

    private NeoD3MatrixWidget matrixWidget;

    // Have this because VisualItems go well with the matrix update method
    private HashSet<VisualItem> conceptVisualItems = new HashSet<VisualItem>();

    // TODO which container? one for VisualItem or one for Resource?
    // The UnionResourceSet is used in graph, and might be better here...
    private final UnionResourceSet conceptResources = new UnionResourceSet(
            new DefaultResourceSet());

    private final ConceptMappingNeighbourhoodLoader<NeoD3Matrix> mappingLoader;

    // = new ConceptMappingNeighbourhoodLoader(conceptMappingService,
    // resourceManager, errorHandler);

    private ResourceSet automaticResources;

    private final ConceptResourceManager conceptResourceManager;

    // private final ResourceCategorizer resourceCategorizer;

    // @Inject
    // private final ResourceManager resourceManager;

    /*
     * TODO The callback is meant to check whether the graph is initialized (and
     * not disposed) when methods are called (to prevent errors in asynchronous
     * callbacks that return after the graph has been disposed or before it has
     * been initialized).
     */
    private final NodeExpansionCallback<NeoD3Matrix> expansionCallback;

    public NeoD3Matrix(DataTypeValidator dataValidation,
            ConceptMappingNeighbourhoodLoader<NeoD3Matrix> mappingLoader,
            // ResourceCategorizer resourceCategorizer,
            ResourceManager resourceManager,
            ResourceCategorizer resourceCategorizer) {
        super(dataValidation);

        this.expansionCallback = new MatrixExpansionCallback(this);
        // this.mappingLoader = new
        // ConceptMappingNeighbourhoodLoader(conceptMappingService,
        // resourceManager, errorHandler);
        this.mappingLoader = mappingLoader;
        // this.resourceCategorizer = resourceCategorizer;
        conceptResourceManager = new ConceptResourceManager(resourceManager,
                resourceCategorizer, this);

    }

    @Override
    public SpecializedResourceManager getSpecificResourceManager() {
        return this.conceptResourceManager;
    }

    @Override
    public void setAutomaticResources(ResourceSet automaticResources) {
        this.automaticResources = automaticResources;
    }

    @Override
    public ResourceSet getAutomaticResources() {
        return this.automaticResources;
    }

    private void addConceptsToMatrix(
            LightweightCollection<VisualItem> addedVisualItems) {
        this.conceptVisualItems.addAll(addedVisualItems.toList());
        for (VisualItem item : addedVisualItems) {
            this.conceptResources.addAll(item.getResources());
        }
        // matrixWidget.addEvents(getTimeLineEvents(addedResourceItems));
    }

    private void createConceptItems(
            LightweightCollection<VisualItem> addedVisualItems) {

        for (VisualItem visualItem : addedVisualItems) {
            visualItem.setDisplayObject(new ConceptMatrixItem(visualItem));

            /*
             * NOTE: all node configuration should be done when calling the
             * automatic expanders, since they rely on returning the correct
             * graph contents etc.
             * 
             * NOTE: we do not execute the expanders if we are restoring the
             * graph
             */
            // registry.getAutomaticExpander(type).expand(visualItem,
            // expansionCallback);
            mappingLoader.expand(visualItem, expansionCallback);
        }
    }

    @Override
    public Widget createWidget() {
        matrixWidget = new NeoD3MatrixWidget();

        matrixWidget.setHeight("100%");
        matrixWidget.setWidth("100%");

        return matrixWidget;
    }

    @Override
    public String getName() {
        return "Mapping Matrix";
    }

    @Override
    public Slot[] getSlots() {
        return SLOTS;
    }

    // private JsTimeLineEvent[] getTimeLineEvents(
    // LightweightCollection<VisualItem> resourceItems) {
    //
    // JsTimeLineEvent[] events = new JsTimeLineEvent[resourceItems.size()];
    // int counter = 0;
    // for (VisualItem item : resourceItems) {
    // TimeLineItem timelineItem = (TimeLineItem) item.getDisplayObject();
    // events[counter++] = timelineItem.getTimeLineEvent();
    // }
    // return events;
    // }

    public NeoD3MatrixWidget getMatrixWidget() {
        return matrixWidget;
    }

    private void removeConceptsFromMatrix(
            LightweightCollection<VisualItem> removedVisualItems) {
        this.conceptVisualItems.removeAll(removedVisualItems.toList());
        for (VisualItem item : removedVisualItems) {
            this.conceptResources.removeAll(item.getResources());
        }
        // matrixWidget.removeConceptFromMatrix(getTimeLineEvents(removedResourceItems));
    }

    // @Override
    // public void restore(Memento state,
    // PersistableRestorationService restorationService,
    // ResourceSetAccessor accessor) {
    //
    // setMainBandZoomIndex((Integer) state.getValue(MEMENTO_ZOOM_PREFIX + 0));
    // setOverviewBandZoomIndex((Integer) state
    // .getValue(MEMENTO_ZOOM_PREFIX + 1));
    //
    // // IMPORTANT: set date *AFTER* zoom restored
    // setCenterVisibleDate((Date) state.getValue(MEMENTO_DATE));
    // }

    // @Override
    // public Memento save(ResourceSetCollector resourceSetCollector) {
    // Memento state = new Memento();
    // state.setValue(MEMENTO_DATE, getCenterVisibleDate());
    // state.setValue(MEMENTO_ZOOM_PREFIX + 0, getMainBandZoomIndex());
    // state.setValue(MEMENTO_ZOOM_PREFIX + 1, getOverviewBandZoomIndex());
    // return state;
    // }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        // matrixWidget.layout();
    }

    @Override
    public void update(Delta<VisualItem> delta,
            LightweightCollection<Slot> updatedSlots) {

        // TODO pull up
        if (!matrixWidget.isAttached()) {
            return;
        }

        LightweightCollection<VisualItem> addedVisualItems = delta
                .getAddedElements();
        if (!addedVisualItems.isEmpty()) {
            createConceptItems(addedVisualItems);
            addConceptsToMatrix(addedVisualItems);
            // updateStatusStyling(addedVisualItems);
        }

        LightweightCollection<VisualItem> updatedVisualItems = delta
                .getUpdatedElements();
        if (!updatedVisualItems.isEmpty()) {
            // updateStatusStyling(updatedVisualItems);
        }

        LightweightCollection<VisualItem> removedVisualItems = delta
                .getRemovedElements();
        if (!removedVisualItems.isEmpty()) {
            removeConceptsFromMatrix(removedVisualItems);
        }

        // TODO refactor
        if (!updatedSlots.isEmpty()) {
            for (VisualItem visualItem : getVisualItems()) {
                ConceptMatrixItem matrixItem = (ConceptMatrixItem) visualItem
                        .getDisplayObject();
                for (Slot slot : updatedSlots) {
                    // if (slot.equals(BORDER_COLOR)) {
                    // timelineItem.updateBorderColor();
                    // } else if (slot.equals(COLOR)) {
                    // timelineItem.updateColor();
                    // }
                }
            }
        }

        updateView();
    }

    public void updateView() {
        this.matrixWidget.updateView(this.conceptVisualItems);
    }

    @Override
    public CompositeVisualItemBehavior createVisualItemBehaviors(
            VisualItemBehaviorFactory behaviorFactory) {
        CompositeVisualItemBehavior composite = behaviorFactory
                .createEmptyCompositeVisualItemBehavior();

        composite.add(behaviorFactory
                .createDefaultHighlightingVisualItemBehavior());

        composite.add(behaviorFactory.createDefaultDragVisualItemBehavior());

        composite.add(behaviorFactory
                .createDefaultPopupWithHighlightingVisualItemBehavior());

        composite.add(behaviorFactory
                .createDefaultSwitchSelectionVisualItemBehavior());

        return composite;
    }

    private class ConceptResourceManager extends SpecializedResourceManager {

        public ConceptResourceManager(ResourceManager resourceManager,
                ResourceCategorizer resourceCategorizer,
                RequiresAutomaticResourceSet automaticResourceOwner) {
            super(resourceManager, resourceCategorizer, automaticResourceOwner);

        }

        @Override
        public boolean containsResourceWithUri(String resourceUri) {
            return NeoD3Matrix.this.conceptResources
                    .containsResourceWithUri(resourceUri);
        }

        @Override
        public Resource getResourceByUri(String value) {
            return NeoD3Matrix.this.conceptResources.getByUri(value);
        }
    }
}