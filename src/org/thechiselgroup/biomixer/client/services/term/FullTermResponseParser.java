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
import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.UriList;
import org.thechiselgroup.choosel.core.client.util.collections.CollectionFactory;
import org.thechiselgroup.choosel.visualization_component.graph.client.ResourceNeighbourhood;
import org.thechiselgroup.choosel.workbench.shared.util.xml.DocumentProcessor;
import org.thechiselgroup.choosel.workbench.shared.util.xml.XPathEvaluationException;

import com.google.inject.Inject;

public class FullTermResponseParser extends AbstractXMLResultParser {

    private static final String REVERSE_PREFIX = "[R]";

    @Inject
    public FullTermResponseParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);
    }

    private String getConceptId(Object r) throws XPathEvaluationException {
        return getText(r, "fullId/text()");
    }

    public ResourceNeighbourhood parse(String ontologyId, String xmlText)
            throws Exception {

        Object rootNode = parseDocument(xmlText);

        Object[] nodes = getNodes(rootNode,
                "//success/data/classBean/relations/entry");

        List<Object> processLater = new ArrayList<Object>();
        List<Object> reversedNodes = new ArrayList<Object>();
        List<String> subclassOrSuperclassConceptIds = new ArrayList<String>();

        UriList parentConcepts = new UriList();
        UriList childConcepts = new UriList();
        List<Resource> resources = new ArrayList<Resource>();

        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i];

            // (1) test list/classbean size
            Object[] relationships = getNodes(node, "list/classBean");

            if (relationships.length == 0) {
                continue;
            }

            // (2) name, check for inverted
            String name = getText(node, "string/text()");
            boolean reversed = name.startsWith(REVERSE_PREFIX);
            if (reversed) {
                name = name.substring(REVERSE_PREFIX.length());
            }

            for (int j = 0; j < relationships.length; j++) {
                Object r = relationships[j];

                if (reversed) {
                    reversedNodes.add(r);
                }

                if (!("SubClass".equals(name) || "SuperClass".equals(name))) {
                    processLater.add(r);
                    continue;
                }

                Resource neighbour = process(r, "SuperClass".equals(name),
                        ontologyId, parentConcepts, childConcepts);

                resources.add(neighbour);

                subclassOrSuperclassConceptIds
                        .add(Concept.getFullId(neighbour));
            }

        }

        for (Object n : processLater) {
            if (subclassOrSuperclassConceptIds.contains(getConceptId(n))) {
                continue;
            }

            process(n, reversedNodes.contains(n), ontologyId, parentConcepts,
                    childConcepts);
        }

        Map<String, Serializable> partialProperties = CollectionFactory
                .createStringMap();
        partialProperties.put(Concept.PARENT_CONCEPTS, parentConcepts);
        partialProperties.put(Concept.CHILD_CONCEPTS, childConcepts);

        return new ResourceNeighbourhood(partialProperties, resources);
    }

    private Resource process(Object node, boolean reversed, String ontologyId,
            UriList parentConcepts, UriList childConcepts)
            throws XPathEvaluationException {

        String conceptId = getConceptId(node);
        String conceptShortId = getText(node, "id/text()");
        String label = getText(node, "label/text()");

        int childCount = 0;
        Object[] nodes = getNodes(node,
                "relations/entry[string/text()=\"ChildCount\"]");
        if (nodes.length > 0) {
            childCount = Integer.parseInt(getText(nodes[0], "int/text()"));
        }

        // retrieve & create concept + relationship
        Resource concept = new Resource(Concept.toConceptURI(ontologyId,
                conceptId));

        concept.putValue(Concept.FULL_ID, conceptId);
        concept.putValue(Concept.SHORT_ID, conceptShortId);
        concept.putValue(Concept.LABEL, label);
        concept.putValue(Concept.ONTOLOGY_ID, ontologyId);
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