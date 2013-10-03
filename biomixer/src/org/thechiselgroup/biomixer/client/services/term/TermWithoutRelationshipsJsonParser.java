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

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

public class TermWithoutRelationshipsJsonParser extends
        AbstractJsonResultParser {

    @Inject
    public TermWithoutRelationshipsJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    public Resource parseConcept(String ontologyAcronym, String json) {
        Object jsonObject = parse(json);
        return parseConcept(ontologyAcronym, jsonObject);
    }

    public Resource parseConcept(String ontologyAcronym, Object jsonObject) {
        String fullId = asString(get(jsonObject, "@id"));
        String label = asString(get(jsonObject, "prefLabel"));
        String type = asString(get(jsonObject, "type"));

        Resource result = new Resource(Concept.toConceptURI(ontologyAcronym,
                fullId));

        result.putValue(Concept.ID, fullId);
        result.putValue(Concept.ONTOLOGY_ACRONYM, ontologyAcronym);
        result.putValue(Concept.TYPE, type);
        result.putValue(Concept.LABEL, label);

        return result;
    }

}
