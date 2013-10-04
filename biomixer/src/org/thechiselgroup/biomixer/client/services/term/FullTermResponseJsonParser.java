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
import java.util.Set;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
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

    private final TermWithoutRelationshipsJsonParser termParser;

    @Inject
    public FullTermResponseJsonParser(JsonParser jsonParser,
            TermWithoutRelationshipsJsonParser termParser) {
        super(jsonParser);
        this.termParser = termParser;
    }

    // Parses results of class call with "parents" or "children" arguments:
    // http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F35146001/parents/?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&callback=__gwt_jsonp__.P4.onSuccess
    // http://data.bioontology.org/ontologies/SNOMEDCT/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FSNOMEDCT%2F35146001/children/?apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a&callback=__gwt_jsonp__.P4.onSuccess
    public ResourceNeighbourhood parseNewChildren(String ontologyAcronym,
            String json) {
        UriList parentConcepts = new UriList();
        UriList childConcepts = new UriList();
        UriList owningConcepts = new UriList();
        UriList ownedConcepts = new UriList();
        List<Resource> resources = new ArrayList<Resource>();

        // TODO What if I get more pages? This seems to low to redispatch to get
        // more...but I have links, so I could write another fetch call in here
        // and keep using the original callback after it is adjusted for
        // multiple passes to get data.

        Integer pageNumber = asInt(get(super.parse(json), "page"));
        Integer maxPageNumber = asInt(get(super.parse(json), "pageCount"));
        Object collectionArray = get(super.parse(json), "collection");
        // Make resources for all the children
        Integer numChildren = length(collectionArray);
        for (int i = 0; i < numChildren; i++) {
            Object child = get(collectionArray, i);

            // I can't seem to find how to get composition relations fromt he
            // new API. Emailed Paul about it.
            // Until I know this, just make use of the parent and child
            // relations.
            boolean classRelation = true;
            boolean compositeRelation = false;
            boolean superOfTarget = false;

            // String relationType = asString(get(entry, "string"));
            // boolean classRelation = "SubClass".equals(relationType)
            // || "SuperClass".equals(relationType);
            // boolean compositeRelation = "has_part".equals(relationType)
            // || "[R]has_part".equals(relationType);
            // boolean reversed = "SuperClass".equals(relationType)
            // || "[R]has_part".equals(relationType);

            // The process method creates the resource and registers the
            // relations.
            Resource neighbour = processImmediateNeighbours(child,
                    superOfTarget, compositeRelation, ontologyAcronym,
                    parentConcepts, childConcepts, owningConcepts,
                    ownedConcepts);
            if (null != neighbour) {
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

    // Would refactor to combine with parseNewChidlren, but the looping differs
    // due to a json parsing glitch (string integers vs integers)
    public ResourceNeighbourhood parseNewParents(String ontologyAcronym,
            String json) {
        UriList parentConcepts = new UriList();
        UriList childConcepts = new UriList();
        UriList owningConcepts = new UriList();
        UriList ownedConcepts = new UriList();
        List<Resource> resources = new ArrayList<Resource>();

        // Note that there are no pages for parents
        Object parentsArray = super.parse(json);

        // Make resources for all the parents
        // Integer numParents = length(parentsArray);
        // Having a problem (again) with implicit array indices being turned
        // into quoted string integers.
        Set<String> quotedStringIndices = getObjectProperties(parentsArray);

        // for (int i = 0; i < numParents; i++) {
        for (String index : quotedStringIndices) {
            Object parent = get(parentsArray, index);

            // I can't seem to find how to get composition relations from the
            // new API. Emailed Paul about it.
            // Until I know this, just make use of the parent and child
            // relations.
            boolean classRelation = true;
            boolean compositeRelation = false;
            boolean superOfTarget = true;

            // String relationType = asString(get(entry, "string"));
            // boolean classRelation = "SubClass".equals(relationType)
            // || "SuperClass".equals(relationType);
            // boolean compositeRelation = "has_part".equals(relationType)
            // || "[R]has_part".equals(relationType);
            // boolean reversed = "SuperClass".equals(relationType)
            // || "[R]has_part".equals(relationType);

            // The process method creates the resource and registers the
            // relations.
            Resource neighbour = processImmediateNeighbours(parent,
                    superOfTarget, compositeRelation, ontologyAcronym,
                    parentConcepts, childConcepts, owningConcepts,
                    ownedConcepts);
            if (null != neighbour) {
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

    public ResourceNeighbourhood parseNewPathsToRoot(String ontologyAcronym,
            String json) {
        UriList parentConcepts = new UriList();
        UriList childConcepts = new UriList();
        UriList owningConcepts = new UriList();
        UriList ownedConcepts = new UriList();
        List<Resource> resources = new ArrayList<Resource>();

        // Note that there are no pages for paths_to_root
        Object arrayOfPathArrays = super.parse(json);
        Set<String> outerKeys = getObjectProperties(arrayOfPathArrays);

        for (String pathIndex : outerKeys) {
            Object collectionArray = get(arrayOfPathArrays, pathIndex);

            boolean immediateParent = false;
            // Make resources for all the children
            Integer numPathSteps = length(collectionArray);

            // There are all sorts that get parsed multiple times, any
            // time prior to a root to path branch.
            for (int i = 0; i < numPathSteps; i++) {
                Object ancestor = get(collectionArray, i);

                boolean classRelation = true;
                boolean compositeRelation = false;
                boolean superOfTarget = true;
                // On the second last element, we are on the parent of the
                // target resource,
                // since these paths provide the target as the final element.
                if (numPathSteps - 2 == i) {
                    immediateParent = true;
                }

                // String relationType = asString(get(entry, "string"));
                // boolean classRelation = "SubClass".equals(relationType)
                // || "SuperClass".equals(relationType);
                // boolean compositeRelation = "has_part".equals(relationType)
                // || "[R]has_part".equals(relationType);
                // boolean reversed = "SuperClass".equals(relationType)
                // || "[R]has_part".equals(relationType);

                // The process method creates the resource and registers the
                // relations.

                Resource neighbour = processNeighbour(ancestor,
                        immediateParent, superOfTarget, compositeRelation,
                        ontologyAcronym, parentConcepts, childConcepts,
                        owningConcepts, ownedConcepts);
                if (null != neighbour) {
                    resources.add(neighbour);
                }
                immediateParent = false;

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

    private Resource processImmediateNeighbours(Object relatedTerm,
            boolean superOfTarget, boolean hasARelation,
            String ontologyAcronym, UriList parentConcepts,
            UriList childConcepts, UriList owningConcepts, UriList ownedConcepts) {

        return processNeighbour(relatedTerm, true, superOfTarget, hasARelation,
                ontologyAcronym, parentConcepts, childConcepts, owningConcepts,
                ownedConcepts);
    }

    private Resource processNeighbour(Object relatedTerm,
            boolean immediateNeighbour, boolean superOfTarget,
            boolean hasARelation, String ontologyAcronym,
            UriList parentConcepts, UriList childConcepts,
            UriList owningConcepts, UriList ownedConcepts) {

        // This is so different now, because it seems that relations are not
        // directly represented as they were in the old API.
        Resource concept = termParser
                .parseConcept(ontologyAcronym, relatedTerm);
        if (((String) concept.getValue(Concept.ID))
                .contains("ontologies/umls/OrphanClass")) {
            return null;
        }
        if (immediateNeighbour) {
            if (hasARelation && superOfTarget) {
                owningConcepts.add(concept.getUri());
            } else if (hasARelation && !superOfTarget) {
                ownedConcepts.add(concept.getUri());
            } else if (!hasARelation && superOfTarget) {
                parentConcepts.add(concept.getUri());
            } else { // !hasARelation && !reversed
                childConcepts.add(concept.getUri());
            }
        }

        return concept;
    }

}
