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
package org.thechiselgroup.biomixer.client.servicesnewapi.ontology;

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

        // The id is now a URL. This will work for us still, right?
        // It should work fine once we are fully converted, but until then
        // a lot of the older services expect old integer ids. Too bad!
        Resource resource = new Resource(
                Ontology.toOntologyURI(ontologyAcronym));

        // used to be "abbreviation"
        resource.putValue(Ontology.ONTOLOGY_ACRONYM,
                asString(get(jsonItem, "acronym")));

        // Used to be "displayLabel"
        resource.putValue(Ontology.ONTOLOGY_FULL_NAME,
                asString(get(jsonItem, "name")));

        // TODO "description" seems to be moved to the next object, under
        // "latestSubmission".
        // resource.putValue(Ontology.DESCRIPTION,
        // asString(get(jsonItem, "description")));
        try {
            // TODO The new API doesn't seem to have this pinned down yet.
            // The private ontology below has an odd error:
            // http://stagedata.bioontology.org/ontologies/HOM_EHS/latest_submission
            resource.putValue(
                    Ontology.VIEWING_RESTRICTIONS,
                    asString(getPossiblyMissing(jsonItem, "viewingRestriction")));
        } catch (Throwable t) {
            // nothing
        }

        return resource;
    }

    @Override
    public Set<Resource> parse(String json) {
        Set<Resource> resources = new HashSet<Resource>();
        // Would parse with Jackson as in NCBO docs, but GWT doesn't support it.
        Object searchResults = super.parse(json);

        int in = 0;
        int total = 0;
        if (isArray(searchResults)) {
            for (int i = 0; i < length(searchResults); i++) {
                Resource item = analyzeItem(get(searchResults, i));
                if (passFilter(item)) {
                    resources.add(item);
                    in++;
                }
                total++;
            }
        } else if (has(searchResults, "0")) {
            // Larger than 0, but isn't an array...use string integers...oi vay!
            int i = 0;
            boolean keepGoing = true;
            while (keepGoing) {
                if (!has(searchResults, i + "")) {
                    keepGoing = false;
                    break;
                }
                Resource item = analyzeItem(get(searchResults, i + ""));
                if (passFilter(item)) {
                    resources.add(item);
                    in++;
                }
                total++;
                i++;
            }
        } else {
            Resource item = analyzeItem(searchResults);
            if (passFilter(item)) {
                resources.add(item);
                in++;
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

    private boolean passFilter(Resource item) {
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
                && this.filterValueSet.contains(((String) item
                        .getValue(this.filterProperty)).toLowerCase());
        if (matchOnSingleProperty || matchOnPropertySet) {
            return true;
        }

        return false;
    }
}
