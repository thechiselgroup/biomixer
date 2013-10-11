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
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.Mapping.createMappingResource;
import static org.thechiselgroup.biomixer.client.graph.DirectConceptMappingArcType.ARC_DIRECTED;
import static org.thechiselgroup.biomixer.client.graph.DirectConceptMappingArcType.ARC_LABEL;
import static org.thechiselgroup.biomixer.client.graph.DirectConceptMappingArcType.ID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceAccessor;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemTestUtils;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers;

public class DirectConceptMappingArcTypeTest {

    @Mock
    private VisualItemContainer context;

    private DirectConceptMappingArcType underTest;

    private Resource concept;

    private String ontologyId;

    @Mock
    private ResourceAccessor resourceAccessor;

    private Arc createExpectedArc(String fromUri, String toUri) {
        return new Arc(Graph.getArcId(ID, fromUri, toUri), fromUri, toUri, ID,
                ARC_LABEL, ARC_DIRECTED);
    }

    @Test
    public void getIncomingArcItemsFromLexicallyBiggerConcepts() {
        String concept2Uri = Concept.toConceptURI(ontologyId, "c");

        Resource mapping = createMappingResource("mappingId", concept2Uri,
                concept.getUri(), ontologyId,
                (String) concept.getValue(Concept.ONTOLOGY_ACRONYM));
        when(resourceAccessor.contains(mapping.getUri())).thenReturn(true);
        when(resourceAccessor.getByUri(mapping.getUri())).thenReturn(mapping);
        concept.getUriListValue(Concept.INCOMING_MAPPINGS)
                .add(mapping.getUri());

        VisualItem conceptResourceItem = VisualItemTestUtils.createVisualItem(
                concept.getUri(), ResourceSetTestUtils.toResourceSet(concept));

        assertThat(underTest.getArcs(conceptResourceItem, context),
                CollectionMatchers.containsExactly(LightweightCollections
                        .toCollection(createExpectedArc(concept.getUri(),
                                concept2Uri))));
    }

    @Test
    public void getIncomingArcItemsFromLexicallySmallerConcepts() {
        String concept2Uri = Concept.toConceptURI(ontologyId, "a");

        Resource mapping = createMappingResource("mappingId", concept2Uri,
                concept.getUri(), ontologyId,
                (String) concept.getValue(Concept.ONTOLOGY_ACRONYM));
        when(resourceAccessor.contains(mapping.getUri())).thenReturn(true);
        when(resourceAccessor.getByUri(mapping.getUri())).thenReturn(mapping);
        concept.getUriListValue(Concept.INCOMING_MAPPINGS)
                .add(mapping.getUri());

        VisualItem conceptResourceItem = VisualItemTestUtils.createVisualItem(
                concept.getUri(), ResourceSetTestUtils.toResourceSet(concept));

        assertThat(underTest.getArcs(conceptResourceItem, context),
                CollectionMatchers.containsExactly(LightweightCollections
                        .toCollection(createExpectedArc(concept2Uri,
                                concept.getUri()))));
    }

    @Test
    public void getOutgoingArcItemsToLexicallyBiggerConcepts() {
        String concept2Uri = Concept.toConceptURI(ontologyId, "c");

        Resource mapping = createMappingResource("mappingId", concept.getUri(),
                concept2Uri,
                (String) concept.getValue(Concept.ONTOLOGY_ACRONYM), ontologyId);
        when(resourceAccessor.contains(mapping.getUri())).thenReturn(true);
        when(resourceAccessor.getByUri(mapping.getUri())).thenReturn(mapping);
        concept.getUriListValue(Concept.OUTGOING_MAPPINGS)
                .add(mapping.getUri());

        VisualItem conceptResourceItem = VisualItemTestUtils.createVisualItem(
                concept.getUri(), ResourceSetTestUtils.toResourceSet(concept));

        assertThat(underTest.getArcs(conceptResourceItem, context),
                CollectionMatchers.containsExactly(LightweightCollections
                        .toCollection(createExpectedArc(concept.getUri(),
                                concept2Uri))));
    }

    @Test
    public void getOutgoingArcItemsToLexicallySmallerConcepts() {
        String concept2Uri = Concept.toConceptURI(ontologyId, "a");

        Resource mapping = createMappingResource("mappingId", concept.getUri(),
                concept2Uri,
                (String) concept.getValue(Concept.ONTOLOGY_ACRONYM), ontologyId);
        when(resourceAccessor.contains(mapping.getUri())).thenReturn(true);
        when(resourceAccessor.getByUri(mapping.getUri())).thenReturn(mapping);
        concept.getUriListValue(Concept.OUTGOING_MAPPINGS)
                .add(mapping.getUri());

        VisualItem conceptResourceItem = VisualItemTestUtils.createVisualItem(
                concept.getUri(), ResourceSetTestUtils.toResourceSet(concept));

        assertThat(underTest.getArcs(conceptResourceItem, context),
                CollectionMatchers.containsExactly(LightweightCollections
                        .toCollection(createExpectedArc(concept2Uri,
                                concept.getUri()))));
    }

    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        ontologyId = "ontologyId";
        concept = Concept.createConceptResource(ontologyId, "b");

        underTest = new DirectConceptMappingArcType(resourceAccessor);
    }
}
