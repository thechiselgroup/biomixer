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

/**
 * Parses full term information for a concept.
 * 
 * NOTE: The JSON structure returned by the NCBO rest services is structured
 * oddly. Lists are used in places where a dictionary would make more sense. For
 * example, relationships are stored in a list with the relation type is at
 * index 0, and then the actual list of relationships in a list at index 1 of
 * the outer list.
 * 
 * To see specifics of the formatting, see the .json test files used by the unit
 * tests in {@link FullTermResponseJsonParserTest}.
 * 
 * @author drusk
 * 
 */
public class FullTermResponseJsonParser extends AbstractJsonResultParser {

    private static final String OWL_THING = "owl:Thing";

    public FullTermResponseJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    public ResourceNeighbourhood parseNeighbourhood(String ontologyId,
            String json) {
        UriList parentConcepts = new UriList();
        UriList childConcepts = new UriList();
        List<Resource> resources = new ArrayList<Resource>();

        JsonArray queriedResourceRelations = getArray(json,
                "$..success.data[0].relations");
        for (int i = 0; i < queriedResourceRelations.size(); i++) {
            /*
             * Relations are in a strange form: [relationType:
             * [relationContents]] ex: ["subClass", [{"id":?, ...}, ...]]
             */
            JsonArray relationTypeAndContents = queriedResourceRelations.get(i)
                    .asArray();

            if (relationTypeAndContents.size() <= 1) {
                continue;
            }

            assert relationTypeAndContents.size() == 2;

            String relationType = relationTypeAndContents.get(0).stringValue();
            if (!("SubClass".equals(relationType) || "SuperClass"
                    .equals(relationType))) {
                /*
                 * XXX OBO relations (such as 'negatively_regulates', '[R]is_a')
                 * get ignored
                 */
                continue;
            }

            JsonArray relationContents = relationTypeAndContents.get(1)
                    .asArray();
            for (int j = 0; j < relationContents.size(); j++) {
                JsonItem relation = relationContents.get(j);

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
        JsonItem queriedResource = getItem(json, "$..success.data[0]");

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
        JsonArray relationsArray = getArray(relation, "$.relations[0]");
        if (relationsArray.size() >= 1
                && relationsArray.getString(0).equals("ChildCount")) {
            childCount = Integer.parseInt(relationsArray.getString(1));
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
