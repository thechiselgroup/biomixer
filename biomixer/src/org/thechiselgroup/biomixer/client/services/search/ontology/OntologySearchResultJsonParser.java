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

import com.google.gwt.user.client.Window;
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
        // String virtualOntologyId = getIntAsString(jsonItem, "ontologyId") +
        // "";
        String ontologyAcronym = asString(get(jsonItem, "acronym"));

        // node.name = ontologyDetails.name;
        // // node.ONTOLOGY_VERSION_ID = ontologyDetails.id;
        // node.uriId = ontologyDetails["@id"]; // Use the URI isntead of
        // virtual id
        // node.LABEL = ontologyDetails.name;
        // node.description = ontologyDetails.description;
        // // node.VIEWING_RESTRICTIONS = ontologyDetails.viewingRestrictions;
        // // might be missing
        //
        // // TODO XXX If we want Description, I think we need to grab the most
        // recent submission
        // // and take it fromt here. This is another API call per ontology.
        // // /ontologies/:acronym:/lastest_submission

        // Resource resource = new Resource(
        // Ontology.toOntologyURI(virtualOntologyId));

        Resource resource = new Resource(
                Ontology.toOntologyURI(ontologyAcronym));

        // resource.putValue(Ontology.ONTOLOGY_VERSION_ID,
        // asString(get(jsonItem, "id")));
        resource.putValue(Ontology.ONTOLOGY_ACRONYM,
                asString(get(jsonItem, "acronym")));
        resource.putValue(Ontology.ONTOLOGY_URI, asString(get(jsonItem, "@id")));
        resource.putValue(Ontology.ONTOLOGY_FULL_NAME,
                asString(get(jsonItem, "name")));
        // TODO XXXX Description is now held in latest_submission of ontology.
        // it will arrive in a later call.
        // resource.putValue(Ontology.DESCRIPTION,
        // asString(get(jsonItem, "description")));

        // I do not know where viewing restrictions are in the new API, but they
        // are not here!
        // try {
        // resource.putValue(
        // Ontology.VIEWING_RESTRICTIONS,
        // asString(getPossiblyMissing(jsonItem, "viewingRestriction")));
        // } catch (Throwable t) {
        // // nothing
        // }

        return resource;
    }

    @Override
    public Set<Resource> parse(String json) {
        // Set<Resource> resources = new HashSet<Resource>();
        // Object searchResults = get(
        // get(get(get(get(get(super.parse(json), "success"), "data"), 0),
        // "list"), 0), "ontologyBean");
        // int in = 0;
        // int total = 0;
        // if (isArray(searchResults)) {
        // for (int i = 0; i < length(searchResults); i++) {
        // Resource item = analyzeItem(get(searchResults, i));
        // if (passFilter(item)) {
        // resources.add(item);
        // in++;
        // }
        // total++;
        // }
        // } else {
        // Resource item = analyzeItem(searchResults);
        // if (passFilter(item)) {
        // resources.add(item);
        // in++;
        // }
        // }

        int ontologiesSkipped = 0;

        Set<Resource> resources = new HashSet<Resource>();
        // Object searchResults = get(
        // get(get(get(get(get(super.parse(json), "success"), "data"), 0),
        // "list"), 0), "ontologyBean");

        int in = 0;
        int total = 0;
        // if (isArray(searchResults)) {
        // for (int i = 0; i < length(searchResults); i++) {
        Object jsonObject = super.parse(json);
        for (String key : getObjectProperties(jsonObject)) {
            Resource item = analyzeItem(get(jsonObject, key));
            if (passFilter(item)) {
                resources.add(item);
                in++;
            } else {
                // Window.alert("Filter skipped " + item.toString());
                ontologiesSkipped++;
            }
            total++;
        }
        // } else {
        // Resource item = analyzeItem(searchResults);
        // if (passFilter(item)) {
        // resources.add(item);
        // in++;
        // }
        // }

        Window.alert("OntologySearchResultJsonParser, skipped "
                + ontologiesSkipped + " of total " + total);

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
