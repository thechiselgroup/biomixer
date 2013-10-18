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
package org.thechiselgroup.biomixer.client.services.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormatFactory;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

public class MappingResponseJsonParser extends AbstractJsonResultParser {

    @Inject
    public MappingResponseJsonParser(JsonParser jsonParser,
            DateTimeFormatFactory dateTimeFormatFactory) {
        super(jsonParser);
    }

    private Resource parseForConceptMapping(Object mappingData) {
        // Get process that produced the mapping
        Object processArray = get(mappingData, "process");
        String processes = "";
        for (int i = 0; i < length(processArray); i++) {
            processes += get(get(processArray, i), "name") + " ";
        }
        processes = processes.trim();

        // Get mapping relation
        Object mappingPair = get(mappingData, "classes");
        Object firstMapping = get(mappingPair, 0);
        Object secondMapping = get(mappingPair, 1);

        // Get process, and mapping endpoints

        // We have no mapping id anymore! Combine concept ids instead.
        String firstConceptId = asString(get(firstMapping, "@id"));
        String secondConceptId = asString(get(secondMapping, "@id"));

        String firstOntologyUrl = asString(get(get(firstMapping, "links"),
                "ontology"));
        String secondOntologyUrl = asString(get(get(secondMapping, "links"),
                "ontology"));
        String firstOntologyAcronym = firstOntologyUrl
                .substring(firstOntologyUrl.lastIndexOf("/") + 1);
        String secondOntologyAcronym = secondOntologyUrl
                .substring(secondOntologyUrl.lastIndexOf("/") + 1);

        String id = firstConceptId + "->" + secondConceptId;

        Resource mappingResource = Mapping.createMappingResource(id,
                firstConceptId, secondConceptId, firstOntologyAcronym,
                secondOntologyAcronym);
        mappingResource.putValue(Mapping.PROCESSES, processes);

        return mappingResource;
    }

    public List<Resource> parseForConceptMapping(String json) {
        List<Resource> result = new ArrayList<Resource>();

        Object jsonObject = parse(json);
        Set<String> keys = getObjectProperties(jsonObject);
        for (String index : keys) {
            Object mappingPair = get(jsonObject, index);
            result.add(parseForConceptMapping(mappingPair));
        }

        return result;
    }
}
