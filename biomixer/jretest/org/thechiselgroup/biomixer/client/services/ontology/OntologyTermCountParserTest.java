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
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class OntologyTermCountParserTest {

    private OntologyTermCountParser underTest;

    @Test
    public void getTermCount() throws Exception {
        String termCount = getTermCount("ontology_term_count_test.response");
        assertThat(termCount, equalTo("129"));
    }

    public String getTermCount(String xmlFilename) throws Exception {
        return underTest.parse(IOUtils
                .readIntoString(OntologyTermCountParserTest.class
                        .getResourceAsStream(xmlFilename)));
    }

    @Before
    public void setUP() throws Exception {
        this.underTest = new OntologyTermCountParser(
                new StandardJavaXMLDocumentProcessor());
    }

}
