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
 * Ontology URIs are composed of the virtual ontology id. This utility class
 * contains methods to work with ontology URIs.
 * 
 * TODO In progress, incomplete. See {@link Concept} class for additional
 * examples of attributes (e.g. childConcepts). The Ontology model is sparse
 * right now.
 * 
 * @author everbeek
 */
public final class Ontology {

    public static final String RESOURCE_URI_PREFIX = "ncbo-ontology";

    // Always leave as label, for CHoosel purposes
    public static final String ONTOLOGY_FULL_NAME = "label";

    public static final String ONTOLOGY_ACRONYM = "ontologyAcronym";

    public static final String ONTOLOGY_URI = "ontologyAbsoluteUri";

    public static final String TYPE = "type";

    // Unique URLs in the new API @id field, or the acronym, appear to be the
    // new unique identifiers. Integer identifiers don't seem to exist in the
    // new API, except for submission version numbers (the sequential
    // submissionId).
    @Deprecated
    public static final String OLD_VIRTUAL_ONTOLOGY_ID = "ontologyId";

    @Deprecated
    public static final String OLD_ONTOLOGY_VERSION_ID = "ontologyVersionId";

    public static final String DESCRIPTION = "description";

    public static final String NUMBER_OF_CLASSES = "numberOfClasses";

    public static final String NUMBER_OF_INDIVIDUALS = "numberOfIndividuals";

    public static final String NUMBER_OF_PROPERTIES = "numberOfProperties";

    public static final String NOTE = "note";

    public static final String UI_LABEL = "ui_label";

    // TODO Property that can be used to make link to ontology on bioportal

    /**
     * URIs of mapping where this ontology is the source. Have the other
     * ontology URI as well as the count in the URI value.
     */
    public static final String OUTGOING_MAPPINGS = "outgoingMappings";

    /**
     * URIs of mapping where this ontology is the target. Have the other
     * ontology URI as well as the count in the URI value.
     */
    public static String INCOMING_MAPPINGS = "incomingMappings";

    public static final String VIEWING_RESTRICTIONS = "viewingRestriction";

    public static final String VALUE_VIEWING_RESTRICTION_PRIVATE = "private";

    public static Resource createOntologyResource(String ontologyAcronym,
            String ontologyId, String ontologyName) {

        Resource ontology = Resource.createIndexedResource(Ontology
                .toOntologyURI(ontologyAcronym));

        // XXX
        ontology.putValue(Ontology.ONTOLOGY_ACRONYM, ontologyAcronym);
        ontology.putValue(Ontology.ONTOLOGY_URI, ontologyId);
        ontology.putValue(Ontology.ONTOLOGY_FULL_NAME, ontologyName);
        ontology.putValue(Ontology.UI_LABEL, constructUiLabel(ontology));

        return ontology;
    }

    public static String getOntologyId(Resource resource) {
        return getOntologyId(resource.getUri());
    }

    public static String getOntologyId(String ontologyArcURI) {
        return ontologyArcURI.substring(RESOURCE_URI_PREFIX.length() + 1,
                ontologyArcURI.indexOf('/'));
    }

    public static String getPureOntologyURI(String ontologyArcURI) {
        // Only needed to add this because I needed to add the count to the URI
        // of ontology mappings arcs. I do not like this.
        return ontologyArcURI.substring(0, ontologyArcURI.indexOf('/') + 1);
    }

    public static int getOntologyCount(String ontologyArcURI) {
        String count = ontologyArcURI
                .substring(ontologyArcURI.indexOf('/') + 1);
        if (null == count || count.isEmpty()) {
            return 1;
        } else {
            try {
                return Integer.parseInt(count);
            } catch (Exception e) {
                return 1;
            }
        }
    }

    public static String constructUiLabel(Resource ontology) {
        return ontology.getValue(ONTOLOGY_ACRONYM) + " ("
                + ontology.getValue(ONTOLOGY_FULL_NAME) + ")";
    }

    public static boolean isOntology(Resource resource) {
        return resource.getUri().startsWith(Ontology.RESOURCE_URI_PREFIX);
    }

    /**
     * Given the virtual ontology id, get the unique ontology URI. Remember that
     * the virtual id isn't always referred to as a virtual id in the REST
     * service results.
     * 
     * @param ontologyId
     * @return
     */
    public static String toOntologyURI(String ontologyId) {
        return Ontology.RESOURCE_URI_PREFIX + ":" + ontologyId + "/";
    }

    public static String toOntologyURIWithCount(String ontologyId,
            int relationCount) {
        return Ontology.RESOURCE_URI_PREFIX + ":" + ontologyId + "/"
                + relationCount;
    }

    private Ontology() {
    }

    public static List<String> asUris(String... ontologyAcronyms) {
        List<String> uris = new ArrayList<String>();
        for (String ontologyAcronym : ontologyAcronyms) {
            uris.add(toOntologyURI(ontologyAcronym));
        }
        return uris;
    }

    static public class OntologyDataTypeValidator implements DataTypeValidator {
        @Override
        public boolean validateDataTypes(ResourceSet resourceSet) {
            for (Resource resource : resourceSet) {
                if (!Ontology.isOntology(resource)) {
                    return false;
                }
            }
            return true;
        }
    }
}