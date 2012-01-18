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

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;

import com.google.inject.Inject;

/**
 * Parses the response XML that is returned by the term service into a set of
 * mapping resources.
 * 
 * @author Lars Grammel
 */
public class LightTermResponseWithoutRelationshipsParser extends
        AbstractXMLResultParser {

    @Inject
    public LightTermResponseWithoutRelationshipsParser(
            DocumentProcessor documentProcessor) {

        super(documentProcessor);
    }

    public Resource parseConcept(String ontologyId,
            String mappingServiceResponse) throws Exception {

        Object responseDocument = parseDocument(mappingServiceResponse);

        Object[] conceptNodes = getNodes(responseDocument,
                "/success/data/classBean");
        assert conceptNodes.length == 1;
        Object node = conceptNodes[0];

        String fullId = getText(node, "fullId/text()");
        String label = getText(node, "label/text()");
        String type = getText(node, "type/text()");

        Resource result = new Resource(Concept.toConceptURI(ontologyId, fullId));

        result.putValue(Concept.FULL_ID, fullId);
        result.putValue(Concept.VIRTUAL_ONTOLOGY_ID, ontologyId);
        result.putValue(Concept.TYPE, type);
        result.putValue(Concept.LABEL, label);

        return result;
    }
}