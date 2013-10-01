/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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

import java.util.HashMap;
import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.embeds.TimeoutErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCount;
import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCountServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology_overview.TotalMappingCount;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeBulkExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;

import com.google.gwt.user.client.Window;

/**
 * Frame for expanding data on a collection of {@link VisualItem}s with a single
 * {@link Resource}. This is useful when we have REST calls that support passing
 * a (sub)graph for results (e.g. such as for finding mappings among ontologies
 * that are currently in a graph).
 * 
 * Other expanders have methods with names like isExpandedDataLoaded() and
 * reconstructExpandedData(), which I do not have here. Are they necessary?
 * 
 * @author Eric Verbeek
 */
public class OntologyNodeMappingExpander implements NodeBulkExpander<Graph> {

    private final OntologyMappingCountServiceAsync mappingService;

    protected final ErrorHandler errorHandler;

    // protected final ResourceManager resourceManager;

    public OntologyNodeMappingExpander(
            OntologyMappingCountServiceAsync mappingService,
            ErrorHandler errorHandler) {

        this.errorHandler = errorHandler;
        this.mappingService = mappingService;
    }

    protected String getErrorMessageWhenLoadingFails(String additionalMessage) {
        return "Could not expand all mappings for ontology nodes \""
                + additionalMessage;
    }

    @Override
    public final void expand(
            final LightweightCollection<VisualItem> visualItems,
            final NodeExpansionCallback<Graph> expansionCallback) {

        assert visualItems != null && visualItems.size() > 0;
        assert expansionCallback != null;

        // XXX TODO Fix this!
        Window.alert("Need to modify REST call and parsing. May not have convenient way of doing this.");

        LightweightList<String> ontologyAcronyms = LightweightCollections
                .<String> toList();
        for (VisualItem visItem : visualItems) {
            // Makes same assumption regarding first element of each item. This
            // is done elsewhere too.
            ontologyAcronyms.add((String) visItem.getResources()
                    .getFirstElement().getValue(Ontology.ONTOLOGY_ACRONYM));
        }

        mappingService.getMappingCounts(ontologyAcronyms,
                new TimeoutErrorHandlingAsyncCallback<TotalMappingCount>(
                        errorHandler) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return getErrorMessageWhenLoadingFails("Error finding ontology mappings");
                    }

                    @Override
                    protected void runOnSuccess(TotalMappingCount results)
                            throws Exception {

                        if (!expansionCallback.isInitialized()) {
                            return;
                        }

                        // Make an index to look the visual items up in
                        Map<String, Resource> itemIdMap = new HashMap<String, Resource>();
                        for (VisualItem visItem : visualItems) {
                            Resource resource = getSingleResource(visItem);
                            itemIdMap.put(Ontology.getOntologyId(resource),
                                    resource);
                        }

                        // Iterate through mapping results, and create the links
                        // This happens differently for concept mappings in
                        // calculatePartialProperties(). See there for contrast.
                        for (OntologyMappingCount mapping : results) {
                            Resource sourceResource = itemIdMap.get(mapping
                                    .getSourceOntologyAcronym());
                            Resource targetResource = itemIdMap.get(mapping
                                    .getTargetOntologyAcronym());
                            int mappingNumberOfConcepts = mapping
                                    .getSourceMappingCount();

                            if (null != sourceResource
                                    && null != targetResource) {
                                UriList sourceOutgoing = sourceResource
                                        .getUriListValue(Ontology.OUTGOING_MAPPINGS);

                                UriList targetIncoming = targetResource
                                        .getUriListValue(Ontology.INCOMING_MAPPINGS);

                                // This is a memory leak, because if there are
                                // already arcs in here, and the corresponding
                                // nodes have been removed, they don't all get
                                // updated here. But maybe that is not a
                                // responsibility to be handled here?
                                sourceOutgoing.add(Ontology
                                        .toOntologyURIWithCount(Ontology
                                                .getOntologyId(targetResource),
                                                mappingNumberOfConcepts));
                                targetIncoming.add(Ontology
                                        .toOntologyURIWithCount(Ontology
                                                .getOntologyId(sourceResource),
                                                mappingNumberOfConcepts));

                                sourceResource.putValue(
                                        Ontology.OUTGOING_MAPPINGS,
                                        sourceOutgoing);
                                targetResource.putValue(
                                        Ontology.INCOMING_MAPPINGS,
                                        targetIncoming);
                            }
                        }

                        expansionCallback.updateArcsForVisuaItems(visualItems);
                    }

                });
    }

    protected String getOntologyInfoForErrorMessage(Resource resource) {
        String ontologyName = (String) resource
                .getValue(Concept.CONCEPT_ONTOLOGY_NAME);
        if (ontologyName != null) {
            return "(" + ontologyName + ")";
        } else {
            String ontologyAcronym = (String) resource
                    .getValue(Concept.ONTOLOGY_ACRONYM);
            return "(ontology acronym: " + ontologyAcronym + ")";
        }
    }

    protected final Resource getSingleResource(VisualItem visualItem) {
        assert visualItem.getResources().size() == 1;
        return visualItem.getResources().getFirstElement();
    }

}