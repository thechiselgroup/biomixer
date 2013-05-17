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
package org.thechiselgroup.biomixer.client.services.ontology_overview;

import org.thechiselgroup.biomixer.client.services.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

public class OntologyMappingCountJSONParser extends AbstractJsonResultParser {

    private String sourceOntologyId;

    @Inject
    public OntologyMappingCountJSONParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    @Override
    public TotalMappingCount parse(String json) {
        TotalMappingCount result = new TotalMappingCount();
        // Grab source ontology id...they removed form list:
        // Like:
        // "accessedResource":"\/bioportal\/virtual\/mappings\/stats\/ontologies\/1032"
        String resourceString = asString(get(get(super.parse(json), "success"),
                "accessedResource"));
        String ontologyIdCutoffString = "ontologies/";
        int indexOfOntologyId = resourceString
                .lastIndexOf(ontologyIdCutoffString);
        this.sourceOntologyId = resourceString.substring(indexOfOntologyId
                + ontologyIdCutoffString.length());

        Object list = get(
                get(get(get(get(super.parse(json), "success"), "data"), 0),
                        "list"), 0);

        // The empty list returned unfortunately is not *empty*, but is a
        // zero-length string, and throws JSON parsing null exceptions if not
        // checked for. But if we check for it as a string, it throws an
        // exception
        // when it is a json object. Seems like a try-catch is simplest.
        try {
            if (!has(list, "ontologyMappingStatistics")) {
                return result;
            }
        } catch (Exception e) {
            return result;
        }

        Object jsonArray = get(list, "ontologyMappingStatistics");

        if (isArray(jsonArray)) {
            for (int i = 0; i < length(jsonArray); i++) {
                result.add(analyzeItem(get(jsonArray, i)));
            }
        } else {
            result.add(analyzeItem(jsonArray));
        }

        return result;
    }

    private OntologyMappingCount analyzeItem(Object jsonItem) {
        // They appear to have changed the REST service since the previous
        // version
        // of this class.
        String targetOntologyId = "" + asInt(get(jsonItem, "ontologyId"));
        int sourceMappings = asInt(get(jsonItem, "sourceMappings"));
        int targetMappings = asInt(get(jsonItem, "targetMappings"));

        return new OntologyMappingCount(sourceOntologyId, targetOntologyId,
                sourceMappings, targetMappings);
    }

}
