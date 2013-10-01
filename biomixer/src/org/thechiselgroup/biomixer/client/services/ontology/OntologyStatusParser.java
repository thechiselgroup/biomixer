/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.services.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;

import com.google.inject.Inject;

public class OntologyStatusParser extends AbstractXMLResultParser {

    @Inject
    public OntologyStatusParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);
    }

    public Map<String, List<String>> parseStatuses(String xmlText)
            throws Exception {
        Map<String, List<String>> ontologyAcronymsByStatus = new HashMap<String, List<String>>();

        Object root = parseDocument(xmlText);

        for (Object ontologyBean : getNodes(root,
                "//success/data/list/ontologyBean")) {

            String ontologyAcronym = getText(ontologyBean,
                    "virtualOntologyId/text()");
            String status = getText(ontologyBean, "status/text()");
            if (!ontologyAcronymsByStatus.containsKey(status)) {
                ontologyAcronymsByStatus.put(status, new ArrayList<String>());
            }
            ontologyAcronymsByStatus.get(status).add(ontologyAcronym);
        }

        return ontologyAcronymsByStatus;
    }
}
