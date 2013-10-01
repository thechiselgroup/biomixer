/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel, Bo Fu
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactlyLightweightCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.mapping.ConceptMappingServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers;
import org.thechiselgroup.biomixer.shared.core.test.mockito.FirstInvocationArgumentAnswer;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConceptMappingNeighbourhoodExpanderTest {

    @Mock
    private VisualItem visualItem;

    private ResourceSet visualItemResources;

    private ConceptMappingNeighbourhoodExpander<Graph> underTest;

    @Mock
    private ConceptMappingServiceAsync mappingService;

    @Mock
    private ResourceManager resourceManager;

    @Mock
    private TermServiceAsync termService;

    @Mock
    private NodeExpansionCallback<Graph> expansionCallback;

    private Resource outgoingMapping;

    private Resource incomingMapping;

    private Resource sourceResource;

    private Resource concept;

    private Resource targetResource;

    private ErrorHandler errorHandler;

    @Test
    public void addExistingMappedResourcesToAutomaticResources() {
        visualItemResources.add(concept);
        stubResourceManagerContains(sourceResource);
        stubResourceManagerContains(targetResource);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(outgoingMapping);
        mappings.add(incomingMapping);

        expandUnderTestWithMappings(concept.getUri(), mappings,
                new HashMap<String, Serializable>());

        verify(expansionCallback, times(1))
                .addAutomaticResource(sourceResource);
        verify(expansionCallback, times(1))
                .addAutomaticResource(targetResource);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void addLoadedResourcesToAutomaticResources() {
        visualItemResources.add(concept);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(outgoingMapping);
        mappings.add(incomingMapping);

        Resource addedTargetResource = Concept.createConceptResource(
                "otherOntologyId1", "targetConcept1");

        Resource addedSourceResource = Concept.createConceptResource(
                "otherOntologyId2", "sourceConcept1");

        when(resourceManager.add(sourceResource)).thenReturn(
                addedSourceResource);
        when(resourceManager.add(targetResource)).thenReturn(
                addedTargetResource);

        ArgumentCaptor<AsyncCallback> sourceCaptor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        doNothing().when(termService).getBasicInformation(
                eq(Concept.getOntologyId(sourceResource)),
                eq(Concept.getConceptId(sourceResource)),
                sourceCaptor.capture());

        ArgumentCaptor<AsyncCallback> targetCaptor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        doNothing().when(termService).getBasicInformation(
                eq(Concept.getOntologyId(targetResource)),
                eq(Concept.getConceptId(targetResource)),
                targetCaptor.capture());

        expandUnderTestWithMappings(concept.getUri(), mappings,
                new HashMap<String, Serializable>());

        AsyncCallback<Resource> sourceCallback = sourceCaptor.getValue();
        sourceCallback.onSuccess(sourceResource);

        AsyncCallback<Resource> targetCallback = targetCaptor.getValue();
        targetCallback.onSuccess(targetResource);

        verify(expansionCallback, times(1)).addAutomaticResource(
                addedSourceResource);
        verify(expansionCallback, times(1)).addAutomaticResource(
                incomingMapping);
        verify(expansionCallback, times(1)).addAutomaticResource(
                addedTargetResource);
        verify(expansionCallback, times(1)).addAutomaticResource(
                outgoingMapping);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void addLoadedResourcesToManager() {
        visualItemResources.add(concept);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(outgoingMapping);
        mappings.add(incomingMapping);

        ArgumentCaptor<AsyncCallback> sourceCaptor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        doNothing().when(termService).getBasicInformation(
                eq(Concept.getOntologyId(sourceResource)),
                eq(Concept.getConceptId(sourceResource)),
                sourceCaptor.capture());

        ArgumentCaptor<AsyncCallback> targetCaptor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        doNothing().when(termService).getBasicInformation(
                eq(Concept.getOntologyId(targetResource)),
                eq(Concept.getConceptId(targetResource)),
                targetCaptor.capture());

        expandUnderTestWithMappings(concept.getUri(), mappings,
                new HashMap<String, Serializable>());

        AsyncCallback<Resource> sourceCallback = sourceCaptor.getValue();
        sourceCallback.onSuccess(sourceResource);

        AsyncCallback<Resource> targetCallback = targetCaptor.getValue();
        targetCallback.onSuccess(targetResource);

        verify(resourceManager, times(1)).add(sourceResource);
        verify(resourceManager, times(1)).add(targetResource);
    }

    @Test
    public void addMappingsToAutomaticResources() {
        visualItemResources.add(concept);
        stubResourceManagerContains(sourceResource);
        stubResourceManagerContains(targetResource);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(outgoingMapping);
        mappings.add(incomingMapping);

        expandUnderTestWithMappings(concept.getUri(), mappings,
                new HashMap<String, Serializable>());

        verify(expansionCallback, times(1)).addAutomaticResource(
                incomingMapping);
        verify(expansionCallback, times(1)).addAutomaticResource(
                outgoingMapping);
    }

    @Test
    public void addMappingsToResourceManagerAndUseReturnedVersion() {
        final List<Resource> resultMappings = new ArrayList<Resource>();

        underTest = new ConceptMappingNeighbourhoodExpander<Graph>(
                mappingService, errorHandler, resourceManager, termService) {
            @Override
            protected void expandNeighbourhood(VisualItem visualItem,
                    Resource concept, NodeExpansionCallback<Graph> graph,
                    List<Resource> mappings) {

                resultMappings.addAll(mappings);
            };
        };

        visualItemResources.add(concept);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(outgoingMapping);
        mappings.add(incomingMapping);

        Resource addedOutgoingMapping = Mapping.createMappingResource(
                "mapping1", concept.getUri(), targetResource.getUri());
        Resource addedIncomingMapping = Mapping.createMappingResource(
                "mapping2", sourceResource.getUri(), concept.getUri());

        when(resourceManager.add(incomingMapping)).thenReturn(
                addedIncomingMapping);
        when(resourceManager.add(outgoingMapping)).thenReturn(
                addedOutgoingMapping);

        expandUnderTestWithMappings(concept.getUri(), mappings,
                new HashMap<String, Serializable>());

        assertThat(resultMappings,
                containsExactly(ResourceSetTestUtils.toResourceSet(
                        addedIncomingMapping, addedOutgoingMapping)));
    }

    @SuppressWarnings("unchecked")
    public void correctIdsPassedIntoMappingService() {
        visualItemResources.add(concept);

        underTest.expand(visualItem, expansionCallback);

        verify(mappingService, times(1)).getMappings(
                eq(Concept.getOntologyAcronym(concept.getUri())),
                eq(Concept.getConceptId(concept.getUri())), eq(false),
                any(AsyncCallback.class));
    }

    @Test
    public void doNotLoadContainedConceptsWithLabel() {
        String addedResourceUri = Concept.toConceptURI("ontologyId2",
                "conceptId2");
        Resource addedResource = new Resource(addedResourceUri);
        addedResource.putValue(Concept.LABEL, "label");

        testLoadConcepts(0, addedResourceUri, addedResource);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void doNotLoadMappingsIfUriListsSetAndMappingResourcesAvailable() {
        visualItemResources.add(concept);

        concept.putValue(Concept.OUTGOING_MAPPINGS,
                new UriList(outgoingMapping.getUri()));
        concept.putValue(Concept.INCOMING_MAPPINGS,
                new UriList(incomingMapping.getUri()));

        stubResourceManagerContains(incomingMapping);
        stubResourceManagerContainsAllResources(Concept.INCOMING_MAPPINGS);
        stubResourceManagerContains(outgoingMapping);
        stubResourceManagerContainsAllResources(Concept.OUTGOING_MAPPINGS);

        underTest.expand(visualItem, expansionCallback);

        verify(mappingService, times(0)).getMappings(any(String.class),
                any(String.class), eq(true), any(AsyncCallback.class));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void expandUnderTestWithMappings(String conceptUri,
            List<Resource> mappings, Map<String, Serializable> partialProperties) {

        ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        doNothing().when(mappingService).getMappings(
                eq(Concept.getOntologyAcronym(conceptUri)),
                eq(Concept.getConceptId(conceptUri)), eq(false),
                captor.capture());

        underTest.expand(visualItem, expansionCallback);

        AsyncCallback<ResourceNeighbourhood> callback = captor.getValue();

        callback.onSuccess(new ResourceNeighbourhood(partialProperties,
                mappings));
    }

    @Test
    public void incomingMappingsAddedToResource() {
        visualItemResources.add(concept);

        HashMap<String, Serializable> partialMappings = new HashMap<String, Serializable>();
        partialMappings.put(Concept.INCOMING_MAPPINGS, new UriList(
                incomingMapping.getUri()));

        expandUnderTestWithMappings(concept.getUri(),
                new ArrayList<Resource>(), partialMappings);

        assertThat(concept.getUriListValue(Concept.INCOMING_MAPPINGS),
                CollectionMatchers.containsExactly(incomingMapping.getUri()));
    }

    @Test
    public void loadUncontainedConcepts() {
        testLoadConcepts(1, Concept.toConceptURI("ontologyId2", "conceptId2"),
                null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadUncontainedSourceConceptWithoutMappingServiceCall() {
        visualItemResources.add(concept);

        concept.putValue(Concept.OUTGOING_MAPPINGS, new UriList());
        concept.putValue(Concept.INCOMING_MAPPINGS,
                new UriList(incomingMapping.getUri()));

        stubResourceManagerContains(incomingMapping);
        stubResourceManagerContainsAllResources(Concept.INCOMING_MAPPINGS);
        stubResourceManagerContainsAllResources(Concept.OUTGOING_MAPPINGS);
        stubResourceManagerResolveResources(Concept.INCOMING_MAPPINGS,
                incomingMapping);
        stubResourceManagerResolveResources(Concept.OUTGOING_MAPPINGS,
                outgoingMapping);

        underTest.expand(visualItem, expansionCallback);

        verify(termService, times(1)).getBasicInformation(
                eq(Concept.getOntologyAcronym(sourceResource.getUri())),
                eq(Concept.getConceptId(sourceResource.getUri())),
                any(AsyncCallback.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadUncontainedTargetConceptWithoutMappingServiceCall() {
        visualItemResources.add(concept);

        concept.putValue(Concept.OUTGOING_MAPPINGS,
                new UriList(outgoingMapping.getUri()));
        concept.putValue(Concept.INCOMING_MAPPINGS, new UriList());

        stubResourceManagerContains(outgoingMapping);
        stubResourceManagerContainsAllResources(Concept.INCOMING_MAPPINGS);
        stubResourceManagerContainsAllResources(Concept.OUTGOING_MAPPINGS);
        stubResourceManagerResolveResources(Concept.INCOMING_MAPPINGS,
                incomingMapping);
        stubResourceManagerResolveResources(Concept.OUTGOING_MAPPINGS,
                outgoingMapping);

        underTest.expand(visualItem, expansionCallback);

        verify(termService, times(1)).getBasicInformation(
                eq(Concept.getOntologyAcronym(targetResource.getUri())),
                eq(Concept.getConceptId(targetResource.getUri())),
                any(AsyncCallback.class));
    }

    @Test
    public void outgoingMappingsAddedToResource() {
        visualItemResources.add(concept);

        HashMap<String, Serializable> partialMappings = new HashMap<String, Serializable>();
        partialMappings.put(Concept.OUTGOING_MAPPINGS, new UriList(
                outgoingMapping.getUri()));

        expandUnderTestWithMappings(concept.getUri(),
                new ArrayList<Resource>(), partialMappings);

        assertThat(concept.getUriListValue(Concept.OUTGOING_MAPPINGS),
                CollectionMatchers.containsExactly(outgoingMapping.getUri()));
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        visualItemResources = new DefaultResourceSet();
        when(visualItem.getResources()).thenReturn(visualItemResources);

        errorHandler = new ErrorHandler() {
            @Override
            public void handleError(Throwable error) {
                throw new AssertionFailedError(error.getMessage());
            }
        };

        underTest = new ConceptMappingNeighbourhoodExpander(mappingService,
                errorHandler, resourceManager, termService);

        concept = Concept.createConceptResource("ontologyId", "conceptId");

        targetResource = Concept.createConceptResource("otherOntologyId1",
                "targetConcept1");

        sourceResource = Concept.createConceptResource("otherOntologyId2",
                "sourceConcept1");

        outgoingMapping = Mapping.createMappingResource("mapping1",
                concept.getUri(), targetResource.getUri());
        incomingMapping = Mapping.createMappingResource("mapping2",
                sourceResource.getUri(), concept.getUri());

        when(expansionCallback.getResourceManager())
                .thenReturn(resourceManager);

        when(resourceManager.add(any(Resource.class))).thenAnswer(
                new FirstInvocationArgumentAnswer<Resource>());
        when(resourceManager.addAll(any(List.class))).thenAnswer(
                new FirstInvocationArgumentAnswer<List<Resource>>());

        when(expansionCallback.isInitialized()).thenReturn(true);
    }

    private void stubResourceManagerContains(Resource resource) {
        String uri = resource.getUri();
        when(resourceManager.contains(uri)).thenReturn(true);
        when(resourceManager.getByUri(uri)).thenReturn(resource);
    }

    private void stubResourceManagerContainsAllResources(String property) {
        when(resourceManager.containsAllReferencedResources(concept, property))
                .thenReturn(true);
    }

    private void stubResourceManagerResolveResources(String property,
            Resource resource) {
        when(resourceManager.resolveResources(concept, property)).thenReturn(
                CollectionUtils.toList(resource));
    }

    @SuppressWarnings("unchecked")
    private void testLoadConcepts(int expectedResourceManagerCalls,
            String addedResourceUri, Resource addedResource) {

        assert addedResource == null
                || addedResource.getUri().equals(addedResourceUri);

        String concept1Uri = Concept.toConceptURI("ontologyId1", "conceptId1");

        visualItemResources.add(Concept.createConceptResource(
                Concept.getOntologyAcronym(concept1Uri),
                Concept.getConceptId(concept1Uri)));

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(Mapping.createMappingResource("mappingId1", concept1Uri,
                addedResourceUri));

        if (addedResource != null) {
            stubResourceManagerContains(addedResource);
        } else {
            when(resourceManager.contains(addedResourceUri)).thenReturn(false);
        }

        expandUnderTestWithMappings(concept1Uri, mappings,
                new HashMap<String, Serializable>());

        verify(termService, times(expectedResourceManagerCalls))
                .getBasicInformation(
                        eq(Concept.getOntologyAcronym(addedResourceUri)),
                        eq(Concept.getConceptId(addedResourceUri)),
                        any(AsyncCallback.class));
    }

    @Test
    public void updateArcsForVisualItemAfterAddingMappings() {
        visualItemResources.add(concept);
        stubResourceManagerContains(sourceResource);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(incomingMapping);

        expandUnderTestWithMappings(concept.getUri(), mappings,
                new HashMap<String, Serializable>());

        InOrder inOrder = inOrder(expansionCallback);

        inOrder.verify(expansionCallback, times(1)).addAutomaticResource(
                sourceResource);
        inOrder.verify(expansionCallback, times(1)).addAutomaticResource(
                incomingMapping);
        VisualItem[] visualItems = { visualItem };
        inOrder.verify(expansionCallback, times(1)).updateArcsForVisuaItems(
                argThat(containsExactlyLightweightCollection(visualItems)));
    }
}