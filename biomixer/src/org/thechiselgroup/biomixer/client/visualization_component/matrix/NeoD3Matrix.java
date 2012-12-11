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
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.behaviors.CompositeVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.AbstractViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer.VisualItemBehaviorFactory;

import com.google.gwt.user.client.ui.Widget;

// TODO The zoom levels of the different bands should be configurable 
public class NeoD3Matrix extends AbstractViewContentDisplay
// implements
// MatrixRenderer
{

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

    private HashSet<VisualItem> concepts = new HashSet<VisualItem>();

    public NeoD3Matrix(DataTypeValidator dataValidation) {
        super(dataValidation);
    }

    private void addConceptsToMatrix(
            LightweightCollection<VisualItem> addedResourceItems) {
        this.concepts.addAll(addedResourceItems.toList());
        // matrixWidget.addEvents(getTimeLineEvents(addedResourceItems));
    }

    private void createConceptItems(
            LightweightCollection<VisualItem> addedVisualItems) {

        for (VisualItem visualItem : addedVisualItems) {
            visualItem.setDisplayObject(new ConceptMatrixItem(visualItem));
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
            LightweightCollection<VisualItem> removedResourceItems) {
        this.concepts.removeAll(removedResourceItems.toList());
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

        this.matrixWidget.updateView(this.concepts);
    }

    // private void updateStatusStyling(
    // LightweightCollection<VisualItem> visualItems) {
    //
    // for (VisualItem visualItem : visualItems) {
    // visualItem.<TimeLineItem> getDisplayObject().setStatusStyling();
    // }
    // }

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

}