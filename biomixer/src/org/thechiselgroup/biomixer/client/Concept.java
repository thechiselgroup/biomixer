/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;

/**
 * Concept URIs are composed of the virtual ontology id and the encoded full id
 * of the concept. This utility class contains methods to work with concept
 * URIs.
 * 
 * @author Lars Grammel
 */
public final class Concept {

    public static final String RESOURCE_URI_PREFIX = "ncbo-concept";

    @Deprecated
    public static final String OLD_FULL_ID = "fullId";

    public static final String ID = "id";

    @Deprecated
    public static final String OLD_SHORT_ID = "shortId";

    public static final String LABEL = "label";

    public static final String TYPE = "type";

    @Deprecated
    public static final String OLD_VIRTUAL_ONTOLOGY_ID = "virtualOntologyId";

    public static final String ONTOLOGY_ACRONYM = "parentOntologyAcronym";

    public static final String UI_LABEL = "ui_label";

    /**
     * URIs of mapping where this concept is the source.
     */
    public static final String OUTGOING_MAPPINGS = "outgoingMappings";

    /**
     * URIs of mapping where this concept is the target.
     */
    public static String INCOMING_MAPPINGS = "incomingMappings";

    public static final String ONTOLOGY_VERSION_ID = "ontologyVersionId";

    @Deprecated
    // Now that we have ontology acronym, it seems ok to use that and not do all
    // the extra REST calls necessary to get the ontology names.
    public static final String CONCEPT_ONTOLOGY_NAME = "ontologyName";

    public static final String CHILD_CONCEPTS = "childConcepts";

    // is-a
    public static final String PARENT_CONCEPTS = "parentConcepts";

    // has-a
    public static final String HAS_PART_CONCEPTS = "hasPartConcepts";

    public static final String PART_OF_CONCEPTS = "partOfConcepts";

    public static Resource createConceptResource(String ontologyAcronym,
            String conceptId, String conceptLabel) {

        Resource concept = Resource.createIndexedResource(Concept.toConceptURI(
                ontologyAcronym, conceptId));

        concept.putValue(Concept.ID, conceptId);
        concept.putValue(Concept.ONTOLOGY_ACRONYM, ontologyAcronym);
        concept.putValue(Concept.LABEL, conceptLabel);
        concept.putValue(Concept.UI_LABEL, constructUiLabel(concept));

        return concept;
    }

    public static String constructUiLabel(Resource concept) {
        return getLabel(concept) + " (" + getOntologyAcronym(concept) + ")";
        // + " (" + getConceptId(concept) + ")";
    }

    public static String getConceptId(Resource resource) {
        return getConceptId(resource.getUri());
    }

    public static String getConceptId(String conceptURI) {
        return conceptURI.substring(conceptURI.indexOf('/') + 1);
    }

    public static String getFullId(Resource concept) {
        assert isConcept(concept);
        return (String) concept.getValue(ID);
    }

    public static String getLabel(Resource concept) {
        assert isConcept(concept);
        return (String) concept.getValue(LABEL);
    }

    public static String getOntologyAcronym(Resource concept) {
        assert isConcept(concept);
        return (String) concept.getProperties().get(Concept.ONTOLOGY_ACRONYM);
    }

    public static boolean isConcept(Resource resource) {
        return resource.getUri().startsWith(Concept.RESOURCE_URI_PREFIX);
    }

    // TODO use encoded long full id
    public static String toConceptURI(String ontologyId, String fullId) {
        return Concept.RESOURCE_URI_PREFIX + ":" + ontologyId + "/" + fullId;
    }

    private Concept() {
    }

    public static List<String> asUris(String ontologyAcronym,
            String... conceptIds) {
        List<String> uris = new ArrayList<String>();
        for (String conceptId : conceptIds) {
            uris.add(toConceptURI(ontologyAcronym, conceptId));
        }
        return uris;
    }

    static public class ConceptDataTypeValidator implements DataTypeValidator {
        @Override
        public boolean validateDataTypes(ResourceSet resourceSet) {
            for (Resource resource : resourceSet) {
                if (!Concept.isConcept(resource)) {
                    return false;
                }
            }
            return true;
        }
    }
}