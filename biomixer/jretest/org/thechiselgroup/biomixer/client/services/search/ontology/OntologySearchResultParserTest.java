/*******************************************************************************
 * Copyright 2012 Eric Verbeek
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractJsonParserTest;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class OntologySearchResultParserTest extends AbstractJsonParserTest {

    private OntologySearchResultParser underTest;

    public List<String> getValues(Set<Resource> resources, String key) {
        List<String> values = new ArrayList<String>();
        for (Resource resource : resources) {
            values.add((String) resource.getValue(key));
        }
        return values;
    }

    @Test
    public void parseSearchResults() throws Exception {
        Set<Resource> searchResults = parseSearchResults("searchResults.xml");
        List<String> ontologyAcronyms = getValues(searchResults,
                Ontology.ONTOLOGY_ACRONYM);
        assertThat(ontologyAcronyms.size(), equalTo(4));
        assertThat(ontologyAcronyms,
                containsExactly(Arrays.asList("1158", "1033", "1016", "1135")));
    }

    public Set<Resource> parseSearchResults(String xmlFilename)
            throws Exception {
        String responseXml = IOUtils
                .readIntoString(OntologySearchResultParserTest.class
                        .getResourceAsStream(xmlFilename));

        return underTest.parse(responseXml);
    }

    @Before
    public void setUp() throws Exception {
        this.underTest = new OntologySearchResultParser(
                new StandardJavaXMLDocumentProcessor());
    }

}
