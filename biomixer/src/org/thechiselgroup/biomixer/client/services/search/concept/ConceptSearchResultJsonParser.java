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

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.term.TermWithoutRelationshipsJsonParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

public class ConceptSearchResultJsonParser extends
        TermWithoutRelationshipsJsonParser {

    @Inject
    public ConceptSearchResultJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    public Set<Resource> parseSearchResults(String json) {
        Set<Resource> resources = new HashSet<Resource>();
        Object jsonObject = parse(json);
        Object searchResults = get(jsonObject, "collection");
        for (int i = 0; i < length(searchResults); i++) {
            Object conceptObject = get(searchResults, i);

            resources.add(this.parseConcept(conceptObject));
        }

        return resources;
    }
}
