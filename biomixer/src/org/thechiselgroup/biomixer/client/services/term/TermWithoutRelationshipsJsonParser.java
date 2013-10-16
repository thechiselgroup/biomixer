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
package org.thechiselgroup.biomixer.client.services.term;

import java.util.Map;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

public class TermWithoutRelationshipsJsonParser extends
        AbstractJsonResultParser {

    final String ONTOLGOY_ACRONYM_FROM_URL_PREFIX = "/ontologies/";

    @Inject
    public TermWithoutRelationshipsJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    public Resource parseConcept(String ontologyAcronym, String json) {
        Object jsonObject = parse(json);
        return parseConcept(ontologyAcronym, jsonObject);
    }

    public Resource parseConcept(Object jsonObject) {
        String ontologyLink = asString(get(get(jsonObject, "links"), "ontology"));
        String computedAcronym = ontologyLink.substring(ontologyLink
                .lastIndexOf(ONTOLGOY_ACRONYM_FROM_URL_PREFIX)
                + ONTOLGOY_ACRONYM_FROM_URL_PREFIX.length());
        return parseConcept(computedAcronym, jsonObject);
    }

    public Resource parseConcept(String ontologyAcronym, Object jsonObject) {

        String fullId = asString(get(jsonObject, "@id"));
        String label = asString(get(jsonObject, "prefLabel"));
        String type = asString(get(jsonObject, "type"));

        Resource result = Resource.createIndexedResource(Concept.toConceptURI(
                ontologyAcronym, fullId));

        result.putValue(Concept.ID, fullId);
        result.putValue(Concept.ONTOLOGY_ACRONYM, ontologyAcronym);
        result.putValue(Concept.TYPE, type);
        result.putValue(Concept.LABEL, label);
        result.putValue(Concept.UI_LABEL, Concept.constructUiLabel(result));

        return result;
    }

    /**
     * Receives the same object that we want for the parseConcept() method, but
     * we are looking specifically for the properties in it, and for the hasA
     * and partOf relations in particular. Returns a map of concept ids and
     * their relation type.
     * 
     * @param ontologyAcronym
     * @param jsonObject
     * @return
     */
    public Map<String, String> parseForCompositionProperties(String json) {
        Map<String, String> compositionMap = CollectionFactory
                .createStringMap();
        Object jsonObject = parse(json);
        Object propertiesObject = get(jsonObject, "properties");

        Set<String> propertyNames = getObjectProperties(propertiesObject);
        // Need to do this funny little job because the composition property
        // names are variable, and contain things such as the ontology acronym
        // in them. They appear to all end the same way though...
        for (String name : propertyNames) {
            if (name.endsWith("has_part")) {
                Object propArray = get(propertiesObject, name);
                int length = length(propArray);
                for (int i = 0; i < length; i++) {
                    compositionMap.put(asString(get(propArray, i)),
                            Concept.HAS_PART_CONCEPTS);
                }
            }
            if (name.endsWith("part_of")) {
                Object propArray = get(propertiesObject, name);
                int length = length(propArray);
                for (int i = 0; i < length; i++) {
                    compositionMap.put(asString(get(propArray, i)),
                            Concept.PART_OF_CONCEPTS);
                }
            }
        }

        return compositionMap;
    }
}
