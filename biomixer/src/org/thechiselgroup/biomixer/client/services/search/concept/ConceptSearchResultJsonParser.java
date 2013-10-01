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
package org.thechiselgroup.biomixer.client.services.search.concept;

import java.util.HashSet;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

public class ConceptSearchResultJsonParser extends AbstractJsonResultParser {

    @Inject
    public ConceptSearchResultJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    private Resource analyzeItem(Object jsonItem) {
        Window.alert(jsonItem.toString());
        // TODO XXX Not changed yet
        String ontologyAcronym = getIntAsString(jsonItem, "ontologyId");
        String conceptId = asString(get(jsonItem, "conceptId"));

        Resource resource = new Resource(Concept.toConceptURI(ontologyAcronym,
                conceptId));

        String conceptShortId = asString(get(jsonItem, "conceptIdShort"));
        resource.putValue(Concept.FULL_ID, conceptId);
        resource.putValue(Concept.SHORT_ID, conceptShortId);
        resource.putValue(Concept.LABEL,
                asString(get(jsonItem, "preferredName")));
        resource.putValue(Concept.ONTOLOGY_ACRONYM, ontologyAcronym);
        resource.putValue(Concept.CONCEPT_ONTOLOGY_NAME,
                asString(get(jsonItem, "ontologyDisplayLabel")));

        return resource;
    }

    @Override
    public Set<Resource> parse(String json) {
        Set<Resource> resources = new HashSet<Resource>();
        Object searchResults = get(
                get(get(get(
                        get(get(get(super.parse(json), "success"), "data"), 0),
                        "page"), "contents"), "searchResultList"), "searchBean");
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
