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

import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphNodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphNodeExpansionCallback;

import com.google.inject.Inject;

public class AutomaticConceptExpander implements GraphNodeExpander {

    private final ConceptMappingNeighbourhoodLoader conceptMappingNeighbourhoodLoader;

    private final ConceptConceptNeighbourhoodLoader conceptConceptNeighbourhoodLoader;

    @Inject
    public AutomaticConceptExpander(
            ConceptMappingNeighbourhoodLoader conceptMappingNeighbourhoodLoader,
            ConceptConceptNeighbourhoodLoader conceptConceptNeighbourhoodLoader) {

        this.conceptMappingNeighbourhoodLoader = conceptMappingNeighbourhoodLoader;
        this.conceptConceptNeighbourhoodLoader = conceptConceptNeighbourhoodLoader;
    }

    @Override
    public void expand(VisualItem item, GraphNodeExpansionCallback graph) {
        conceptMappingNeighbourhoodLoader.expand(item, graph);
        conceptConceptNeighbourhoodLoader.expand(item, graph);
    }
}