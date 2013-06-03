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
package org.thechiselgroup.biomixer.client.servicesnewapi.ontology_overview;

import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCount;
import org.thechiselgroup.biomixer.client.services.ontology_overview.TotalMappingCount;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

public class OntologyNeighbourhoodMappingCountJSONParser extends
        AbstractJsonResultParser {

    @Inject
    public OntologyNeighbourhoodMappingCountJSONParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    @Override
    public TotalMappingCount parse(String json) {
        TotalMappingCount result = new TotalMappingCount();
        Object list = get(
                get(get(get(get(super.parse(json), "success"), "data"), 0),
                        "set"), 0);
        Object jsonArray = get(list, "ontologyPairMappingStatistics");
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
        String sourceOntologyId = "" + asInt(get(jsonItem, "sourceOntologyId"));
        String targetOntologyId = "" + asInt(get(jsonItem, "targetOntologyId"));
        int mappingCount = asInt(get(jsonItem, "mappingCount"));

        return new OntologyMappingCount(sourceOntologyId, targetOntologyId,
                mappingCount);
    }

}