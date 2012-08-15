/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel, Bo Fu
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
package org.thechiselgroup.biomixer.client.services.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.error_handling.LoggerProvider;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Returns pre-defined answer for some cell aging related concepts, returns
 * empty answers otherwise. Enables quicker manual UI testing without
 * dependencies on mapping REST service.
 */
public class FakeMappingService implements MappingServiceAsync {

    private static final String CONCEPT_1_ID = "http://purl.bioontology.org/ontology/GO/GO_0007569";

    private static final String CONCEPT_1_ONTOLOGY = "1506";

    private static final String CONCEPT_2_ID = "http://www.bootstrep.eu/ontology/GRO#CellAging";

    private static final String CONCEPT_2_ONTOLOGY = "1106";

    private static final int RESPONSE_DELAY_MS = 1500;

    private static final Resource MAPPING_1 = Mapping.createMappingResource(
            "m1", Concept.toConceptURI(CONCEPT_1_ONTOLOGY, CONCEPT_1_ID),
            Concept.toConceptURI(CONCEPT_2_ONTOLOGY, CONCEPT_2_ID));

    private final Logger logger;

    @Inject
    public FakeMappingService(LoggerProvider loggerProvider) {
        this.logger = loggerProvider.getLogger();
    }

    protected void doGetMappings(String ontologyId, String conceptFullId,
            AsyncCallback<ResourceNeighbourhood> callback) {

        logger.info("fake - doGetMappings(ontologyId='" + ontologyId
                + "',conceptFullId='" + conceptFullId + "')");

        List<Resource> mappings = new ArrayList<Resource>();
        UriList incomingMappings = new UriList();
        UriList outgoingMappings = new UriList();

        if (CONCEPT_1_ONTOLOGY.equals(ontologyId)
                && CONCEPT_1_ID.equals(conceptFullId)) {

            outgoingMappings.add(MAPPING_1.getUri());
            mappings.add(MAPPING_1);
        }

        if (CONCEPT_2_ONTOLOGY.equals(ontologyId)
                && CONCEPT_2_ID.equals(conceptFullId)) {

            incomingMappings.add(MAPPING_1.getUri());
            mappings.add(MAPPING_1);
        }

        Map<String, Serializable> partialProperties = CollectionFactory
                .createStringMap();
        partialProperties.put(Concept.INCOMING_MAPPINGS, incomingMappings);
        partialProperties.put(Concept.OUTGOING_MAPPINGS, outgoingMappings);

        callback.onSuccess(new ResourceNeighbourhood(partialProperties,
                mappings));
    }

    @SuppressWarnings("unused")
    @Override
    public void getMappings(final String ontologyId,
            final String conceptFullId, final boolean mappingNeighbourhood,
            final AsyncCallback<ResourceNeighbourhood> callback) {

        if (RESPONSE_DELAY_MS <= 0) {
            doGetMappings(ontologyId, conceptFullId, callback);
        } else {
            new Timer() {
                @Override
                public void run() {
                    doGetMappings(ontologyId, conceptFullId, callback);
                }
            }.schedule(RESPONSE_DELAY_MS);
        }
    }
}