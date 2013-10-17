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
package org.thechiselgroup.biomixer.client.services.search.ontology;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

/**
 * See {@ConceptSearchResultJsonParser} for
 * comparison.
 * 
 * @author everbeek
 * 
 */
public class OntologySearchResultJsonParser extends AbstractJsonResultParser {

    private String filterText = null;

    private String filterProperty = "";

    private Collection<String> filterValueSet = null;

    @Inject
    public OntologySearchResultJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    private Resource analyzeItem(Object jsonItem) {
        String ontologyAcronym = asString(get(jsonItem, "acronym"));
        String uri = asString(get(jsonItem, "@id"));
        String name = asString(get(jsonItem, "name"));
        Resource resource = Ontology.createOntologyResource(ontologyAcronym,
                uri, name);

        // TODO XXXX Description is now held in latest_submission of ontology.
        // it will arrive in a later call.
        // resource.putValue(Ontology.DESCRIPTION,
        // asString(get(jsonItem, "description")));

        return resource;
    }

    public Set<Resource> parseOntologySearchResults(String json) {
        int in = 0;
        int total = 0;
        int ontologiesSkipped = 0;
        Set<Resource> resources = new HashSet<Resource>();
        Object searchResults = super.parse(json);
        Set<String> keys = getObjectProperties(searchResults);
        for (String i : keys) {
            Object ontologyJson = get(searchResults, i);
            total++;
            if (passFilter(ontologyJson, "name")
                    || passFilter(ontologyJson, "acronym")) {
                Resource item = analyzeItem(ontologyJson);
                resources.add(item);
                in++;
            } else {
                ontologiesSkipped++;
            }
        }

        // Window.alert("Filtered in/all: " + in + "/" + total);
        return resources;

    }

    public void setFilterPropertyAndContainedText(String filterProperty,
            String filterText) {
        this.filterText = filterText;
        this.filterProperty = filterProperty;
    }

    /**
     * Looks for the given values in reuslts. Full match only, unlike partial
     * match available in other method.
     * 
     * @param filterValueSet
     * @param filterText
     */
    public void setFilterPropertyAndContainedText(String filterProperty,
            Collection<String> filterValueSet) {
        this.filterProperty = filterProperty;
        this.filterValueSet = filterValueSet;
    }

    private boolean passFilter(Object jsonObject,
            String correspondingJsonProperty) {
        // Window.alert("Debuggin filter");
        // Window.alert(this.filterValueSet.contains(item
        // .getValue(this.filterProperty)) + "");
        // Window.alert("pass? "
        // + this.filterValueSet.contains(item
        // .getValue(this.filterProperty))
        // + " for Filtering on property: " + this.filterProperty
        // + " with value: " + (String) item.getValue(this.filterProperty)
        // + " and list "
        // + CollectionUtils.asSortedList(this.filterValueSet).toString());
        if (this.filterProperty == ""
                || (this.filterText == null && this.filterValueSet == null)) {
            return true;
        }
        boolean matchOnSingleProperty = null != this.filterText
                && has(jsonObject, correspondingJsonProperty)
                && asString(get(jsonObject, correspondingJsonProperty))
                        .toLowerCase().contains(this.filterText.toLowerCase());
        boolean matchOnPropertySet = null != this.filterValueSet;
        if (matchOnSingleProperty || matchOnPropertySet) {
            return true;
        }

        return false;
    }

    // I don't like making resources for all the ontologies that we are
    // filtering through...
    // But I want to leave this method here, unused.
    private boolean passFilter(Resource item) {
        // Window.alert("Debuggin filter");
        // Window.alert(this.filterValueSet.contains(item
        // .getValue(this.filterProperty)) + "");
        // Window.alert("pass? "
        // + this.filterValueSet.contains(item
        // .getValue(this.filterProperty))
        // + " for Filtering on property: " + this.filterProperty
        // + " with value: " + (String) item.getValue(this.filterProperty)
        // + " and list "
        // + CollectionUtils.asSortedList(this.filterValueSet).toString());
        if (this.filterProperty == ""
                || (this.filterText == null && this.filterValueSet == null)) {
            return true;
        }
        boolean matchOnSingleProperty = null != this.filterText
                && item.containsProperty(this.filterProperty)
                && ((String) item.getValue(this.filterProperty)).toLowerCase()
                        .contains(this.filterText.toLowerCase());
        boolean matchOnPropertySet = null != this.filterValueSet
                && item.containsProperty(this.filterProperty)
                && this.filterValueSet.contains(item
                        .getValue(this.filterProperty));
        if (matchOnSingleProperty || matchOnPropertySet) {
            return true;
        }

        return false;
    }
}
