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
package org.thechiselgroup.biomixer.client.services.hierarchy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.Arrays;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.services.hierarchy.HierarchyParser;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class HierarchyParserTest {

    private HierarchyParser underTest;

    private Set<String> getResourcePath(String conceptShortId,
            String xmlFilename, String virtualOntologyId) throws Exception {
        String responseXml = IOUtils.readIntoString(HierarchyParserTest.class
                .getResourceAsStream(xmlFilename));

        return underTest.parse(conceptShortId, responseXml, virtualOntologyId);
    }

    @Test
    public void parseSingleHierarchyLengthFour() throws Exception {
        String virtualOntologyId = "1487";
        Set<String> pathShortIds = getResourcePath("SympatheticNervousSystem",
                "single_hierarchy_length_four.response", virtualOntologyId);

        assertThat(pathShortIds.size(), equalTo(4));
        assertThat(pathShortIds, containsExactly(Arrays.asList(
                "SympatheticNervousSystem", "BodySystem", "NervousSystem",
                "AutonomicNervousSystem")));
    }

    @Test
    public void parseTwoHierarchies() throws Exception {
        String virtualOntologyId = "1070";
        Set<String> pathShortIds = getResourcePath("GO:0007569",
                "two_hierarchies.response", virtualOntologyId);

        assertThat(pathShortIds.size(), equalTo(5));
        assertThat(pathShortIds, containsExactly(Arrays.asList("GO:0008150",
                "GO:0009987", "GO:0032502", "GO:0007568", "GO:0007569")));
    }

    @Before
    public void setUp() throws Exception {
        underTest = new HierarchyParser(new StandardJavaXMLDocumentProcessor());
    }

}
