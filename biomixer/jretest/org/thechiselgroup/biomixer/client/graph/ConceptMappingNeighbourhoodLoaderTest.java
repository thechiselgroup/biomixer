/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel, Bo Fu
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.Concept.createConceptResource;
import static org.thechiselgroup.biomixer.client.Mapping.createMappingResource;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.toResourceSet;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactlyLightweightCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.mapping.ConceptMappingServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.shared.core.test.mockito.FirstInvocationArgumentAnswer;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConceptMappingNeighbourhoodLoaderTest {

    @Mock
    private ConceptMappingServiceAsync mappingService;

    @Mock
    private ErrorHandler errorHandler;

    private ConceptMappingNeighbourhoodLoader underTest;

    @Mock
    private NodeExpansionCallback<Graph> expansionCallback;

    @Mock
    private VisualItem visualItem;

    @Mock
    private ResourceManager resourceManager;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private AsyncCallback<ResourceNeighbourhood> callExpand(Resource concept) {
        ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        doNothing().when(mappingService).getMappings(
                eq(Concept.getOntologyId(concept)),
                eq(Concept.getConceptId(concept)), eq(false), captor.capture());
        underTest.expand(visualItem, expansionCallback);

        return captor.getValue();
    }

    @Test
    public void mappingsAddedToGraph() {
        Resource expandedConcept = createConceptResource("o1", "c1");
        Resource containedConcept = createConceptResource("o2", "c2");
        Resource uncontainedConcept = createConceptResource("o3", "c3");

        when(visualItem.getResources()).thenReturn(
                toResourceSet(expandedConcept));

        when(
                expansionCallback.containsResourceWithUri(expandedConcept
                        .getUri())).thenReturn(true);
        when(
                expansionCallback.containsResourceWithUri(containedConcept
                        .getUri())).thenReturn(true);
        when(
                expansionCallback.containsResourceWithUri(uncontainedConcept
                        .getUri())).thenReturn(false);

        Resource mapping1 = createMappingResource("m1",
                expandedConcept.getUri(), containedConcept.getUri());
        Resource mapping2 = createMappingResource("m2",
                containedConcept.getUri(), expandedConcept.getUri());
        Resource mapping3 = createMappingResource("m3",
                expandedConcept.getUri(), uncontainedConcept.getUri());
        Resource mapping4 = createMappingResource("m4",
                uncontainedConcept.getUri(), expandedConcept.getUri());

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(mapping1);
        mappings.add(mapping2);
        mappings.add(mapping3);
        mappings.add(mapping4);

        AsyncCallback<ResourceNeighbourhood> callback = callExpand(expandedConcept);

        callback.onSuccess(new ResourceNeighbourhood(
                new HashMap<String, Serializable>(), mappings));

        verify(expansionCallback, times(1)).addAutomaticResource(mapping1);
        verify(expansionCallback, times(1)).addAutomaticResource(mapping2);
        verify(expansionCallback, times(0)).addAutomaticResource(mapping3);
        verify(expansionCallback, times(0)).addAutomaticResource(mapping4);
        VisualItem[] visualItems = { visualItem };

        verify(expansionCallback, times(1)).updateArcsForVisuaItems(
                argThat(containsExactlyLightweightCollection(visualItems)));
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        underTest = new ConceptMappingNeighbourhoodLoader(mappingService,
                resourceManager, errorHandler);

        when(resourceManager.add(any(Resource.class))).thenAnswer(
                new FirstInvocationArgumentAnswer<Resource>());
        when(resourceManager.addAll(any(List.class))).thenAnswer(
                new FirstInvocationArgumentAnswer<List<Resource>>());

        when(expansionCallback.isInitialized()).thenReturn(true);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }

}