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
import static org.thechiselgroup.biomixer.client.Concept.createConceptResource;
import static org.thechiselgroup.biomixer.client.graph.ConceptArcType.ARC_DIRECTED;
import static org.thechiselgroup.biomixer.client.graph.ConceptArcType.ID;
import static org.thechiselgroup.choosel.core.shared.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceSetTestUtils;
import org.thechiselgroup.choosel.core.client.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightCollections;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItemContainer;
import org.thechiselgroup.choosel.core.client.visualization.model.implementation.VisualItemTestUtils;

public class ConceptArcTypeTest {

    private ConceptArcType underTest;

    private String ontologyId;

    @Mock
    private VisualItemContainer context;

    private Resource concept;

    private String createConceptUri(String shortId) {
        return Concept.toConceptURI(ontologyId, shortId);
    }

    private Arc createExpectedArc(String fromUri, String toUri) {
        return new Arc(Graph.getArcId(ID, fromUri, toUri), fromUri, toUri, ID,
                ARC_DIRECTED);
    }

    @Test
    public void getChildArcItems() {
        String childConcept1Uri = createConceptUri("childConcept1ShortId");
        String childConcept2Uri = createConceptUri("childConcept2ShortId");

        concept.getUriListValue(Concept.CHILD_CONCEPTS).add(childConcept1Uri);
        concept.getUriListValue(Concept.CHILD_CONCEPTS).add(childConcept2Uri);

        assertThat(underTest.getArcs(
                VisualItemTestUtils.createVisualItem(concept.getUri(), ResourceSetTestUtils.toResourceSet(concept)),
                context), containsExactly(LightweightCollections.toCollection(
                createExpectedArc(childConcept1Uri, concept.getUri()),
                createExpectedArc(childConcept2Uri, concept.getUri()))));
    }

    @Test
    public void getParentArcItems() {
        String parentConcept1Uri = createConceptUri("parentConcept1ShortId");
        String parentConcept2Uri = createConceptUri("parentConcept2ShortId");

        concept.getUriListValue(Concept.PARENT_CONCEPTS).add(parentConcept1Uri);
        concept.getUriListValue(Concept.PARENT_CONCEPTS).add(parentConcept2Uri);

        assertThat(underTest.getArcs(
                VisualItemTestUtils.createVisualItem(concept.getUri(), ResourceSetTestUtils.toResourceSet(concept)),
                context), containsExactly(LightweightCollections.toCollection(
                createExpectedArc(concept.getUri(), parentConcept1Uri),
                createExpectedArc(concept.getUri(), parentConcept2Uri))));
    }

    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        underTest = new ConceptArcType();

        ontologyId = "ontologyId";
        concept = createConceptResource(ontologyId, "conceptShortId");
    }
}
