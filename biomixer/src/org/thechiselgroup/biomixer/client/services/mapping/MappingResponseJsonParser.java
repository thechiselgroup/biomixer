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
import java.util.Date;
import java.util.List;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormat;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormatFactory;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonArray;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonItem;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

public class MappingResponseJsonParser extends AbstractJsonResultParser {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.S z";

    private final DateTimeFormat dateFormat;

    public MappingResponseJsonParser(JsonParser jsonParser,
            DateTimeFormatFactory dateTimeFormatFactory) {
        super(jsonParser);
        this.dateFormat = dateTimeFormatFactory
                .createDateTimeFormat(DATE_PATTERN);
    }

    private Resource parseMapping(JsonItem mapping) {
        String id = getString(mapping, "$.id");

        String sourceOntologyId = getString(mapping, "$.sourceOntologyId");
        // NOTE: odd json format -> {source: [fullId], target: [fullId]}
        String sourceConceptId = getString(mapping, "$.source[0]");
        String sourceUri = Concept.toConceptURI(sourceOntologyId,
                sourceConceptId);

        String targetOntologyId = getString(mapping, "$.targetOntologyId");
        String targetConceptId = getString(mapping, "$.target[0]");
        String targetUri = Concept.toConceptURI(targetOntologyId,
                targetConceptId);

        String mappingType = getString(mapping, "$.mappingType");
        String mappingSource = getString(mapping, "$.mappingSource");
        String mappingSourceName = getString(mapping, "$.mappingSourceName");

        Date date = dateFormat.parse(getString(mapping, "$.date"));

        Resource resource = new Resource(Mapping.toMappingURI(id));

        resource.putValue(Mapping.ID, id);
        resource.putValue(Mapping.SOURCE, sourceUri);
        resource.putValue(Mapping.TARGET, targetUri);
        resource.putValue(Mapping.DATE, date);
        resource.putValue(Mapping.MAPPING_TYPE, mappingType);
        resource.putValue(Mapping.MAPPING_SOURCE, mappingSource);
        resource.putValue(Mapping.MAPPING_SOURCE_NAME, mappingSourceName);

        return resource;
    }

    public List<Resource> parseMapping(String json) {
        List<Resource> result = new ArrayList<Resource>();

        JsonArray mappings = getArray(json,
                "$.success.data[0].contents.mappings");
        for (int i = 0; i < mappings.size(); i++) {
            result.add(parseMapping(mappings.get(i)));
        }

        return result;
    }

}
