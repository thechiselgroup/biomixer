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

import java.util.List;

import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.choosel.core.client.error_handling.ErrorHandler;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceManager;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightCollections;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;
import org.thechiselgroup.choosel.visualization_component.graph.client.GraphNodeExpansionCallback;

import com.google.inject.Inject;

public class ConceptConceptNeighbourhoodExpander extends
        AbstractConceptConceptNeighbourhoodExpander {

    @Inject
    public ConceptConceptNeighbourhoodExpander(ErrorHandler errorHandler,
            ResourceManager resourceManager,
            ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService) {

        super(errorHandler, resourceManager, conceptNeighbourhoodService);
    }

    @Override
    protected void expandNeighbourhood(VisualItem visualItem, Resource resource,
            GraphNodeExpansionCallback graph, List<Resource> neighbourhood) {

        for (Resource neighbour : neighbourhood) {
            if (!graph.containsResourceWithUri(neighbour.getUri())) {
                graph.addAutomaticResource(neighbour);
            }
        }

        graph.updateArcsForVisuaItems(LightweightCollections
                .toCollection(visualItem));
    }
}