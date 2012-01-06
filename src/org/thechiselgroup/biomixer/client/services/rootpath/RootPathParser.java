package org.thechiselgroup.biomixer.client.services.rootpath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourcePath;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.XPathEvaluationException;

import com.google.inject.Inject;

public class RootPathParser extends AbstractXMLResultParser {

    private Resource target;

    @Inject
    public RootPathParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);
    }

    // XXX duplicated from FullTermResponseParser
    private String getConceptId(Object r) throws XPathEvaluationException {
        return getText(r, "fullId/text()");
    }

    public ResourcePath parse(String ontologyId, String conceptId,
            String xmlText) throws Exception {

        target = null;

        Object rootNode = parseDocument(xmlText);

        // This is the top level node, i.e. "Thing"
        Object[] nodes = getNodes(rootNode, "//success/data/classBean");

        assert nodes.length == 1;

        List<Resource> resources = new ArrayList<Resource>();

        List<Object> processLater = new ArrayList<Object>();

        List<String> subclassOrSuperclassConceptIds = new ArrayList<String>();

        // Need each node of each layer to manage its list of parents and
        // children

        traverseLayer(ontologyId, conceptId, nodes[0], resources, processLater,
                subclassOrSuperclassConceptIds, null);

        for (Object n : processLater) {
            if (subclassOrSuperclassConceptIds.contains(getConceptId(n))) {
                continue;
            }

            process(n, ontologyId, new UriList(), new UriList());
        }

        assert target != null;

        return new ResourcePath(target, resources);
    }

    private Resource process(Object node, String ontologyId,
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

        Resource concept = new Resource(Concept.toConceptURI(ontologyId,
                conceptId));

        concept.putValue(Concept.FULL_ID, conceptId);
        concept.putValue(Concept.SHORT_ID, conceptShortId);
        concept.putValue(Concept.LABEL, label);
        concept.putValue(Concept.ONTOLOGY_ID, ontologyId);
        concept.putValue(Concept.CONCEPT_CHILD_COUNT,
                Integer.valueOf(childCount));

        // TODO: parent concepts?
        childConcepts.add(concept.getUri());

        return concept;

    }

    private void traverseLayer(String ontologyId, String conceptId,
            Object entryNode, List<Resource> resources,
            List<Object> processLater,
            List<String> subclassOrSuperclassConceptIds, Resource previous)
            throws XPathEvaluationException {

        Object[] nodes = getNodes(entryNode, "relations/entry");

        if (nodes.length == 0) {
            /* End of branch */
            return;
        }

        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i];

            Object[] relationships = getNodes(node, "list/classBean");

            if (relationships.length == 0) {
                continue;
            }

            // Move parents and child concepts uri list here and don't pass it
            // along to next layer
            UriList parentConcepts = new UriList();
            UriList childConcepts = new UriList();
            Map<String, Serializable> partialPropertiesForPrevious = CollectionFactory
                    .createStringMap();
            Map<String, Serializable> partialPropertiesForChildren = CollectionFactory
                    .createStringMap();

            if (previous != null) {
                parentConcepts.add(previous.getUri());
                partialPropertiesForChildren.put(Concept.CHILD_CONCEPTS,
                        parentConcepts);
                // apply this to each child found
            }

            String name = getText(node, "string/text()");
            // TODO deal with reversed names

            for (int j = 0; j < relationships.length; j++) {
                Object relationship = relationships[j];

                if (!("SubClass".equals(name) || "SuperClass".equals(name))) {
                    processLater.add(relationship);
                    continue;
                }

                Resource resource = process(relationship, ontologyId,
                        parentConcepts, childConcepts);

                resource.applyPartialProperties(partialPropertiesForChildren);
                childConcepts.add(resource.getUri());

                if (!resource.getValue(Concept.FULL_ID).equals(conceptId)) {
                    resources.add(resource);
                    subclassOrSuperclassConceptIds.add(Concept
                            .getFullId(resource));
                } else {
                    /* Found the node with specified concept id */
                    target = resource;
                }

                /* Check for greater depths to traverse */
                traverseLayer(ontologyId, conceptId, relationship, resources,
                        processLater, subclassOrSuperclassConceptIds, resource);
            }

            if (previous != null) {
                partialPropertiesForPrevious.put(Concept.PARENT_CONCEPTS,
                        childConcepts);
                previous.applyPartialProperties(partialPropertiesForPrevious);
            }

        }
    }
}
