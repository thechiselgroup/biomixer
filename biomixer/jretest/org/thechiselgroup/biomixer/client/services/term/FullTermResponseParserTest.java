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
package org.thechiselgroup.biomixer.client.services.term;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class FullTermResponseParserTest {

    private FullTermResponseParser underTest;

    private Resource getResource(String ontologyId, String xmlFilename)
            throws Exception {
        return underTest.parseResource(ontologyId, getResponseXML(xmlFilename));
    }

    private ResourceNeighbourhood getResourceNeighbourhood(String ontologyId,
            String xmlFilename) throws Exception {
        return underTest.parseNeighbourhood(ontologyId,
                getResponseXML(xmlFilename));
    }

    private String getResponseXML(String xmlFilename) throws IOException {
        return IOUtils.readIntoString(FullTermResponseParserTest.class
                .getResourceAsStream(xmlFilename));
    }

    @Test
    public void parseNeighbourhoodMultipleParentsMultipleChildrenOBO()
            throws Exception {
        String virtualOntologyId = "1070";
        ResourceNeighbourhood result = getResourceNeighbourhood(
                virtualOntologyId, "obo_multiple_parents_and_children.response");

        assertThat(result.getResources().size(), equalTo(7));

        Map<String, Serializable> partialProperties = result
                .getPartialProperties();
        UriList childUris = (UriList) partialProperties
                .get(Concept.CHILD_CONCEPTS);
        UriList parentUris = (UriList) partialProperties
                .get(Concept.PARENT_CONCEPTS);

        assertThat(childUris, containsExactly(Concept.asUris(virtualOntologyId,
                "http://purl.org/obo/owl/GO#GO_0001300",
                "http://purl.org/obo/owl/GO#GO_0090398",
                "http://purl.org/obo/owl/GO#GO_0001302",
                "http://purl.org/obo/owl/GO#GO_0007576",
                "http://purl.org/obo/owl/GO#GO_0001301")));

        assertThat(parentUris, containsExactly(Concept.asUris(
                virtualOntologyId, "http://purl.org/obo/owl/GO#GO_0007568",
                "http://purl.org/obo/owl/GO#GO_0009987")));
    }

    @Test
    public void parseResourceWithMultipleParentsMultipleChildrenOBO()
            throws Exception {
        String virtualOntologyId = "1070";
        Resource result = getResource(virtualOntologyId,
                "obo_multiple_parents_and_children.response");
        assertThat((String) result.getValue(Concept.ONTOLOGY_ACRONYM),
                equalTo(virtualOntologyId));
        assertThat((String) result.getValue(Concept.ID),
                equalTo("http://purl.org/obo/owl/GO#GO_0007569"));
        assertThat((String) result.getValue(Concept.OLD_SHORT_ID),
                equalTo("GO:0007569"));
        assertThat((String) result.getValue(Concept.LABEL),
                equalTo("cell aging"));
        assertThat(result.getUriListValue(Concept.CHILD_CONCEPTS).size(),
                equalTo(5));
        assertThat(result.getUriListValue(Concept.PARENT_CONCEPTS).size(),
                equalTo(2));
    }

    @Before
    public void setUp() throws Exception {
        underTest = new FullTermResponseParser(
                new StandardJavaXMLDocumentProcessor());
    }
}
