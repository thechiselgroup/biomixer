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
package org.thechiselgroup.biomixer.client.services.term;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.XPathEvaluationException;

import com.google.inject.Inject;

// This is for old API, and we prefer json to XML, since we use JSONP fro cross domain data.
@Deprecated
public class FullTermResponseParser extends AbstractXMLResultParser {

    private static final String OWL_THING = "owl:Thing";

    @Inject
    public FullTermResponseParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);
    }

    private String getConceptId(Object r) throws XPathEvaluationException {
        return getText(r, "fullId/text()");
    }

    public ResourceNeighbourhood parseNeighbourhood(String ontologyId,
            String xmlText) throws Exception {

        Object rootNode = parseDocument(xmlText);

        Object[] nodes = getNodes(rootNode,
                "//success/data/classBean/relations/entry");

        UriList parentConcepts = new UriList();
        UriList childConcepts = new UriList();
        UriList owningConcepts = new UriList();
        UriList ownedConcepts = new UriList();
        List<Resource> resources = new ArrayList<Resource>();

        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i];

            // (1) test list/classbean size
            Object[] relationships = getNodes(node, "list/classBean");

            if (relationships.length == 0) {
                continue;
            }
            for (int j = 0; j < relationships.length; j++) {
                Object r = relationships[j];

                String name = getText(node, "string/text()");

                boolean classRelation = "SubClass".equals(name)
                        || "SuperClass".equals(name);
                boolean compositeRelation = "has_part".equals(name)
                        || "[R]has_part".equals(name);
                boolean reversed = "SuperClass".equals(name)
                        || "[R]has_part".equals(name);

                if (name == null || !(classRelation || compositeRelation)) {
                    // XXX OBO relations (such as 'negatively_regulates',
                    // '[R]is_a') get ignored
                    continue;
                }

                if (getText(r, "id/text()").equals(OWL_THING)) {
                    // don't include owl:Thing as a neighbour
                    continue;
                }

                Resource neighbour = process(r, reversed, compositeRelation,
                        ontologyId, parentConcepts, childConcepts,
                        owningConcepts, ownedConcepts);

                resources.add(neighbour);
            }

        }

        Map<String, Serializable> partialProperties = CollectionFactory
                .createStringMap();
        partialProperties.put(Concept.PARENT_CONCEPTS, parentConcepts);
        partialProperties.put(Concept.CHILD_CONCEPTS, childConcepts);
        partialProperties.put(Concept.OWNING_CONCEPTS, owningConcepts);
        partialProperties.put(Concept.OWNED_CONCEPTS, ownedConcepts);

        return new ResourceNeighbourhood(partialProperties, resources);
    }

    public Resource parseResource(String ontologyAcronym, String xmlText)
            throws Exception {

        Object rootNode = parseDocument(xmlText);

        Object[] nodes = getNodes(rootNode, "//success/data/classBean");
        assert nodes.length == 1;

        Object node = nodes[0];

        String fullConceptId = getConceptId(node);
        // String shortConceptId = getText(node, "id/text()");
        String label = getText(node, "label/text()");

        Resource resource = new Resource(Concept.toConceptURI(ontologyAcronym,
                fullConceptId));
        resource.putValue(Concept.ID, fullConceptId);
        resource.putValue(Concept.LABEL, label);
        resource.putValue(Concept.ONTOLOGY_ACRONYM, ontologyAcronym);

        ResourceNeighbourhood neighbourhood = parseNeighbourhood(
                ontologyAcronym, xmlText);
        resource.applyPartialProperties(neighbourhood.getPartialProperties());

        return resource;
    }

    private Resource process(Object node, boolean reversed,
            boolean hasARelation, String ontologyAcronym,
            UriList parentConcepts, UriList childConcepts,
            UriList owningConcepts, UriList ownedConcepts)
            throws XPathEvaluationException {

        String conceptId = getConceptId(node);
        // String conceptShortId = getText(node, "id/text()");
        String label = getText(node, "label/text()");

        int childCount = 0;
        Object[] nodes = getNodes(node,
                "relations/entry[string/text()=\"ChildCount\"]");
        if (nodes.length > 0) {
            childCount = Integer.parseInt(getText(nodes[0], "int/text()"));
        }

        // retrieve & create concept + relationship
        Resource concept = new Resource(Concept.toConceptURI(ontologyAcronym,
                conceptId));

        concept.putValue(Concept.ID, conceptId);
        concept.putValue(Concept.LABEL, label);
        concept.putValue(Concept.ONTOLOGY_ACRONYM, ontologyAcronym);
        // CHild count was never used and is no longer conveniently available in
        // the new API
        // concept.putValue(Concept.CONCEPT_CHILD_COUNT,
        // Integer.valueOf(childCount));

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