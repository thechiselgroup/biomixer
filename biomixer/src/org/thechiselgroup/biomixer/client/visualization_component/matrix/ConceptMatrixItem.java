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

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

public class ConceptMatrixItem {

    //
    // protected static final String CSS_RESOURCE_ITEM_ICON =
    // "resourceItemIcon";
    //
    // protected static final int Z_INDEX_DEFAULT = 5;
    //
    // protected static final int Z_INDEX_HIGHLIGHTED = 20;
    //
    // protected static final int Z_INDEX_SELECTED = 10;
    //
    // private static final int OVERVIEW_BAND_ID = 1;
    //
    // private static final String TICK_ELEMENT = "tick";
    //
    // private JsTimeLineEvent timeLineEvent;
    //
    // private String iconElementID;
    //
    // private String tickElementID;

    private final VisualItem visualItem;

    public ConceptMatrixItem(VisualItem visualItem) {

        this.visualItem = visualItem;

        //
        // Object date = visualItem.getValue(TimeLine.DATE);
        // String dateString;
        // if (date instanceof Date) {
        // dateString = date.toString();
        // } else if (date instanceof String) {
        // dateString = (String) date;
        // } else {
        // throw new RuntimeException(date.toString()
        // + " not an appropriate date");
        // }
        //
        // timeLineEvent = JsTimeLineEvent.create(dateString, null, "", this);
        // tickElementID = view.getEventElementID(OVERVIEW_BAND_ID,
        // TICK_ELEMENT,
        // timeLineEvent);
    }

    public String getLabel() {
        return Concept.getLabel(visualItem.getResources().getFirstElement());
    }

    public String getConceptFullId() {
        return Concept.getFullId(visualItem.getResources().getFirstElement());
    }

    public String getConceptId() {
        return Concept
                .getConceptId(visualItem.getResources().getFirstElement());
    }

    public String getOntologyId() {
        return Concept.getOntologyAcronym(visualItem.getResources()
                .getFirstElement());
    }

    public UriList getIncomingMappings() {
        return (UriList) visualItem.getResources().getFirstElement()
                .getValue(Concept.INCOMING_MAPPINGS);
    }

    public UriList getOutgoingMappings() {
        return (UriList) visualItem.getResources().getFirstElement()
                .getValue(Concept.OUTGOING_MAPPINGS);
    }

    //
    // private Color getBorderColor() {
    // return (Color) visualItem.getValue(TimeLine.BORDER_COLOR);
    // }
    //
    // private Color getColor() {
    // return (Color) visualItem.getValue(TimeLine.COLOR);
    // }
    //
    // public JsTimeLineEvent getTimeLineEvent() {
    // return timeLineEvent;
    // }
    //
    // public void onPainted(String labelElementID, String iconElementID) {
    // assert labelElementID != null;
    // assert iconElementID != null;
    //
    // /*
    // * every time the event is repainted, we need to hook up our listeners
    // * again
    // */
    // registerListeners(labelElementID);
    // registerListeners(iconElementID);
    //
    // this.iconElementID = iconElementID;
    //
    // // fix icon representation
    // updateIconElement(iconElementID);
    // }
    //
    // private void registerListeners(String elementID) {
    // Element element = DOM.getElementById(elementID);
    //
    // DOM.sinkEvents(element, Event.MOUSEEVENTS | Event.ONCLICK);
    // DOM.setEventListener(element, new EventListener() {
    // @Override
    // public void onBrowserEvent(Event event) {
    // visualItem.reportInteraction(new VisualItemInteraction(event));
    // }
    // });
    // }
    //
    // private void setIconColor(Color color, Color borderColor) {
    // if (iconElementID == null) {
    // return;
    // }
    //
    // Element element = DOM.getElementById(iconElementID);
    //
    // // can happen on resize
    // if (element == null) {
    // return;
    // }
    //
    // Element div = (Element) element.getFirstChild();
    // if (div != null) {
    // CSS.setBackgroundColor(div, color.toRGBa());
    // CSS.setBorderColor(div, borderColor.toRGBa());
    // }
    // }
    //
    // public void setStatusStyling() {
    // Color color = getColor();
    // Color borderColor = getBorderColor();
    //
    // setIconColor(color, borderColor);
    //
    // if (DOM.getElementById(tickElementID) == null) {
    // return;
    // }
    //
    // updateTickZIndex();
    // updateTickColor(color);
    // }
    //
    // public void updateBorderColor() {
    // Color color = getColor();
    //
    // setIconColor(color, getBorderColor());
    //
    // if (DOM.getElementById(tickElementID) == null) {
    // return;
    // }
    //
    // updateTickColor(color);
    // }
    //
    // public void updateColor() {
    // Color color = getColor();
    //
    // setIconColor(color, getBorderColor());
    //
    // if (DOM.getElementById(tickElementID) == null) {
    // return;
    // }
    //
    // updateTickColor(color);
    // }
    //
    // private void updateIconElement(String iconElementID) {
    // Element element = DOM.getElementById(iconElementID);
    //
    // if (element == null) {
    // return;
    // }
    //
    // element.setInnerHTML("<div style='background-color: "
    // + getColor().toRGBa() + "; border-color: "
    // + getBorderColor().toRGBa() + ";' class='"
    // + CSS_RESOURCE_ITEM_ICON + "'></div>");
    // }
    //
    // private void updateTickColor(Color color) {
    // Element tickElement = DOM.getElementById(tickElementID);
    //
    // CSS.setBackgroundColor(tickElement, color.toRGBa());
    //
    // // /*
    // // * TODO refactor: this sets a bottom border on highlighted ticks,
    // // * because they are otherwise hard to see
    // // */
    // // if (visualItem.isStatus(Subset.HIGHLIGHTED, Status.COMPLETE,
    // // Status.PARTIAL)) {
    // // CSS.setBorderBottom(tickElement, "6px solid black");
    // // } else {
    // // CSS.setBorderBottom(tickElement, "none");
    // // }
    //
    // timeLineEvent.setTickBackgroundColor(color.toRGBa());
    // }
    //
    // private void updateTickZIndex() {
    // if (visualItem
    // .isStatus(Subset.HIGHLIGHTED, Status.FULL, Status.PARTIAL)) {
    // CSS.setZIndex(tickElementID, Z_INDEX_HIGHLIGHTED);
    // timeLineEvent.setTickZIndex("" + Z_INDEX_HIGHLIGHTED);
    // } else if (visualItem.isStatus(Subset.SELECTED, Status.FULL,
    // Status.PARTIAL)) {
    // CSS.setZIndex(tickElementID, Z_INDEX_SELECTED);
    // timeLineEvent.setTickZIndex("" + Z_INDEX_SELECTED);
    // } else {
    // CSS.setZIndex(tickElementID, Z_INDEX_DEFAULT);
    // timeLineEvent.setTickZIndex("" + Z_INDEX_DEFAULT);
    // }
    // }

}
