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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyVersionParser;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class OntologyVersionParserTest {

    private OntologyVersionParser underTest;

    public String getOntologyVersionId(String virtualOntologyId,
            String xmlFilename) throws Exception {
        String responseXML = IOUtils
                .readIntoString(OntologyVersionParserTest.class
                        .getResourceAsStream(xmlFilename));

        return underTest.parse(virtualOntologyId, responseXML);
    }

    @Test
    public void getOntologyVersionIdForVirtualId1148() throws Exception {
        String virtualOntologyId = "1148";
        String ontologyVersionId = getOntologyVersionId(virtualOntologyId,
                "virtual_ontology_id_1148.response");
        assertThat(ontologyVersionId, equalTo("42948"));
    }

    @Test
    public void getOntologyVersionIdForVirtualId1487() throws Exception {
        String virtualOntologyId = "1487";
        String ontologyVersionId = getOntologyVersionId(virtualOntologyId,
                "virtual_ontology_id_1487.response");
        assertThat(ontologyVersionId, equalTo("42651"));
    }

    @Before
    public void setUp() throws Exception {
        underTest = new OntologyVersionParser(
                new StandardJavaXMLDocumentProcessor());
    }

}
