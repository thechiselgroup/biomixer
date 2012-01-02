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
package org.thechiselgroup.biomixer.client.visualization_component.chart;

import org.thechiselgroup.choosel.core.client.util.collections.Delta;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightCollection;
import org.thechiselgroup.choosel.core.client.visualization.model.AbstractViewContentDisplay;
import org.thechiselgroup.choosel.core.client.visualization.model.Slot;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem.Status;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem.Subset;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItemInteraction;
import org.thechiselgroup.choosel.protovis.client.PV;
import org.thechiselgroup.choosel.protovis.client.PVEventHandler;
import org.thechiselgroup.choosel.protovis.client.PVMark;
import org.thechiselgroup.choosel.protovis.client.PVPanel;
import org.thechiselgroup.choosel.protovis.client.ProtovisWidget;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsArgs;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsArrayGeneric;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsUtils;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

/**
 * An abstract ViewContentDisplay class which any Protovis chart's specific
 * ViewContentDisplay can extend.
 * 
 * @author Bradley Blashko
 * @author Lars Grammel
 */
public abstract class ChartViewContentDisplay extends
        AbstractViewContentDisplay {

    private static final String CSS_CLASS_CHOOSEL_CHART_WIDGET = "Choosel-Chart-Widget";

    // TODO wrapper for jsarraygeneric that implements java.util.List
    protected JsArrayGeneric<VisualItem> visualItemsJsArray = JsUtils
            .createJsArrayGeneric();

    protected String[] eventTypes = { PV.Event.CLICK, PV.Event.MOUSEDOWN,
            PV.Event.MOUSEMOVE, PV.Event.MOUSEOUT, PV.Event.MOUSEOVER,
            PV.Event.MOUSEUP };

    private PVEventHandler handler = new PVEventHandler() {
        @Override
        public void onEvent(Event e, String pvEventType, JsArgs args) {
            ChartViewContentDisplay.this.onEvent(e, pvEventType, args);
        }
    };

    /**
     * Flags status that chart widget is rendering. While rendering, events are
     * discarded.
     */
    protected boolean isRendering;

    protected ProtovisWidget chartWidget;

    protected int width;

    protected int height;

    public void addVisualItem(VisualItem visualItem) {
        visualItemsJsArray.push(visualItem);
    }

    /**
     * Called after the rendering is finished. Subclasses can override this
     * method to clear temporary objects that were constructed for the rendering
     * process.
     */
    protected void afterRender() {
    }

    /**
     * Is called before the chart is rendered. Subclasses can override this
     * method to recalculate values that are used for all resource item specific
     * calls from Protovis.
     */
    protected void beforeRender() {
    }

    /**
     * Builds the visualization. <code>buildChart</code> is only called if there
     * are actual data items that can be rendered ( jsVisualItems.length >= 1 ).
     */
    protected abstract void buildChart();

    @Override
    public final Widget createWidget() {
        chartWidget = new ProtovisWidget();
        chartWidget.addStyleName(CSS_CLASS_CHOOSEL_CHART_WIDGET);
        return chartWidget;
    }

    protected PVPanel getChart() {
        return chartWidget.getPVPanel();
    }

    public VisualItem getVisualItem(int index) {
        assert visualItemsJsArray != null;
        assert 0 <= index;
        assert index < visualItemsJsArray.length();

        return visualItemsJsArray.get(index);
    }

    // TODO refactoring: introduce view item list that offers this functionality
    public boolean hasVisualItemsWithPartialSubset(Subset subset) {
        for (int i = 0; i < visualItemsJsArray.length(); i++) {
            VisualItem visualItem = visualItemsJsArray.get(i);
            if (visualItem.isStatus(subset, Status.PARTIAL)) {
                return true;
            }
        }
        return false;
    }

    protected void onEvent(Event e, String pvEventType, JsArgs args) {
        int index = args.<PVMark> getThis().index();
        getVisualItem(index).reportInteraction(new VisualItemInteraction(e));
    }

    protected abstract void registerEventHandler(String eventType,
            PVEventHandler handler);

    protected void registerEventHandlers() {
        for (String eventType : eventTypes) {
            registerEventHandler(eventType, handler);
        }
    }

    // TODO move into js array to java.util.List wrapper
    public void removeVisualItem(VisualItem visualItem) {
        int occurences = 0;
        for (int i = 0; i < visualItemsJsArray.length(); i++) {
            VisualItem itemFromArray = visualItemsJsArray.get(i);
            if (itemFromArray == visualItem) {
                occurences++;
            } else if (occurences > 0) {
                visualItemsJsArray.set(i - occurences, itemFromArray);
            }
        }
        visualItemsJsArray.setLength(visualItemsJsArray.length() - occurences);
    }

    @Override
    public void setSize(int width, int height) {
        if (width == this.width && height == this.height) {
            return;
        }

        this.width = width;
        this.height = height;

        super.setSize(width, height);

        /*
         * TODO we could use updateChart(false) here to improve the performance.
         * This would require several changes in the chart implementation,
         * though.
         */
        updateChart(true);
    }

    // XXX rename
    protected void setUpChartForRendering(boolean structuralChange) {
        if (structuralChange) {
            chartWidget.initPVPanel();
            if (visualItemsJsArray.length() > 0) {
                buildChart();
                registerEventHandlers();
            } 
        }
    }

    /**
     * A method that listens for any updates on any resource items relevant to
     * the chart. Chart only will get rendered or updated (depending on the
     * situation) once no matter how many resource items are being affected.
     */
    @Override
    public void update(Delta<VisualItem> delta,
            LightweightCollection<Slot> changedSlots) {

        if (!isAttached()) {
            return;
        }

        for (VisualItem visualItem : delta.getAddedElements()) {
            addVisualItem(visualItem);
        }

        for (VisualItem visualItem : delta.getRemovedElements()) {
            removeVisualItem(visualItem);
        }

        /*
         * PERFORMANCE only rebuild the chart SVG DOM elements when structure
         * changes (i.e. resource items are added or removed), otherwise just
         * update their attributes.
         * 
         * TODO check under which circumstances a rebuild is required if
         * structure changes or if rendering is sufficient
         * 
         * TODO changing slots requires a rebuild because it affects the scales
         * and rulers - look for a better solution
         */
        updateChart(!delta.getAddedElements().isEmpty()
                || !delta.getRemovedElements().isEmpty()
                || !changedSlots.isEmpty());
    }

    /**
     * Updates the visualization.
     * 
     * @param structuralChange
     *            If <code>true</code>, the current chart is abandoned and a new
     *            Protovis panel is used and a new visualization is build from
     *            scratch and rendered. If <code>false</code>, the current
     *            visualization is re-rendered and the chart structure will not
     *            change, just the attributes of the SVG elements are updated.
     */
    protected void updateChart(boolean structuralChange) {
        if (!isAttached()) {
            return; // cannot render yet
        }

        setUpChartForRendering(structuralChange);

        /*
         * XXX re-rendering with layout requires reset see
         * "http://groups.google.com/group/protovis/browse_thread/thread/b9032215a2f5ac25"
         * 
         * TODO instead of isRendering flag, remove event listeners before
         * rendering starts and add them again after rendering is finished.
         */
        // XXX how often are event listeners assigned? are they removed?
        try {
            isRendering = true;
            beforeRender();
            getChart().render();
            afterRender(); // TODO move into finally block?
        } finally {
            isRendering = false;
        }
    }
}