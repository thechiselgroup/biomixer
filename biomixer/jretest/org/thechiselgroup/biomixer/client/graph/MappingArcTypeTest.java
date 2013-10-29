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
import static org.thechiselgroup.biomixer.client.Mapping.createMappingResource;
import static org.thechiselgroup.biomixer.client.graph.MappingArcType.ARC_DIRECTED;
import static org.thechiselgroup.biomixer.client.graph.MappingArcType.ARC_LABEL;
import static org.thechiselgroup.biomixer.client.graph.MappingArcType.ID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemTestUtils;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers;

public class MappingArcTypeTest {

    private MappingArcType underTest;

    @Mock
    private VisualItemContainer context;

    private String ontologyId;

    private Arc createExpectedArc(String fromUri, String toUri) {
        return new Arc(Graph.getArcId(ID, fromUri, toUri), fromUri, toUri, ID,
                ARC_LABEL, ARC_DIRECTED, null);
    }

    @Test
    public void getArcsForMapping() {
        String concept1Uri = Concept
                .toConceptURI(ontologyId, "conceptShortId1");
        String concept2Uri = Concept
                .toConceptURI(ontologyId, "conceptShortId2");

        Resource mapping = createMappingResource("mappingId", concept1Uri,
                concept2Uri, ontologyId, ontologyId);

        VisualItem mappingVisualItem = VisualItemTestUtils.createVisualItem(
                mapping.getUri(), ResourceSetTestUtils.toResourceSet(mapping));

        assertThat(
                underTest.getArcs(mappingVisualItem, context),
                CollectionMatchers.containsExactly(LightweightCollections.toCollection(
                        createExpectedArc(concept1Uri, mapping.getUri()),
                        createExpectedArc(mapping.getUri(), concept2Uri))));
    }

    @Test
    public void getIncomingArcsForConcept() {
        Resource concept = Concept.createConceptResource(ontologyId,
                "conceptShortId1", "label", "type");
        String concept2Uri = Concept
                .toConceptURI(ontologyId, "conceptShortId2");
        Resource mapping = createMappingResource("mappingId", concept.getUri(),
                concept2Uri, ontologyId, ontologyId);
        concept.getUriListValue(Concept.INCOMING_MAPPINGS)
                .add(mapping.getUri());

        VisualItem conceptVisualItem = VisualItemTestUtils.createVisualItem(
                concept.getUri(), ResourceSetTestUtils.toResourceSet(concept));

        assertThat(underTest.getArcs(conceptVisualItem, context),
                CollectionMatchers.containsExactly(LightweightCollections
                        .toCollection(createExpectedArc(mapping.getUri(),
                                concept.getUri()))));
    }

    @Test
    public void getOutoingArcsForConcept() {
        Resource concept = Concept.createConceptResource(ontologyId,
                "conceptShortId1", "label", "type");
        String concept2Uri = Concept
                .toConceptURI(ontologyId, "conceptShortId2");
        Resource mapping = createMappingResource("mappingId", concept.getUri(),
                concept2Uri, ontologyId, ontologyId);
        concept.getUriListValue(Concept.OUTGOING_MAPPINGS)
                .add(mapping.getUri());

        VisualItem conceptVisualItem = VisualItemTestUtils.createVisualItem(
                concept.getUri(), ResourceSetTestUtils.toResourceSet(concept));

        assertThat(underTest.getArcs(conceptVisualItem, context),
                CollectionMatchers.containsExactly(LightweightCollections
                        .toCollection(createExpectedArc(concept.getUri(),
                                mapping.getUri()))));
    }

    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        ontologyId = "ontologyId";
        underTest = new MappingArcType();
    }
}
