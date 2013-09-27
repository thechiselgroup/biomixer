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

import java.util.List;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.embeds.TimeoutErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.services.mapping.ConceptMappingServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ViewWithResourceManager;

import com.google.inject.Inject;

public class ConceptMappingNeighbourhoodExpander<T extends ViewWithResourceManager>
        extends AbstractConceptMappingNeighbourhoodExpander<T> {

    private final TermServiceAsync termService;

    @Inject
    public ConceptMappingNeighbourhoodExpander(
            ConceptMappingServiceAsync mappingService,
            ErrorHandler errorHandler, ResourceManager resourceManager,
            TermServiceAsync termService) {

        super(mappingService, resourceManager, errorHandler);
        this.termService = termService;
    }

    @Override
    protected void expandNeighbourhood(VisualItem visualItem,
            final Resource concept, final NodeExpansionCallback<T> callback,
            List<Resource> mappings) {

        final String conceptUri = concept.getUri();

        for (final Resource mapping : mappings) {
            String sourceUri = Mapping.getSource(mapping);
            String targetUri = Mapping.getTarget(mapping);

            assert conceptUri.equals(sourceUri) || conceptUri.equals(targetUri);
            assert !(conceptUri.equals(sourceUri) && conceptUri
                    .equals(targetUri));

            final String otherUri = conceptUri.equals(sourceUri) ? targetUri
                    : sourceUri;

            assert !otherUri.equals(conceptUri);

            if (resourceManager.contains(otherUri)) {
                callback.addAutomaticResource(resourceManager
                        .getByUri(otherUri));
                callback.addAutomaticResource(mapping);
                continue;
            }

            termService.getBasicInformation(Concept.getOntologyId(otherUri),
                    Concept.getConceptId(otherUri),
                    new TimeoutErrorHandlingAsyncCallback<Resource>(
                            errorHandler) {

                        @Override
                        protected String getMessage(Throwable caught) {
                            return "Could not get basic information for \""
                                    + concept.getValue(Concept.LABEL) + "\" "
                                    + getOntologyInfoForErrorMessage(concept);
                        }

                        @Override
                        protected void runOnSuccess(Resource result)
                                throws Exception {

                            Resource addedResource = resourceManager
                                    .add(result);
                            callback.addAutomaticResource(mapping);
                            callback.addAutomaticResource(addedResource);
                        }
                    });
        }

        callback.updateArcsForVisuaItems(LightweightCollections
                .toCollection(visualItem));
    }
}