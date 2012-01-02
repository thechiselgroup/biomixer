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

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.choosel.visualization_component.graph.client.DefaultGraphExpansionRegistry;

import com.google.inject.Inject;

public class BioMixerGraphExpansionRegistry extends
        DefaultGraphExpansionRegistry {

    @Inject
    public void registerAutomaticConceptExpander(
            AutomaticConceptExpander expander) {

        putAutomaticExpander(Concept.RESOURCE_URI_PREFIX, expander);
    }

    @Inject
    public void registerConceptConceptNeighbourhoodExpander(
            ConceptConceptNeighbourhoodExpander expander) {

        putNodeMenuEntry(Concept.RESOURCE_URI_PREFIX, "Concepts", expander);
    }

    @Inject
    public void registerConceptMappingNeighbourhoodExpander(
            ConceptMappingNeighbourhoodExpander expander) {

        putNodeMenuEntry(Concept.RESOURCE_URI_PREFIX, "Mappings", expander);
    }

    @Inject
    public void registerMappingConceptNeighbourhoodExpander(
            MappingExpander expander) {

        putNodeMenuEntry(Mapping.RESOURCE_URI_PREFIX, "Concepts", expander);
    }

}