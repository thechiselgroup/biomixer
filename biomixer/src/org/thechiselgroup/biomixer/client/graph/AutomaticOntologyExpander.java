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
package org.thechiselgroup.biomixer.client.graph;

import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.embeds.TimeoutErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologyMetricServiceAsync;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologyMetrics;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

import com.google.inject.Inject;

public class AutomaticOntologyExpander implements NodeExpander<Graph> {

    private final OntologyMetricServiceAsync ontologyMetricService;

    protected final ErrorHandler errorHandler;

    @Inject
    public AutomaticOntologyExpander(
            OntologyMetricServiceAsync ontologyMetricService,
            ErrorHandler errorHandler) {

        this.ontologyMetricService = ontologyMetricService;
        this.errorHandler = errorHandler;
    }

    // @Override
    // public void expand(VisualItem item, GraphNodeExpansionCallback graph) {
    // ontologyMetricLoader.expand(item, graph);
    // }

    protected String getErrorMessageWhenLoadingFails(String additionalMessage) {
        return "Could not retrieve statistics for ontology nodes \""
                + additionalMessage;
    }

    @Override
    public final void expand(final VisualItem visualItem,
            final NodeExpansionCallback<Graph> expansionCallback) {

        assert visualItem != null;
        assert expansionCallback != null;

        ontologyMetricService.getMetrics(getSingleResource(visualItem),
                new TimeoutErrorHandlingAsyncCallback<OntologyMetrics>(
                        errorHandler) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return getErrorMessageWhenLoadingFails("Error finding ontology metrics");
                    }

                    @Override
                    protected void runOnSuccess(OntologyMetrics results)
                            throws Exception {

                        if (!expansionCallback.isInitialized()) {
                            return;
                        }

                        int numClasses = 0;
                        if (null != results) {
                            if (results.numberOfClasses != null) {
                                numClasses = results.numberOfClasses;
                            }
                        } else {
                            // Cannot expand without metric information
                            return;
                        }

                        // TODO There must be a smarter way to do this rather
                        // than setting the node size directly...but for now...
                        // Set the node's size property.
                        // Resource resource = getSingleResource(visualItem);
                        // visualItem.getValue(slot)
                        // node.setSize(size);
                        Node node = expansionCallback.getDisplay().getNode(
                                visualItem.getId());
                        // 2 * is for the radius conversion...added after
                        // changing stuff in circle.
                        // Rest of the formula is arbitrary for aesthetics.
                        expansionCallback.getDisplay().setNodeStyle(node,
                                GraphDisplay.NODE_SIZE, numClasses + "");
                        // Refactored so that transformers are used per graph to
                        // control
                        // node sizing. This can be a raw number.
                        // 2 * (4 + Math.sqrt((numClasses) / 10)) + "");

                        Resource resource = visualItem.getResources()
                                .getFirstElement();
                        resource.putValue(Ontology.NUMBER_OF_CONCEPTS,
                                numClasses);
                    }

                });
    }

    protected String getOntologyInfoForErrorMessage(Resource resource) {
        String ontologyName = (String) resource.getValue(Ontology.LABEL);
        if (ontologyName != null) {
            return "(" + ontologyName + ")";
        } else {
            String virtualOntologyId = (String) resource
                    .getValue(Ontology.VIRTUAL_ONTOLOGY_ID);
            return "(virtual ontology id: " + virtualOntologyId + ")";
        }
    }

    protected final Resource getSingleResource(VisualItem visualItem) {
        assert visualItem.getResources().size() == 1;
        return visualItem.getResources().getFirstElement();
    }

}