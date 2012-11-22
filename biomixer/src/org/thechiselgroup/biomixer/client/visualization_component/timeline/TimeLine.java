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
package org.thechiselgroup.biomixer.client.visualization_component.timeline;

import java.util.Date;

import org.thechiselgroup.biomixer.client.DataTypeValidator;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.AbstractViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

import com.google.gwt.user.client.ui.Widget;

// TODO The zoom levels of the different bands should be configurable 
public class TimeLine extends AbstractViewContentDisplay {

    public static final Slot DATE = new Slot("date", "Date", DataType.DATE);

    public final static Slot COLOR = new Slot("color", "Color", DataType.COLOR);

    public final static Slot BORDER_COLOR = new Slot("borderColor",
            "Border Color", DataType.COLOR);

    private static final Slot[] SLOTS = new Slot[] { BORDER_COLOR, COLOR, DATE };

    private static final String MEMENTO_DATE = "date";

    private static final String MEMENTO_ZOOM_PREFIX = "zoom-band-";

    private TimeLineWidget timelineWidget;

    public TimeLine(DataTypeValidator dataValidation) {
        super(dataValidation);
    }

    private void addEventsToTimeline(
            LightweightCollection<VisualItem> addedResourceItems) {

        timelineWidget.addEvents(getTimeLineEvents(addedResourceItems));
    }

    private void createTimeLineItems(
            LightweightCollection<VisualItem> addedVisualItems) {

        for (VisualItem visualItem : addedVisualItems) {
            visualItem.setDisplayObject(new TimeLineItem(visualItem, this));
        }
    }

    @Override
    public Widget createWidget() {
        timelineWidget = new TimeLineWidget();

        timelineWidget.setHeight("100%");
        timelineWidget.setWidth("100%");

        return timelineWidget;
    }

    public Date getCenterVisibleDate() {
        return timelineWidget.getCenterVisibleDate();
    }

    public final String getEventElementID(int bandIndex, String elementType,
            JsTimeLineEvent event) {
        return timelineWidget.getEventElementID(bandIndex, elementType, event);
    }

    public int getMainBandZoomIndex() {
        return timelineWidget.getZoomIndex(0);
    }

    @Override
    public String getName() {
        return "Timeline";
    }

    public int getOverviewBandZoomIndex() {
        return timelineWidget.getZoomIndex(1);
    }

    @Override
    public Slot[] getSlots() {
        return SLOTS;
    }

    @Override
    public boolean validateDataTypes(ResourceSet resourceSet) {
        // TODO This was added at a time when no data requirements were
        // recognized for this. Specialized visualizers should override this.
        return true;
    }

    private JsTimeLineEvent[] getTimeLineEvents(
            LightweightCollection<VisualItem> resourceItems) {

        JsTimeLineEvent[] events = new JsTimeLineEvent[resourceItems.size()];
        int counter = 0;
        for (VisualItem item : resourceItems) {
            TimeLineItem timelineItem = (TimeLineItem) item.getDisplayObject();
            events[counter++] = timelineItem.getTimeLineEvent();
        }
        return events;
    }

    public TimeLineWidget getTimeLineWidget() {
        return timelineWidget;
    }

    private void removeEventsFromTimeline(
            LightweightCollection<VisualItem> removedResourceItems) {
        timelineWidget.removeEvents(getTimeLineEvents(removedResourceItems));
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        setMainBandZoomIndex((Integer) state.getValue(MEMENTO_ZOOM_PREFIX + 0));
        setOverviewBandZoomIndex((Integer) state
                .getValue(MEMENTO_ZOOM_PREFIX + 1));

        // IMPORTANT: set date *AFTER* zoom restored
        setCenterVisibleDate((Date) state.getValue(MEMENTO_DATE));
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        Memento state = new Memento();
        state.setValue(MEMENTO_DATE, getCenterVisibleDate());
        state.setValue(MEMENTO_ZOOM_PREFIX + 0, getMainBandZoomIndex());
        state.setValue(MEMENTO_ZOOM_PREFIX + 1, getOverviewBandZoomIndex());
        return state;
    }

    public void setCenterVisibleDate(Date date) {
        timelineWidget.setCenterVisibleDate(date);
    }

    public void setMainBandZoomIndex(int zoomIndex) {
        timelineWidget.setZoomIndex(0, zoomIndex);
    }

    public void setOverviewBandZoomIndex(int zoomIndex) {
        timelineWidget.setZoomIndex(1, zoomIndex);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        timelineWidget.layout();
    }

    @Override
    public void update(Delta<VisualItem> delta,
            LightweightCollection<Slot> updatedSlots) {

        // TODO pull up
        if (!timelineWidget.isAttached()) {
            return;
        }

        LightweightCollection<VisualItem> addedVisualItems = delta
                .getAddedElements();
        if (!addedVisualItems.isEmpty()) {
            createTimeLineItems(addedVisualItems);
            addEventsToTimeline(addedVisualItems);
            updateStatusStyling(addedVisualItems);
        }

        LightweightCollection<VisualItem> updatedVisualItems = delta
                .getUpdatedElements();
        if (!updatedVisualItems.isEmpty()) {
            updateStatusStyling(updatedVisualItems);
        }

        LightweightCollection<VisualItem> removedVisualItems = delta
                .getRemovedElements();
        if (!removedVisualItems.isEmpty()) {
            removeEventsFromTimeline(removedVisualItems);
        }

        // TODO refactor
        if (!updatedSlots.isEmpty()) {
            for (VisualItem visualItem : getVisualItems()) {
                TimeLineItem timelineItem = (TimeLineItem) visualItem
                        .getDisplayObject();
                for (Slot slot : updatedSlots) {
                    if (slot.equals(BORDER_COLOR)) {
                        timelineItem.updateBorderColor();
                    } else if (slot.equals(COLOR)) {
                        timelineItem.updateColor();
                    }
                }
            }
        }
    }

    private void updateStatusStyling(
            LightweightCollection<VisualItem> visualItems) {

        for (VisualItem visualItem : visualItems) {
            visualItem.<TimeLineItem> getDisplayObject().setStatusStyling();
        }
    }

}