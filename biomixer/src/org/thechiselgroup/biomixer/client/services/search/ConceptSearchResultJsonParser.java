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
package org.thechiselgroup.biomixer.client.services.search;

import java.util.HashSet;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonItem;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

public class ConceptSearchResultJsonParser extends AbstractJsonResultParser {

    private static final String ROOT_EXPRESSION = "$..success.data[0].contents.searchResultList.searchBean[*]";

    public ConceptSearchResultJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    private Resource analyzeItem(JsonItem jsonItem) {
        String ontologyId = getString(jsonItem, "$.ontologyId");
        String conceptId = getString(jsonItem, "$.ontologyId");

        Resource resource = new Resource(Concept.toConceptURI(ontologyId,
                conceptId));

        String conceptShortId = getString(jsonItem, "$.conceptIdShort");
        resource.putValue(Concept.FULL_ID, conceptId);
        resource.putValue(Concept.SHORT_ID, conceptShortId);
        resource.putValue(Concept.LABEL,
                getString(jsonItem, "preferredName/text()"));
        resource.putValue(Concept.VIRTUAL_ONTOLOGY_ID, ontologyId);
        resource.putValue(Concept.CONCEPT_ONTOLOGY_NAME,
                getString(jsonItem, "ontologyDisplayLabel/text()"));

        return resource;
    }

    public Set<Resource> parse(String json) {
        Set<Resource> resources = new HashSet<Resource>();
        for (JsonItem jsonItem : getJsonItems(json, ROOT_EXPRESSION)) {
            resources.add(analyzeItem(jsonItem));
        }
        return resources;
    }
}
