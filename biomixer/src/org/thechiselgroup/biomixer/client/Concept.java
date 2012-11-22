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

    public static final String FULL_ID = "fullId";

    public static final String SHORT_ID = "shortId";

    public static final String LABEL = "label";

    public static final String TYPE = "type";

    public static final String VIRTUAL_ONTOLOGY_ID = "virtualOntologyId";

    /**
     * URIs of mapping where this concept is the source.
     */
    public static final String OUTGOING_MAPPINGS = "outgoingMappings";

    /**
     * URIs of mapping where this concept is the target.
     */
    public static String INCOMING_MAPPINGS = "incomingMappings";

    public static final String ONTOLOGY_VERSION_ID = "ontologyVersionId";

    public static final String CONCEPT_ONTOLOGY_NAME = "ontologyName";

    public static final String CONCEPT_CHILD_COUNT = "childCount";

    public static final String CHILD_CONCEPTS = "childConcepts";

    // is-a
    public static final String PARENT_CONCEPTS = "parentConcepts";

    // has-a
    public static final String OWNING_CONCEPTS = "ownerConcepts";

    public static final String OWNED_CONCEPTS = "ownedConcepts";

    // TODO change to full id
    public static Resource createConceptResource(String ontologyId,
            String conceptId) {

        Resource concept = new Resource(Concept.toConceptURI(ontologyId,
                conceptId));

        // XXX
        concept.putValue(Concept.SHORT_ID, conceptId);
        concept.putValue(Concept.FULL_ID, conceptId);
        concept.putValue(Concept.VIRTUAL_ONTOLOGY_ID, ontologyId);

        return concept;
    }

    public static String getConceptId(Resource resource) {
        return getConceptId(resource.getUri());
    }

    public static String getConceptId(String conceptURI) {
        return conceptURI.substring(conceptURI.indexOf('/') + 1);
    }

    public static String getFullId(Resource concept) {
        assert isConcept(concept);
        return (String) concept.getValue(FULL_ID);
    }

    public static String getOntologyId(Resource resource) {
        return getOntologyId(resource.getUri());
    }

    public static String getOntologyId(String conceptURI) {
        return conceptURI.substring(RESOURCE_URI_PREFIX.length() + 1,
                conceptURI.indexOf('/'));
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

    public static List<String> asUris(String virtualOntologyId,
            String... conceptIds) {
        List<String> uris = new ArrayList<String>();
        for (String conceptId : conceptIds) {
            uris.add(toConceptURI(virtualOntologyId, conceptId));
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