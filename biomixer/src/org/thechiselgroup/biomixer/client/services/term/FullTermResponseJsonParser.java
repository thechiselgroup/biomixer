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
import org.thechiselgroup.biomixer.client.services.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonArray;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonItem;
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
        List<Resource> resources = new ArrayList<Resource>();

        /*
         * Note: only entries with a list attribute are retrieved
         */
        JsonArray queriedResourceRelations = getArray(json,
                "$.success.data[0].classBean.relations[0].entry[?(@.list)]");
        for (int i = 0; i < queriedResourceRelations.size(); i++) {
            JsonItem entry = queriedResourceRelations.get(i);

            String relationType = getString(entry, "$.string");
            if (relationType == null
                    || !("SubClass".equals(relationType) || "SuperClass"
                            .equals(relationType))) {
                /*
                 * XXX OBO relations (such as 'negatively_regulates', '[R]is_a')
                 * get ignored
                 */
                continue;
            }

            JsonArray entryListContents = getArray(entry, "$.list[0].classBean");
            if (entryListContents == null) {
                // if there is just one classbean it is not stored in an array
                // XXX CLEAN THIS UP
                JsonItem item = getItem(entry.stringValue(),
                        "$.list[0].classBean");
                if (item == null || getString(item, "$.id").equals(OWL_THING)) {
                    continue;
                }
                Resource neighbour = process(item,
                        "SuperClass".equals(relationType), ontologyId,
                        parentConcepts, childConcepts);
                resources.add(neighbour);
                continue;
            }

            for (int j = 0; j < entryListContents.size(); j++) {
                JsonItem relation = entryListContents.get(j);

                if (getString(relation, "$.id").equals(OWL_THING)) {
                    // don't include owl:Thing as a neighbour
                    continue;
                }

                Resource neighbour = process(relation,
                        "SuperClass".equals(relationType), ontologyId,
                        parentConcepts, childConcepts);
                resources.add(neighbour);
            }
        }

        Map<String, Serializable> partialProperties = CollectionFactory
                .createStringMap();
        partialProperties.put(Concept.PARENT_CONCEPTS, parentConcepts);
        partialProperties.put(Concept.CHILD_CONCEPTS, childConcepts);

        return new ResourceNeighbourhood(partialProperties, resources);
    }

    public Resource parseResource(String ontologyId, String json) {
        JsonItem queriedResource = getItem(json, "$.success.data[0].classBean");

        String fullConceptId = getString(queriedResource, "$.fullId");
        String shortConceptId = getString(queriedResource, "$.id");
        String label = getString(queriedResource, "$.label");

        Resource resource = new Resource(Concept.toConceptURI(ontologyId,
                fullConceptId));
        resource.putValue(Concept.FULL_ID, fullConceptId);
        resource.putValue(Concept.SHORT_ID, shortConceptId);
        resource.putValue(Concept.LABEL, label);
        resource.putValue(Concept.VIRTUAL_ONTOLOGY_ID, ontologyId);

        ResourceNeighbourhood neighbourhood = parseNeighbourhood(ontologyId,
                json);
        resource.applyPartialProperties(neighbourhood.getPartialProperties());

        return resource;
    }

    private Resource process(JsonItem relation, boolean reversed,
            String ontologyId, UriList parentConcepts, UriList childConcepts) {

        String conceptId = getString(relation, "$.fullId");
        String conceptShortId = getString(relation, "$.id");
        String label = getString(relation, "$.label");

        int childCount = 0;
        JsonItem relationsEntry = getItem(relation.stringValue(),
                "$.relations[0].entry");
        String entryString = getString(relationsEntry, "$.string");
        if (entryString != null && entryString.equals("ChildCount")) {
            childCount = Integer.parseInt(getString(relationsEntry, "$.int"));
        }

        Resource concept = new Resource(Concept.toConceptURI(ontologyId,
                conceptId));

        concept.putValue(Concept.FULL_ID, conceptId);
        concept.putValue(Concept.SHORT_ID, conceptShortId);
        concept.putValue(Concept.LABEL, label);
        concept.putValue(Concept.VIRTUAL_ONTOLOGY_ID, ontologyId);
        concept.putValue(Concept.CONCEPT_CHILD_COUNT,
                Integer.valueOf(childCount));

        if (reversed) {
            parentConcepts.add(concept.getUri());
        } else {
            childConcepts.add(concept.getUri());
        }

        return concept;
    }

}
