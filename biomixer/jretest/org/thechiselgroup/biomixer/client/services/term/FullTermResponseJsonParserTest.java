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
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.services.AbstractJsonParserTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.server.workbench.util.json.JacksonJsonParser;

public class FullTermResponseJsonParserTest extends AbstractJsonParserTest {

    private ConceptRelationshipJsonParser underTest;

    @Test
    public void parseNeighbourhoodMultipleParentsMultipleChildrenOBO()
            throws IOException {
        String virtualOntologyId = "1070";
        ResourceNeighbourhood neighbourhood = parseResourceNeighbourhood(
                virtualOntologyId, "obo_multiple_parents_and_children.json");

        assertThat(neighbourhood.getResources().size(), equalTo(8));
        Map<String, Serializable> partialProperties = neighbourhood
                .getPartialProperties();
        UriList childUris = (UriList) partialProperties
                .get(Concept.CHILD_CONCEPTS);
        UriList parentUris = (UriList) partialProperties
                .get(Concept.PARENT_CONCEPTS);

        assertThat(childUris, containsExactly(Concept.asUris(virtualOntologyId,
                "http://purl.obolibrary.org/obo/GO_0001300",
                "http://purl.obolibrary.org/obo/GO_0090399",
                "http://purl.obolibrary.org/obo/GO_0090398",
                "http://purl.obolibrary.org/obo/GO_0001302",
                "http://purl.obolibrary.org/obo/GO_0007576",
                "http://purl.obolibrary.org/obo/GO_0001301")));

        assertThat(parentUris, containsExactly(Concept.asUris(
                virtualOntologyId, "http://purl.obolibrary.org/obo/GO_0007568",
                "http://purl.obolibrary.org/obo/GO_0009987")));
    }

    // private Resource parseResource(String ontologyId, String jsonFilename)
    // throws IOException {
    // // Broken! Need new test.
    // return underTest.parseNewParents(ontologyId,
    // getFileContentsAsString(jsonFilename).toString());
    // }

    private ResourceNeighbourhood parseResourceNeighbourhood(String ontologyId,
            String jsonFilename) throws IOException {
        // Broken! Need new test.
        return underTest.parseNewParents(ontologyId,
                getFileContentsAsString(jsonFilename).toString());
    }

    // @Test
    // public void parseResourceWithMultipleParentsMultipleChildrenOBO()
    // throws IOException {
    // String virtualOntologyId = "1070";
    // Resource result = parseResource(virtualOntologyId,
    // "obo_multiple_parents_and_children.json");
    // assertThat((String) result.getValue(Concept.ONTOLOGY_ACRONYM),
    // equalTo(virtualOntologyId));
    // assertThat((String) result.getValue(Concept.ID),
    // equalTo("http://purl.obolibrary.org/obo/GO_0007569"));
    // assertThat((String) result.getValue(Concept.OLD_SHORT_ID),
    // equalTo("GO:0007569"));
    // assertThat((String) result.getValue(Concept.LABEL),
    // equalTo("cell aging"));
    // assertThat(result.getUriListValue(Concept.CHILD_CONCEPTS).size(),
    // equalTo(6));
    // assertThat(result.getUriListValue(Concept.PARENT_CONCEPTS).size(),
    // equalTo(2));
    // }

    // @Test
    // public void parseResourceWithNoChildren() throws IOException {
    // Resource result = parseResource("1487", "full_term_no_children.json");
    //
    // assertThat((String) result.getValue(Concept.ID),
    // equalTo("http://who.int/bodysystem.owl#VestibularSystem"));
    // assertThat(result.getUriListValue(Concept.PARENT_CONCEPTS).size(),
    // equalTo(1));
    // assertThat(result.getUriListValue(Concept.CHILD_CONCEPTS).size(),
    // equalTo(0));
    // }

    @Before
    public void setUp() {
        underTest = new ConceptRelationshipJsonParser(new JacksonJsonParser());
    }
}
