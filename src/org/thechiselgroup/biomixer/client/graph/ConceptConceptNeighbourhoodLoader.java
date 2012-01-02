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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphNodeExpansionCallback;

import com.google.inject.Inject;

public class ConceptConceptNeighbourhoodLoader extends
        AbstractConceptConceptNeighbourhoodExpander {

    @Inject
    public ConceptConceptNeighbourhoodLoader(ErrorHandler errorHandler,
            ResourceManager resourceManager,
            ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService) {

        super(errorHandler, resourceManager, conceptNeighbourhoodService);
    }

    @Override
    protected void expandNeighbourhood(VisualItem visualItem, Resource resource,
            GraphNodeExpansionCallback graph, List<Resource> neighbourhood) {

        /*
         * We do not add the resources in the neighbourhood to the graph as this
         * would cause recursive neighbourhood expansion.
         */
        graph.updateArcsForVisuaItems(LightweightCollections
                .toCollection(visualItem));
    }
}