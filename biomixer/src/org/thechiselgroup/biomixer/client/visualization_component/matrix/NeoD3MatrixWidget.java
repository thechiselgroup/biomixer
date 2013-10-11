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

import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UnionResourceSet;
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
import com.google.gwt.user.client.ui.Widget;

public class NeoD3MatrixWidget extends Widget {

    private HandlerManager handlerManager = new HandlerManager(this);

    JSONObject matrixJSONContextObject;

    private final NeoD3MatrixJavascriptInterface jsInterface = new NeoD3MatrixJavascriptInterface();

    public NeoD3MatrixWidget() {
        Element d3Div = DOM.createDiv();
        d3Div.setAttribute("d3RootDiv", "");
        d3Div.setAttribute("style", "background: white");
        setElement(d3Div);

        initializeView();
    }

    private void initializeView() {
        matrixJSONContextObject = jsInterface.initD3Layout(this.getElement());

    }

    public void updateView(HashSet<VisualItem> concepts,
            UnionResourceSet mappingResources) {
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
            if (!(visItem.getDisplayObject() instanceof ConceptMatrixItem)) {
            	// Remove when concepts are the only things coming through, not mappings
                continue;
            }
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

        for (Resource mappingResource : mappingResources) {
            assert Mapping.isMapping(mappingResource);
            String sourceUri = (String) mappingResource
                    .getValue(Mapping.SOURCE_CONCEPT_URI);
            String targetUri = (String) mappingResource
                    .getValue(Mapping.TARGET_CONCEPT_URI);

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
        // jsonStrBuilder.append("]");

        // End of json
        // jsonStrBuilder.append("}");

        // matrixJsonData is preferable to jsonObj and to using a string builder.
        jsInterface.applyD3Layout(this.getElement(), matrixJSONContextObject,
                matrixJsonData);
        // jsonObj.toString()); // Old way
        // jsonStrBuilder.toString()); // Yet older way

    }

    private HashMap<String, Integer> ontologyGroupNumbers = new HashMap<String, Integer>();

    private int getGroupForOntology(String ontologyId) {
        if (!ontologyGroupNumbers.containsKey(ontologyId)) {
            ontologyGroupNumbers.put(ontologyId, ontologyGroupNumbers.size());
        }
        return ontologyGroupNumbers.get(ontologyId);
    }
}
