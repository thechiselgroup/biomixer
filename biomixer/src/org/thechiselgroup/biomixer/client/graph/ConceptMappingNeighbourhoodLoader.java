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

import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.mapping.MappingServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphNodeExpansionCallback;

import com.google.inject.Inject;

public class ConceptMappingNeighbourhoodLoader extends
        AbstractConceptMappingNeighbourhoodExpander {

    @Inject
    public ConceptMappingNeighbourhoodLoader(
            MappingServiceAsync mappingService,
            ResourceManager resourceManager, ErrorHandler errorHandler) {

        super(mappingService, resourceManager, errorHandler);
    }

    @Override
    protected void expandNeighbourhood(VisualItem visualItem, Resource resource,
            GraphNodeExpansionCallback graph, List<Resource> neighbourhood) {

        /*
         * Adds mappings for which both concept ends are contained in graph (if
         * they are not contained yet)
         */
        List<Resource> displayableMappings = new ArrayList<Resource>();
        for (Resource mapping : neighbourhood) {
            assert Mapping.isMapping(mapping);

            String sourceUri = (String) mapping.getValue(Mapping.SOURCE);
            String targetUri = (String) mapping.getValue(Mapping.TARGET);

            if (graph.containsResourceWithUri(sourceUri)
                    && graph.containsResourceWithUri(targetUri)
                    && !graph.containsResourceWithUri(mapping.getUri())) {

                displayableMappings.add(mapping);
            }
        }

        for (Resource mapping : displayableMappings) {
            graph.addAutomaticResource(mapping);
        }

        graph.updateArcsForVisuaItems(LightweightCollections
                .toCollection(visualItem));
    }
}