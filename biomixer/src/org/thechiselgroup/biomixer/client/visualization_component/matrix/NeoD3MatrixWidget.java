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

import java.util.HashMap;
import java.util.HashSet;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

public class NeoD3MatrixWidget extends Widget {

    private HandlerManager handlerManager = new HandlerManager(this);

    JSONObject matrixJSONContextObject;

    // private JsTimeLineEventSource eventSource;
    //
    // private DateTimeFormat inputFormat = DateTimeFormat
    // .getFormat("MMM d yyyy HH:mm:ss z");

    // // TODO http://code.google.com/p/google-web-toolkit/issues/detail?id=3415
    // // wait for fix to switch to "EEE, dd MMM yyyy HH:mm:ss z"
    // public final static DateTimeFormat GMT_FORMAT = DateTimeFormat
    // .getFormat("dd MMM yyyy HH:mm:ss z");

    // private JsTimeLine jsTimeLine;

    // private String mainBandWidth = "80%";
    //
    // private String overviewBandWidth = "20%";

    public NeoD3MatrixWidget() {
        Element d3Div = DOM.createDiv();
        d3Div.setAttribute("d3RootDiv", "");
        d3Div.setAttribute("style", "background: white");
        setElement(d3Div);

        initializeView();
    }

    private void initializeView() {
        matrixJSONContextObject = initD3Layout(this.getElement());

    }

    public void updateView(HashSet<VisualItem> concepts) {
        /*-
         * (Trick: this dash prevents code formatting from clobbering my layout below!)
         * (The @ formatter stuff I tried malfunctioned...)
         * 
         * We need to convert our Java structures to json.
         * The D3 will want the concepts as "nodes" and the mappings as "links".
         * The links will have numerically addressed "source" and "target" each,
         * relative to the index of the nodes in question.
         * The data format expected by D3 is like below:
         * 
         * {
         *      "nodes":[
         *         {"name":"CHEBI_48961","group":0}, 
         *         {"name":"GO_0009493","group":1}, 
         *         {"name":"CHEBI_38553","group":0}, 
         *         {"name":"GO_0005489","group":1}, 
         *         {"name":"D000456","group":2}, 
         *         {"name":"Alga","group":4}
         *     ],
         *     "links":[
         *         {"source":0, "target":1, "value": 1},
         *         {"source":2, "target":3, "value": 1},
         *         {"source":120, "target":119, "value": 1},
         *         {"source":126, "target":125, "value": 1},
         *         {"source":94, "target":118, "value": 1}
         *     ]
         *  }
         * 
         */

        // Converting this is horrible. Part of the problem is that the links
        // use array indices rather than the string reference.
        // The other problem is that the links are separated from their nodes.
        // In the original RESTreturn values, the links are embedded in the
        // nodes, essentially.

        HashMap<String, Integer> conceptIndices = new HashMap<String, Integer>();

        // Start of json
        // StringBuilderImpl jsonStrBuilder = new StringBuilderImpl();
        // jsonStrBuilder.append("{");
        JSONObject jsonObj = new JSONObject();

        MatrixJsonData matrixJsonData = MatrixJsonData.createMatrixJsonData();

        // jsonStrBuilder.append("\"nodes\":[");

        JSONArray jsonNodeArray = new JSONArray();
        jsonObj.put("nodes", jsonNodeArray);

        JsArray<JavaScriptObject> jsNodeArray = JavaScriptObject.createArray()
                .cast();

        for (VisualItem visItem : concepts) {
            ConceptMatrixItem displayObject = visItem.getDisplayObject();

            // jsonStrBuilder.append("{\"name\":"+displayObject.getConceptFullId()+",");
            // jsonStrBuilder.append("\"group\":"+getGroupForOntology(displayObject.getOntologyId()));
            // jsonStrBuilder.append("},");

            JSONObject nodeObject = new JSONObject();
            nodeObject.put("name", new JSONString(displayObject.getLabel()));
            nodeObject.put("uri",
                    new JSONString(displayObject.getConceptFullId()));
            nodeObject.put("group", new JSONNumber(
                    getGroupForOntology(displayObject.getOntologyId())));
            int index = jsonNodeArray.size();
            jsonNodeArray.set(index, nodeObject);

            int addedIndex = matrixJsonData.pushNode(displayObject.getLabel(),
                    displayObject.getConceptFullId(),
                    getGroupForOntology(displayObject.getOntologyId()));

            assert (addedIndex == index);

            conceptIndices.put(visItem.getResources().getFirstElement()
                    .getUri(), index);
        }
        // jsonStrBuilder.append("], ");

        // jsonStrBuilder.append("\"links\":[");

        JSONArray jsonLinkArray = new JSONArray();
        jsonObj.put("links", jsonLinkArray);

        for (VisualItem visItem : concepts) {
            // Combine arrays to avoid two code blocks with nearly identicle
            // code to maintain.
            UriList sourceUris = visItem.getResources().getFirstElement()
                    .getUriListValue(Concept.INCOMING_MAPPINGS);
            UriList targetUris = visItem.getResources().getFirstElement()
                    .getUriListValue(Concept.OUTGOING_MAPPINGS);
            UriList[] uris = { sourceUris, targetUris };

            String centralUri = visItem.getResources().getFirstElement()
                    .getUri();

            for (int ioIndex = 0; ioIndex <= 1; ioIndex++) {
                UriList otherUris = uris[ioIndex];
                for (String loopedUri : otherUris) {
                    // Incoming? Looped is source, else central is source.
                    String sourceUri = (ioIndex == 0) ? loopedUri : centralUri;
                    // Incoming? Central is target, looped is target.
                    String targetUri = (ioIndex == 0) ? centralUri : loopedUri;

                    JSONObject linkObject = new JSONObject();
                    linkObject.put("source",
                            new JSONNumber(conceptIndices.get(sourceUri)));
                    // new JSONString(sourceUri));
                    linkObject.put("target",
                            new JSONNumber(conceptIndices.get(targetUri)));
                    // new JSONString(targetUri));

                    linkObject.put("target", new JSONNumber(1));
                    jsonLinkArray.set(jsonLinkArray.size(), linkObject);

                    int addedIndex = matrixJsonData.pushLink(
                            conceptIndices.get(sourceUri),
                            conceptIndices.get(targetUri), 1);

                    // jsonStrBuilder.append("{");
                    // jsonStrBuilder.append("\"source\":"+sourceUri+",");
                    // jsonStrBuilder.append("\"target\":"+targetUri+",");
                    // jsonStrBuilder.append("\"value\":1");
                    // jsonStrBuilder.append("},");
                }
            }
        }
        // jsonStrBuilder.append("]");

        // End of json
        // jsonStrBuilder.append("}");

        // TODO matrixJsonData doesn't seem to work right now. Fix it. I prefer
        // that to jsonObj and to using a string builder.
        Window.alert(new JSONObject(matrixJsonData).toString());
        applyD3Layout(this.getElement(), matrixJSONContextObject,
                matrixJsonData);
        // jsonObj.toString());
        // jsonStrBuilder.toString());

    }

    private HashMap<String, Integer> ontologyGroupNumbers = new HashMap<String, Integer>();

    private int getGroupForOntology(String ontologyId) {
        if (!ontologyGroupNumbers.containsKey(ontologyId)) {
            ontologyGroupNumbers.put(ontologyId, ontologyGroupNumbers.size());
        }
        return ontologyGroupNumbers.get(ontologyId);
    }

    // This was the very simple way to load data from the prototype. The data is
    // already prepared, and there was no way to change it. The prototype
    // demonstrated the graphics and the mouse interaction, but not data
    // swapping.
    private native void applyD3Layout(Element div,
            JSONObject matrixJSONContextObject, String jsonString)/*-{
		$wnd.updateMatrixLayoutString(div, matrixJSONContextObject, jsonString);
    }-*/;

    // Uses the same method as the less cool JSONObject receiving version
    private native void applyD3Layout(Element div,
            JSONObject matrixJSONContextObject, MatrixJsonData jsonMatrixData)/*-{
		$wnd.updateMatrixLayout(div, matrixJSONContextObject, jsonMatrixData);
    }-*/;

    private native void applyD3Layout(Element div,
            JSONObject matrixJSONContextObject, JSONObject jsonObject)/*-{
		$wnd.updateMatrixLayout(div, matrixJSONContextObject, jsonObject);
    }-*/;

    private native JSONObject initD3Layout(Element div)/*-{
		return $wnd.initMatrixLayout(div);
    }-*/;

    // public void addEvents(JsTimeLineEvent[] events) {
    // // eventSource.addEvents(events);
    // // jsTimeLine.paint();
    // updateView();
    // }

    // public void removeConceptFromMatrix(JsTimeLineEvent[] events) {
    // // eventSource.removeEvents(events);
    // // jsTimeLine.paint();
    // updateView();
    // }

    // public HandlerRegistration addScrollHandler(
    // TimelineInteractionEventHandler handler) {
    // return handlerManager
    // .addHandler(TimelineInteractionEvent.TYPE, handler);
    // }

    // private BandInformation createBandInformation(int bandIndex) {
    // return new BandInformation(bandIndex,
    // jsTimeLine.getZoomIndex(bandIndex),
    // jsTimeLine.getMinVisibleDateAsGMTString(bandIndex),
    // jsTimeLine.getMaxVisibleDateAsGMTString(bandIndex));
    // }
    //
    // private BandInformation createBandInformation(int bandIndex,
    // JavaScriptObject centerDate) {
    //
    // return new BandInformation(bandIndex,
    // jsTimeLine.getZoomIndex(bandIndex),
    // jsTimeLine.getMinVisibleDateAsGMTString(bandIndex, centerDate),
    // jsTimeLine.getMaxVisibleDateAsGMTString(bandIndex, centerDate));
    // }

    // private void eventPainted(int bandIndex, JsTimeLineEvent event) {
    // String labelElementID = getEventElementID(bandIndex, "label", event);
    // String iconElementID = getEventElementID(bandIndex, "icon", event);
    // event.getTimeLineItem().onPainted(labelElementID, iconElementID);
    //
    // // TODO use just one listener instead of one per item (for
    // // performance)
    // // 1. get the id of the element
    // // ((Element) e.getCurrentEventTarget().cast()).getId()
    // // 2. resolve timeline event from id
    // }

    // public Date getCenterVisibleDate() {
    // // TODO
    // // http://code.google.com/p/google-web-toolkit/issues/detail?id=3415
    // // wait for fix to switch to "EEE, dd MMM yyyy HH:mm:ss z"
    // return GMT_FORMAT.parse(jsTimeLine.getCenterVisibleDateAsGMTString()
    // .substring(5));
    // }

    // public final String getEventElementID(int bandIndex, String elementType,
    // JsTimeLineEvent event) {
    // return jsTimeLine.getEventElementID(bandIndex, elementType, event);
    // }

    // public String getMainBandWidth() {
    // return mainBandWidth;
    // }

    // public String getOverviewBandWidth() {
    // return overviewBandWidth;
    // }

    // public JsTimeLine getTimeLine() {
    // return jsTimeLine;
    // }

    // public final int getZoomIndex(int bandNumber) {
    // return jsTimeLine.getZoomIndex(bandNumber);
    // }

    // public void layout() {
    // if (jsTimeLine != null) {
    // jsTimeLine.layout();
    // }
    // }

    @Override
    protected void onAttach() {
        super.onAttach();
        //
        // if (jsTimeLine == null) {
        // eventSource = JsTimeLineEventSource.create();
        //
        // jsTimeLine = JsTimeLine.create(getElement(), eventSource,
        // inputFormat.format(new Date()), mainBandWidth,
        // overviewBandWidth);
        //
        // jsTimeLine.disableBubbles();
        // jsTimeLine.registerPaintListener(new JsTimelinePaintCallback() {
        // @Override
        // public void eventPainted(int bandIndex, JsTimeLineEvent event) {
        // NeoD3MatrixWidget.this.eventPainted(bandIndex, event);
        // }
        // });
        // jsTimeLine
        // .registerInteractionHandler(new JsTimelineInteractionCallback() {
        // @Override
        // public void onInteraction(String interaction,
        // int bandIndex) {
        // NeoD3MatrixWidget.this.onInteraction(interaction,
        // bandIndex);
        // }
        //
        // @Override
        // public void onInteraction(String interaction,
        // int bandIndex, JavaScriptObject newCenterDate) {
        // NeoD3MatrixWidget.this.onInteraction(interaction,
        // bandIndex, newCenterDate);
        // }
        // });
        // }
    }

    private void onInteraction(String interaction, int bandIndex) {
        // event construction is expensive so we check if there are any handlers
        // if (handlerManager.getHandlerCount(TimelineInteractionEvent.TYPE) ==
        // 0) {
        // return;
        // }

        // handlerManager.fireEvent(new TimelineInteractionEvent(this,
        // bandIndex,
        // interaction, new BandInformation[] { createBandInformation(0),
        // createBandInformation(1) }));
    }

    protected void onInteraction(String interaction, int bandIndex,
            JavaScriptObject newCenterDate) {

        // event construction is expensive so we check if there are any handlers
        // if (handlerManager.getHandlerCount(TimelineInteractionEvent.TYPE) ==
        // 0) {
        // return;
        // }

        // handlerManager.fireEvent(new TimelineInteractionEvent(this,
        // bandIndex,
        // interaction, new BandInformation[] {
        // createBandInformation(0, newCenterDate),
        // createBandInformation(1, newCenterDate) }));
    }

    // public void setCenterVisibleDate(Date date) {
    // assert date != null;
    // // TODO use output format once
    // // http://code.google.com/p/google-web-toolkit/issues/detail?id=3415
    // // is fixed.
    // jsTimeLine.setCenterVisibleDate(DateTimeFormat.getFormat(
    // "EEE, dd MMM yyyy HH:mm:ss z").format(date));
    // }

    // public void setMainBandWidth(String mainBandWidth) {
    // this.mainBandWidth = mainBandWidth;
    // }

    // public void setOverviewBandWidth(String overviewBandWidth) {
    // this.overviewBandWidth = overviewBandWidth;
    // }

    // public final void setZoomIndex(int bandNumber, int zoomIndex) {
    // jsTimeLine.setZoomIndex(bandNumber, zoomIndex);
    // }
}
