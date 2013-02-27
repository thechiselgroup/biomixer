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
package org.thechiselgroup.biomixer.client.services.search.ontology;

import java.util.HashSet;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.XPathEvaluationException;

import com.google.inject.Inject;

/**
 * See {@ConceptSearchResultParser} for comparison.
 * 
 * @author everbeek
 * 
 */
public class OntologySearchResultParser extends AbstractXMLResultParser {

    private static final String ROOT_EXPRESSION = "//success/data/list/ontologyBean";

    // NB JSon version has filtering capacity. That should make its way here if
    // this is to be used.

    @Inject
    public OntologySearchResultParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);
    }

    protected Resource analyzeNode(Object node) throws XPathEvaluationException {
        String virtualOntologyId = getText(node, "ontologyId/text()");

        Resource ontology = new Resource(
                Ontology.toOntologyURI(virtualOntologyId));

        ontology.putValue(Ontology.ONTOLOGY_VERSION_ID,
                getText(node, "id/text()"));
        ontology.putValue(Ontology.ONTOLOGY_ABBREVIATION,
                getText(node, "abbreviation/text()"));
        ontology.putValue(Ontology.VIRTUAL_ONTOLOGY_ID, virtualOntologyId);
        ontology.putValue(Ontology.LABEL, getText(node, "displayLabel/text()"));
        ontology.putValue(Ontology.DESCRIPTION,
                getText(node, "description/text()"));

        return ontology;
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