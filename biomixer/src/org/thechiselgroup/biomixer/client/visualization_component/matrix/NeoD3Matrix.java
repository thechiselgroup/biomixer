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

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.DataTypeValidator;
import org.thechiselgroup.biomixer.client.Mapping;
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
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ViewWithResourceManager;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer.VisualItemBehaviorFactory;

import com.google.gwt.user.client.ui.Widget;

public class NeoD3Matrix extends AbstractViewContentDisplay implements
        ViewWithResourceManager, RequiresAutomaticResourceSet {

    public final static Slot COLOR = new Slot("color", "Color", DataType.COLOR);

    public final static Slot BORDER_COLOR = new Slot("borderColor",
            "Border Color", DataType.COLOR);

    public static final Slot LABEL_SLOT = new Slot("label", "Label",
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

    private final UnionResourceSet mappingResources = new UnionResourceSet(
            new DefaultResourceSet());

    private final NodeExpander<NeoD3Matrix> mappingLoader;

    private ResourceSet automaticResources;

    private final ConceptResourceManager conceptResourceManager;

    /*
     * The callback is meant to check whether the graph is initialized (and
     * not disposed) when methods are called (to prevent errors in asynchronous
     * callbacks that return after the graph has been disposed or before it has
     * been initialized).
     */
    private final NodeExpansionCallback<NeoD3Matrix> expansionCallback;

    public NeoD3Matrix(
            DataTypeValidator dataValidation,
            ConceptMappingNeighbourhoodLoader<NeoD3Matrix> mappingLoader,
            ResourceManager resourceManager,
            ResourceCategorizer resourceCategorizer) {
        super(dataValidation);

        this.expansionCallback = new MatrixExpansionCallback(this);
        this.mappingLoader = mappingLoader;
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
            if (Concept.isConcept(item.getResources().getFirstElement())) {
                this.conceptResources.addAll(item.getResources());
            } else if (Mapping.isMapping(item.getResources().getFirstElement())) {
                this.mappingResources.addAll(item.getResources());
            }

        }
    }

    private void removeConceptsFromMatrix(
            LightweightCollection<VisualItem> removedVisualItems) {
        this.conceptVisualItems.removeAll(removedVisualItems.toList());
        for (VisualItem item : removedVisualItems) {
            ResourceSet resources = item.getResources();
            this.conceptResources.removeAll(resources);
            // TODO I think we really want to remove mappings that no longer
            // have both ends in the resource set too.
            // This is the problem with using the resource set to track them,
            // rather than using a proper Map object with simple concept URIs...
            for (String mappingURI : resources.getFirstElement()
                    .getUriListValue(Concept.INCOMING_MAPPINGS)) {
                mappingResources.remove(mappingResources.getByUri(mappingURI));
            }
            for (String mappingURI : resources.getFirstElement()
                    .getUriListValue(Concept.OUTGOING_MAPPINGS)) {
                mappingResources.remove(mappingResources.getByUri(mappingURI));
            }
        }
    }

    private void createConceptDisplayObjects(
            LightweightCollection<VisualItem> addedVisualItems) {

        for (VisualItem visualItem : addedVisualItems) {
            if (Concept.isConcept(visualItem.getResources().getFirstElement())) {
                visualItem.setDisplayObject(new ConceptMatrixItem(visualItem));
                mappingLoader.expand(visualItem, expansionCallback);
                // The expander callback leads to mapping objects being created.
            } else if (Mapping.isMapping(visualItem.getResources()
                    .getFirstElement())) {
                visualItem.setDisplayObject(new MappingMatrixItem(visualItem));
            }
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

    public NeoD3MatrixWidget getMatrixWidget() {
        return matrixWidget;
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
            // TODO Mapping VisualItems go through here too, so I have to filter
            // between the two. I decided to do that within the loop inside
            // createConceptItems().
            createConceptDisplayObjects(addedVisualItems);
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
        this.matrixWidget.updateView(this.conceptVisualItems,
                this.mappingResources);
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
            boolean isConcept = isResourceConcept(resourceUri);
            if (isConcept) {
                return NeoD3Matrix.this.conceptResources
                        .containsResourceWithUri(resourceUri);
            } else { // if (isMapping) {
                return NeoD3Matrix.this.mappingResources
                        .containsResourceWithUri(resourceUri);
            }
        }

        @Override
        public Resource getResourceByUri(String resourceUri) {
            boolean isConcept = isResourceConcept(resourceUri);
            if (isConcept) {
                return NeoD3Matrix.this.conceptResources.getByUri(resourceUri);
            } else { // if (isMapping) {
                return NeoD3Matrix.this.mappingResources.getByUri(resourceUri);
            }
        }

        private boolean isResourceConcept(String resourceUri) {
            String category = resourceCategorizer.getCategory(resourceUri);
            boolean isConcept = category.equals(Concept.RESOURCE_URI_PREFIX);
            boolean isMapping = category.equals(Mapping.RESOURCE_URI_PREFIX);
            assert isConcept || isMapping;
            return isConcept;
        }
    }
}