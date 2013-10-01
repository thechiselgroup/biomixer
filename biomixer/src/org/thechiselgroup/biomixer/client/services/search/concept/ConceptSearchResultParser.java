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
package org.thechiselgroup.biomixer.client.services.search.concept;

import java.util.HashSet;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.XPathEvaluationException;

import com.google.inject.Inject;

public class ConceptSearchResultParser extends AbstractXMLResultParser {

    private static final String ROOT_EXPRESSION = "//success/data/page/contents/searchResultList/searchBean";

    @Inject
    public ConceptSearchResultParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);
    }

    protected Resource analyzeNode(Object node) throws XPathEvaluationException {
        String conceptShortId = getText(node, "conceptIdShort/text()");
        String ontologyId = getText(node, "ontologyId/text()");
        String conceptId = getText(node, "conceptId/text()");

        Resource concept = new Resource(Concept.toConceptURI(ontologyId,
                conceptId));

        concept.putValue(Concept.FULL_ID, conceptId);
        concept.putValue(Concept.SHORT_ID, conceptShortId);
        concept.putValue(Concept.LABEL, getText(node, "preferredName/text()"));
        // TODO XXX Not changed yet
        concept.putValue(Concept.ONTOLOGY_ACRONYM, ontologyId);
        concept.putValue(Concept.CONCEPT_ONTOLOGY_NAME,
                getText(node, "ontologyDisplayLabel/text()"));

        return concept;
    }

    protected Set<Resource> parse(String xmlText) throws Exception,
            XPathEvaluationException {

        Object rootNode = parseDocument(xmlText);

        Set<Resource> resources = new HashSet<Resource>();

        Object[] nodes = getNodes(rootNode, ROOT_EXPRESSION);
        for (Object node : nodes) {
            resources.add(analyzeNode(node));
        }
        return resources;
    }

}