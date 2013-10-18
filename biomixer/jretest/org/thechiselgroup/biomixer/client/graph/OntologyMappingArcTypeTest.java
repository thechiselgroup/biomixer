/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.graph;

import static org.junit.Assert.assertThat;
import static org.thechiselgroup.biomixer.client.Ontology.createOntologyResource;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemTestUtils;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;

public class OntologyMappingArcTypeTest {

    private OntologyMappingArcType underTest;

    private String ontologyId;

    @Mock
    private VisualItemContainer context;

    private Resource ontology;

    private String createOntologyUri(String ontologyId) {
        return Ontology.toOntologyURI(ontologyId);
    }

    private Arc createExpectedArc(String fromUri, String toUri) {
        return new Arc(
                Graph.getArcId(OntologyMappingArcType.ID, fromUri, toUri),
                fromUri, toUri, OntologyMappingArcType.ID,
                OntologyMappingArcType.ARC_LABEL,
                OntologyMappingArcType.ARC_DIRECTED);
    }

    @Test
    public void getChildArcItems() {
        String childOntology1Uri = createOntologyUri("childOntology1ShortId");
        String childOntology2Uri = createOntologyUri("childOntology2ShortId");

        ontology.getUriListValue(Ontology.OUTGOING_MAPPINGS).add(
                childOntology1Uri);
        ontology.getUriListValue(Ontology.OUTGOING_MAPPINGS).add(
                childOntology2Uri);
        assertThat(
                underTest.getArcs(VisualItemTestUtils.createVisualItem(
                        ontology.getUri(),
                        ResourceSetTestUtils.toResourceSet(ontology)), context),
                containsExactly(LightweightCollections.toCollection(
                        createExpectedArc(childOntology1Uri, ontology.getUri()),
                        createExpectedArc(childOntology2Uri, ontology.getUri()))));
    }

    @Test
    public void getParentArcItems() {
        String parentConcept1Uri = createOntologyUri("parentOntology1ShortId");
        String parentConcept2Uri = createOntologyUri("parentOntology2ShortId");

        UriList targetIncoming = ontology
                .getUriListValue(Ontology.INCOMING_MAPPINGS);
        targetIncoming.add(parentConcept1Uri);
        targetIncoming.add(parentConcept2Uri);

        ontology.putValue(Ontology.INCOMING_MAPPINGS, targetIncoming);

        assertThat(
                underTest.getArcs(VisualItemTestUtils.createVisualItem(
                        ontology.getUri(),
                        ResourceSetTestUtils.toResourceSet(ontology)), context),
                containsExactly(LightweightCollections.toCollection(
                        createExpectedArc(ontology.getUri(), parentConcept1Uri),
                        createExpectedArc(ontology.getUri(), parentConcept2Uri))));
    }

    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        underTest = new OntologyMappingArcType();

        ontologyId = "ontologyId";
        ontology = createOntologyResource(ontologyId, "uri", "name");
    }
}
