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
        List<Resource> resources = new ArrayList<Resource>();

        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i];

            // (1) test list/classbean size
            Object[] relationships = getNodes(node, "list/classBean");

            if (relationships.length == 0) {
                continue;
            }

            String name = getText(node, "string/text()");
            for (int j = 0; j < relationships.length; j++) {
                Object r = relationships[j];

                if (!("SubClass".equals(name) || "SuperClass".equals(name))) {
                    // XXX OBO relations (such as 'negatively_regulates',
                    // '[R]is_a') get ignored
                    continue;
                }

                if (getText(r, "id/text()").equals(OWL_THING)) {
                    // don't include owl:Thing as a neighbour
                    continue;
                }

                Resource neighbour = process(r, "SuperClass".equals(name),
                        ontologyId, parentConcepts, childConcepts);

                resources.add(neighbour);
            }

        }

        Map<String, Serializable> partialProperties = CollectionFactory
                .createStringMap();
        partialProperties.put(Concept.PARENT_CONCEPTS, parentConcepts);
        partialProperties.put(Concept.CHILD_CONCEPTS, childConcepts);

        return new ResourceNeighbourhood(partialProperties, resources);
    }

    public Resource parseResource(String ontologyId, String xmlText)
            throws Exception {

        Object rootNode = parseDocument(xmlText);

        Object[] nodes = getNodes(rootNode, "//success/data/classBean");
        assert nodes.length == 1;

        Object node = nodes[0];

        String fullConceptId = getConceptId(node);
        String shortConceptId = getText(node, "id/text()");
        String label = getText(node, "label/text()");

        Resource resource = new Resource(Concept.toConceptURI(ontologyId,
                fullConceptId));
        resource.putValue(Concept.FULL_ID, fullConceptId);
        resource.putValue(Concept.SHORT_ID, shortConceptId);
        resource.putValue(Concept.LABEL, label);
        resource.putValue(Concept.VIRTUAL_ONTOLOGY_ID, ontologyId);

        ResourceNeighbourhood neighbourhood = parseNeighbourhood(ontologyId,
                xmlText);
        resource.applyPartialProperties(neighbourhood.getPartialProperties());

        return resource;
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