/*******************************************************************************
 * Copyright 2012 David Rusk
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
package org.thechiselgroup.biomixer.client.servicesnewapi.search.concept;

import java.util.HashSet;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

public class ConceptSearchResultJsonParser extends AbstractJsonResultParser {

    @Inject
    public ConceptSearchResultJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    private Resource analyzeItem(Object jsonItem) {
        // Used to be ontologyId
        String ontologyId = asString(get(get(jsonItem, "links"), "ontology"));
        // Used to be conceptId
        String conceptId = asString(get(jsonItem, "@id"));

        // TODO What about this? Don't need the redundancy of ontology and
        // concept id
        // now that the ids are URLs and concept URLs are nested into an
        // ontology
        // one.
        Resource resource = new Resource(Concept.toConceptURI(ontologyId,
                conceptId));

        // Used to be conceptIdShort from REST, now substring
        String conceptShortId = conceptId
                .substring(conceptId.lastIndexOf("#") + 1);

        resource.putValue(Concept.FULL_ID, conceptId);
        resource.putValue(Concept.SHORT_ID, conceptShortId);

        // Used to be preferredName
        String conceptLabel = asString(get(jsonItem, "prefLabel"));
        resource.putValue(Concept.LABEL, conceptLabel);

        // Used to be the integer ontology id, now is substring for acronym
        String ontologyAcronym = ontologyId.substring(ontologyId
                .lastIndexOf("/") + 1);
        resource.putValue(Concept.VIRTUAL_ONTOLOGY_ID, ontologyAcronym);

        // Used to be ontolgoyDisplayLabel, now is same as virtual id, since the
        // integer ids are gone, and they are now half-decent identifiers.
        resource.putValue(Concept.CONCEPT_ONTOLOGY_NAME, conceptLabel + " ("
                + ontologyAcronym + ")");

        return resource;
    }

    @Override
    public Set<Resource> parse(String json) {
        Set<Resource> resources = new HashSet<Resource>();
        Object searchResults = get(super.parse(json), "collection");
        if (isArray(searchResults)) {
            for (int i = 0; i < length(searchResults); i++) {
                resources.add(analyzeItem(get(searchResults, i)));
            }
        } else {
            resources.add(analyzeItem(searchResults));
        }
        return resources;
    }
}
