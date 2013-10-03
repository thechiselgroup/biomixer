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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactlyLightweightCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConceptConceptNeighbourhoodExpanderTest {

    private static final String CONCEPT_2_FULL_ID = "shortid2";

    private static final String CONCEPT_1_ONTOLOGY_ID = "ontology_id";

    private static final String CONCEPT_2_ONTOLOGY_ID = "ontology_id2";

    private static final String CONCEPT_1_FULL_ID = "shortid";

    private Resource concept2;

    private Resource concept2FromResult;

    @Mock
    private Node concept2Node;

    private String concept2Uri;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private DialogManager dialogManager;

    @Mock
    private NodeExpansionCallback<Graph> expansionCallback;

    @Mock
    private GraphDisplay graphDisplay;

    private Resource concept1;

    @Mock
    private Resource concept1FromResult;

    private String concept1Uri;

    @Mock
    private Node inputNode;

    private ConceptConceptNeighbourhoodExpander underTest;

    private ResourceManager resourceManager;

    private ResourceNeighbourhood result;

    @Mock
    private VisualItem visualItem;

    private ResourceSet visualItemResources;

    @Mock
    private ConceptNeighbourhoodServiceAsync neighbourhoodService;

    @Test
    public void addNodesToDataProvider() {
        callExpand();

        verify(expansionCallback, times(1)).addAutomaticResource(concept2);
        verify(graphDisplay, never()).addNode(any(Node.class));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void callExpand() {
        ArgumentCaptor<ErrorHandlingAsyncCallback> captor = ArgumentCaptor
                .forClass(ErrorHandlingAsyncCallback.class);

        doNothing().when(neighbourhoodService).getNeighbourhood(
                eq(CONCEPT_1_ONTOLOGY_ID), eq(CONCEPT_1_FULL_ID),
                captor.capture());

        underTest.expand(visualItem, expansionCallback);

        AsyncCallback<ResourceNeighbourhood> callback = captor.getValue();
        callback.onSuccess(result);
    }

    @Test
    public void doNotAddDuplicatedNodesToDataProvider() {
        when(expansionCallback.containsResourceWithUri(concept2.getUri()))
                .thenReturn(Boolean.TRUE);

        callExpand();

        verify(expansionCallback, never()).addAutomaticResource(concept2);
        verify(graphDisplay, never()).addNode(any(Node.class));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        concept2FromResult = ResourceSetTestUtils.createResource(1);

        resourceManager = spy(new DefaultResourceManager());

        concept1Uri = Concept.toConceptURI(CONCEPT_1_ONTOLOGY_ID,
                CONCEPT_1_FULL_ID);
        concept1 = new Resource(concept1Uri);
        concept1.putValue(Concept.ID, CONCEPT_1_FULL_ID);
        concept1.putValue(Concept.ONTOLOGY_ACRONYM, CONCEPT_1_ONTOLOGY_ID);

        concept2Uri = Concept.toConceptURI(CONCEPT_2_ONTOLOGY_ID,
                CONCEPT_2_FULL_ID);
        concept2 = new Resource(concept2Uri);
        concept2.putValue(Concept.ID, CONCEPT_2_FULL_ID);
        concept2.putValue(Concept.ONTOLOGY_ACRONYM, CONCEPT_2_ONTOLOGY_ID);

        when(graphDisplay.getNode(concept2Uri)).thenReturn(concept2Node);
        when(graphDisplay.getNode(concept1Uri)).thenReturn(inputNode);

        when(concept1FromResult.getUri()).thenReturn(concept1Uri);

        List<Resource> resources = new ArrayList<Resource>();
        resources.add(concept2FromResult);

        UriList childConcepts = new UriList();
        childConcepts.add(concept2Uri);

        Map<String, Serializable> partialProperties = new HashMap<String, Serializable>();
        partialProperties.put(Concept.CHILD_CONCEPTS, childConcepts);
        partialProperties.put(Concept.PARENT_CONCEPTS, childConcepts);

        result = new ResourceNeighbourhood(partialProperties, resources);

        when(resourceManager.add(concept1FromResult)).thenReturn(concept1);
        when(resourceManager.add(concept2FromResult)).thenReturn(concept2);

        visualItemResources = new DefaultResourceSet();
        when(visualItem.getResources()).thenReturn(visualItemResources);
        visualItemResources.add(concept1);

        underTest = new ConceptConceptNeighbourhoodExpander(errorHandler,
                resourceManager, neighbourhoodService, dialogManager);

        when(expansionCallback.isInitialized()).thenReturn(true);
    }

    @Test
    public void visualItemArcsUpdatedAfterNeighboursAdded() {
        callExpand();

        InOrder inOrder = inOrder(expansionCallback);

        inOrder.verify(expansionCallback, times(1)).addAutomaticResource(
                concept2);
        VisualItem[] visualItems = { visualItem };
        inOrder.verify(expansionCallback, times(1)).updateArcsForVisuaItems(
                argThat(containsExactlyLightweightCollection(visualItems)));
    }
}
