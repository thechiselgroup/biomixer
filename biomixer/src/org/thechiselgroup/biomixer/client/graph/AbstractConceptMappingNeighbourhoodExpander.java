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

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.mapping.MappingServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.AbstractGraphNodeSingleResourceNeighbourhoodExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AbstractConceptMappingNeighbourhoodExpander extends
        AbstractGraphNodeSingleResourceNeighbourhoodExpander {

    private final MappingServiceAsync mappingService;

    protected AbstractConceptMappingNeighbourhoodExpander(
            MappingServiceAsync mappingService,
            ResourceManager resourceManager, ErrorHandler errorHandler) {

        super(errorHandler, resourceManager);

        this.mappingService = mappingService;
    }

    @Override
    protected String getErrorMessageWhenNeighbourhoodloadingFails(
            Resource resource) {
        return "Could not expand all mappings for \""
                + resource.getValue(Concept.LABEL) + "\" ("
                + resource.getValue(Concept.CONCEPT_ONTOLOGY_NAME) + ")";
    }

    @Override
    protected boolean isNeighbourhoodLoaded(VisualItem visualItem,
            Resource resource) {
        assert Concept.isConcept(resource);
        return resourceManager.containsAllReferencedResources(resource,
                Concept.INCOMING_MAPPINGS)
                && resourceManager.containsAllReferencedResources(resource,
                        Concept.OUTGOING_MAPPINGS);
    }

    @Override
    protected void loadNeighbourhood(VisualItem visualItem, Resource resource,
            AsyncCallback<ResourceNeighbourhood> callback) {

        String ontologyId = (String) resource
                .getValue(Concept.VIRTUAL_ONTOLOGY_ID);
        String conceptId = (String) resource.getValue(Concept.FULL_ID);

        mappingService.getMappings(ontologyId, conceptId, callback);
    }

    @Override
    protected List<Resource> reconstructNeighbourhood(VisualItem visualItem,
            final Resource concept) {

        List<Resource> mappings = new ArrayList<Resource>();

        mappings.addAll(resourceManager.resolveResources(concept,
                Concept.INCOMING_MAPPINGS));
        mappings.addAll(resourceManager.resolveResources(concept,
                Concept.OUTGOING_MAPPINGS));

        return mappings;
    }

}