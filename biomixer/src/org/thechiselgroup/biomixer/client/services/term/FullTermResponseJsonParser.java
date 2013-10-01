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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

/**
 * Parses full term information for a concept.
 * 
 * NOTE: see the .json test files used by the unit tests in
 * {@link FullTermResponseJsonParserTest}. They show the structure of the json
 * that must be parsed.
 * 
 * @author drusk
 * 
 */
public class FullTermResponseJsonParser extends AbstractJsonResultParser {

    private static final String OWL_THING = "owl:Thing";

    @Inject
    public FullTermResponseJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    public ResourceNeighbourhood parseNeighbourhood(String ontologyId,
            String json) {
        UriList parentConcepts = new UriList();
        UriList childConcepts = new UriList();
        UriList owningConcepts = new UriList();
        UriList ownedConcepts = new UriList();
        List<Resource> resources = new ArrayList<Resource>();

        /*
         * The code below should fix situations such as the one mentioned in
         * issue #149. However, the mapping discussed in #149 has now been
         * removed by BioPortal. // check if there is a "status" key in the JSON
         * returned //Window.alert("original JSON: " + json); if
         * (json.contains("status") == true) { // inform the user CharSequence
         * s1 = "{"; CharSequence s2 = ");";
         * 
         * int location1 = json.indexOf(s1.toString()); int location2 =
         * json.indexOf(s2.toString()); String finalJson =
         * json.substring(location1, location2);
         * Window.alert("substringed json: " + finalJson); Object statusKey =
         * get(super.parse(finalJson), "status"); Window.alert("statusKey: " +
         * statusKey); if (statusKey == "404") { Window.alert(
         * "This mapping cannot be viewed because the target concept's id cannot be found in the most recent version of the target ontology."
         * );
         * 
         * }
         * 
         * } else { // process JSON as usual
         * 
         * }
         */

        /*
         * Note: only entries with a list attribute are retrieved
         */
        // "$.success.data[0].classBean.relations[0].entry[?(@.list)]");
        Object queriedResourceRelationsObject = get(
                get(get(get(
                        get(get(get(super.parse(json), "success"), "data"), 0),
                        "classBean"), "relations"), 0), "entry");
        LightweightList<Object> queriedResourceRelations = CollectionFactory
                .createLightweightList();
        for (int i = 0; i < length(queriedResourceRelationsObject); i++) {
            Object object = get(queriedResourceRelationsObject, i);
            if (has(object, "list")) {
                queriedResourceRelations.add(object);
            }
        }

        for (int i = 0; i < queriedResourceRelations.size(); i++) {
            Object entry = queriedResourceRelations.get(i);

            String relationType = asString(get(entry, "string"));

            boolean classRelation = "SubClass".equals(relationType)
                    || "SuperClass".equals(relationType);
            boolean compositeRelation = "has_part".equals(relationType)
                    || "[R]has_part".equals(relationType);
            boolean reversed = "SuperClass".equals(relationType)
                    || "[R]has_part".equals(relationType);

            if (relationType == null || !(classRelation || compositeRelation)) {
                /*
                 * XXX OBO relations (such as 'negatively_regulates', '[R]is_a')
                 * get ignored
                 */
                continue;
            }

            Object entryListContents = get(get(get(entry, "list"), 0),
                    "classBean"); // "$.list[0].classBean"
            if (entryListContents == null || !isArray(entryListContents)) {
                // if there is just one classbean it is not stored in an array
                // XXX CLEAN THIS UP
                // JsonItem item = getItem(entry.stringValue(),
                // "$.list[0].classBean");
                if (entryListContents == null // TODO ! isObject
                        || asString(get(entryListContents, "id")).equals(
                                OWL_THING)) {
                    continue;
                }
                Resource neighbour = process(entryListContents, reversed,
                        compositeRelation, ontologyId, parentConcepts,
                        childConcepts, owningConcepts, ownedConcepts);
                resources.add(neighbour);
                continue;
            }

            for (int j = 0; j < length(entryListContents); j++) {
                Object relation = get(entryListContents, j);

                if (asString(get(relation, "id")).equals(OWL_THING)) {
                    // don't include owl:Thing as a neighbour
                    continue;
                }

                Resource neighbour = process(relation, reversed,
                        compositeRelation, ontologyId, parentConcepts,
                        childConcepts, owningConcepts, ownedConcepts);
                resources.add(neighbour);
            }
        }

        Map<String, Serializable> partialProperties = CollectionFactory
                .createStringMap();
        partialProperties.put(Concept.PARENT_CONCEPTS, parentConcepts);
        partialProperties.put(Concept.CHILD_CONCEPTS, childConcepts);
        partialProperties.put(Concept.OWNED_CONCEPTS, ownedConcepts);
        partialProperties.put(Concept.OWNING_CONCEPTS, owningConcepts);

        return new ResourceNeighbourhood(partialProperties, resources);
    }

    public Resource parseResource(String ontologyAcronym, String json) {
        Object queriedResource = get(
                get(get(get(super.parse(json), "success"), "data"), 0),
                "classBean");

        String fullConceptId = asString(get(queriedResource, "fullId"));
        String shortConceptId = asString(get(queriedResource, "id"));
        String label = asString(get(queriedResource, "label"));

        Resource resource = new Resource(Concept.toConceptURI(ontologyAcronym,
                fullConceptId));
        resource.putValue(Concept.FULL_ID, fullConceptId);
        resource.putValue(Concept.SHORT_ID, shortConceptId);
        resource.putValue(Concept.LABEL, label);
        resource.putValue(Concept.ONTOLOGY_ACRONYM, ontologyAcronym);

        ResourceNeighbourhood neighbourhood = parseNeighbourhood(
                ontologyAcronym, json);
        resource.applyPartialProperties(neighbourhood.getPartialProperties());

        return resource;
    }

    private Resource process(Object relation, boolean reversed,
            boolean hasARelation, String ontologyAcronym,
            UriList parentConcepts, UriList childConcepts,
            UriList owningConcepts, UriList ownedConcepts) {

        String conceptId = asString(get(relation, "fullId"));
        String conceptShortId = asString(get(relation, "id"));
        String label = asString(get(relation, "label"));

        int childCount = 0;
        Object relationsEntry = get(get(get(relation, "relations"), 0), "entry");

        if (relationsEntry != null) {
            Object entryString = get(relationsEntry, "string");
            if (entryString != null
                    && asString(entryString).equals("ChildCount")) {
                childCount = asInt(get(relationsEntry, "int"));
            }
        }

        Resource concept = new Resource(Concept.toConceptURI(ontologyAcronym,
                conceptId));

        concept.putValue(Concept.FULL_ID, conceptId);
        concept.putValue(Concept.SHORT_ID, conceptShortId);
        concept.putValue(Concept.LABEL, label);
        concept.putValue(Concept.ONTOLOGY_ACRONYM, ontologyAcronym);
        concept.putValue(Concept.CONCEPT_CHILD_COUNT,
                Integer.valueOf(childCount));

        if (hasARelation && reversed) {
            owningConcepts.add(concept.getUri());
        } else if (hasARelation && !reversed) {
            ownedConcepts.add(concept.getUri());
        } else if (!hasARelation && reversed) {
            parentConcepts.add(concept.getUri());
        } else { // !hasARelation && !reversed
            childConcepts.add(concept.getUri());
        }

        return concept;
    }

}
