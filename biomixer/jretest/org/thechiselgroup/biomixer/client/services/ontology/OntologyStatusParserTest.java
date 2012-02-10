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

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyStatusParser;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class OntologyStatusParserTest {

    private OntologyStatusParser underTest;

    @Test
    public void checkVirtualOntologyIdAssociatedWithCorrectStatus()
            throws Exception {
        Map<String, List<String>> virtualOntologyIdsByStatus = parseResponse("20120206_statuses.response");
        assertTrue(virtualOntologyIdsByStatus.get("28").contains("1000"));
        assertTrue(virtualOntologyIdsByStatus.get("28").contains("1676"));
        assertTrue(virtualOntologyIdsByStatus.get("99").contains("1562"));
    }

    public Map<String, List<String>> parseResponse(String xmlFilename)
            throws Exception {
        String responseXML = IOUtils.readIntoString(OntologyStatusParser.class
                .getResourceAsStream(xmlFilename));
        return underTest.parseStatuses(responseXML);
    }

    @Before
    public void setUp() throws Exception {
        underTest = new OntologyStatusParser(
                new StandardJavaXMLDocumentProcessor());
    }

}
