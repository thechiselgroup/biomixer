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
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologyLatestSubmissionDetails;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologyLatestSubmissionServiceAsync;
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

    private final OntologyLatestSubmissionServiceAsync ontologyDescriptionService;

    protected final ErrorHandler errorHandler;

    @Inject
    public AutomaticOntologyExpander(
            OntologyMetricServiceAsync ontologyMetricService,
            OntologyLatestSubmissionServiceAsync ontologyDescriptionService,
            ErrorHandler errorHandler) {

        this.ontologyMetricService = ontologyMetricService;
        this.ontologyDescriptionService = ontologyDescriptionService;
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

        // We have to get the metrics, but we also need the ontology description
        // from a separate API call.
        ontologyMetricService.getMetrics(getSingleResource(visualItem),
                new ErrorHandlingAsyncCallback<OntologyMetrics>(errorHandler) {

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

                        Integer numClasses = null;
                        Integer numIndividuals = null;
                        Integer numProperties = null;
                        // Arbitrarily set the node size to 100 for
                        // private or licensed nodes (or those without
                        // metric results)
                        int nodeSizeBasis = 100;
                        if (null != results) {
                            if (results.numberOfClasses != null) {
                                numClasses = results.numberOfClasses;
                                nodeSizeBasis = numClasses;
                            }
                            if (results.numberOfIndividuals != null) {
                                numIndividuals = results.numberOfIndividuals;
                            }
                            if (results.numberOfProperties != null) {
                                numProperties = results.numberOfProperties;
                            }
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
                                GraphDisplay.NODE_SIZE, nodeSizeBasis + "");
                        // Refactored so that transformers are used per graph to
                        // control
                        // node sizing. This can be a raw number.
                        // 2 * (4 + Math.sqrt((numClasses) / 10)) + "");

                        Resource resource = getSingleResource(visualItem);
                        resource.putValue(Ontology.NUMBER_OF_CLASSES,
                                numClasses);
                        resource.putValue(Ontology.NUMBER_OF_INDIVIDUALS,
                                numIndividuals);
                        resource.putValue(Ontology.NUMBER_OF_PROPERTIES,
                                numProperties);
                        if (null == results) {
                            resource.putValue(Ontology.NOTE,
                                    "No metrics available. Private or licensed ontology.");
                        }
                    }

                });

        ontologyDescriptionService
                .getLatestSubmissionDetails(
                        getSingleResource(visualItem),
                        new ErrorHandlingAsyncCallback<OntologyLatestSubmissionDetails>(
                                errorHandler) {

                            @Override
                            protected String getMessage(Throwable caught) {
                                return getErrorMessageWhenLoadingFails("Error finding ontology submission details");
                            }

                            @Override
                            protected void runOnSuccess(
                                    OntologyLatestSubmissionDetails results)
                                    throws Exception {

                                // Can do more, but for now just set description
                                // of ontology.
                                Resource resource = getSingleResource(visualItem);
                                resource.putValue(Ontology.DESCRIPTION,
                                        results.description);

                                // The expansionCallback. doesn't really need to
                                // be informed.
                            }
                        });
    }

    protected String getOntologyInfoForErrorMessage(Resource resource) {
        String ontologyName = (String) resource.getValue(Ontology.ONTOLOGY_FULL_NAME);
        if (ontologyName != null) {
            return "(" + ontologyName + ")";
        } else {
            String ontologyAcronym = (String) resource
                    .getValue(Ontology.ONTOLOGY_ACRONYM);
            return "(ontology id: " + ontologyAcronym + ")";
        }
    }

    protected final Resource getSingleResource(VisualItem visualItem) {
        assert visualItem.getResources().size() == 1;
        return visualItem.getResources().getFirstElement();
    }

}