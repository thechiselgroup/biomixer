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
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactlyLightweightCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologyMetricServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers;
import org.thechiselgroup.biomixer.shared.core.test.mockito.FirstInvocationArgumentAnswer;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * TODO The expander test here is based off of the
 * ConceptMappingNeighbourhoodExpanderTest. The expander goals are fairly
 * different. I put this in place so we could have some failed tests, and things
 * need to be tested properly. See that other test class for ideas.
 * 
 * @author everbeek
 * 
 */
@Ignore
// Ignore until it is fleshed out
public class OntologyNodeMetricsExpanderTest {

    @Mock
    private VisualItem visualItem;

    private ResourceSet visualItemResources;

    private AutomaticOntologyExpander underTest;

    @Mock
    private OntologyMetricServiceAsync mappingService;

    @Mock
    private ResourceManager resourceManager;

    @Mock
    private NodeExpansionCallback<Graph> expansionCallback;

    private Resource outgoingMapping;

    private Resource incomingMapping;

    private Resource sourceResource;

    private Resource ontology;

    private Resource targetResource;

    private ErrorHandler errorHandler;

    @Test
    public void addExistingMappedResourcesToAutomaticResources() {
        visualItemResources.add(ontology);
        stubResourceManagerContains(sourceResource);
        stubResourceManagerContains(targetResource);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(outgoingMapping);
        mappings.add(incomingMapping);

        expandUnderTestWithMappings(ontology.getUri(), mappings,
                new HashMap<String, Serializable>());

        verify(expansionCallback, times(1))
                .addAutomaticResource(sourceResource);
        verify(expansionCallback, times(1))
                .addAutomaticResource(targetResource);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void addLoadedResourcesToAutomaticResources() {
        visualItemResources.add(ontology);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(outgoingMapping);
        mappings.add(incomingMapping);

        Resource addedTargetResource = Ontology
                .createOntologyResource("otherOntologyId1");

        Resource addedSourceResource = Ontology
                .createOntologyResource("otherOntologyId2");

        when(resourceManager.add(sourceResource)).thenReturn(
                addedSourceResource);
        when(resourceManager.add(targetResource)).thenReturn(
                addedTargetResource);

        ArgumentCaptor<AsyncCallback> sourceCaptor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        // doNothing().when(termService).getBasicInformation(
        // eq(Ontology.getOntologyId(sourceResource)),
        // eq(Ontology.getConceptId(sourceResource)),
        // sourceCaptor.capture());

        ArgumentCaptor<AsyncCallback> targetCaptor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        // doNothing().when(termService).getBasicInformation(
        // eq(Ontology.getOntologyId(targetResource)),
        // eq(Ontology.getConceptId(targetResource)),
        // targetCaptor.capture());

        expandUnderTestWithMappings(ontology.getUri(), mappings,
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
        visualItemResources.add(ontology);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(outgoingMapping);
        mappings.add(incomingMapping);

        ArgumentCaptor<AsyncCallback> sourceCaptor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        // doNothing().when(termService).getBasicInformation(
        // eq(Ontology.getOntologyId(sourceResource)),
        // eq(Ontology.getConceptId(sourceResource)),
        // sourceCaptor.capture());

        ArgumentCaptor<AsyncCallback> targetCaptor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        // doNothing().when(termService).getBasicInformation(
        // eq(Ontology.getOntologyId(targetResource)),
        // eq(Ontology.getConceptId(targetResource)),
        // targetCaptor.capture());

        expandUnderTestWithMappings(ontology.getUri(), mappings,
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
        visualItemResources.add(ontology);
        stubResourceManagerContains(sourceResource);
        stubResourceManagerContains(targetResource);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(outgoingMapping);
        mappings.add(incomingMapping);

        expandUnderTestWithMappings(ontology.getUri(), mappings,
                new HashMap<String, Serializable>());

        verify(expansionCallback, times(1)).addAutomaticResource(
                incomingMapping);
        verify(expansionCallback, times(1)).addAutomaticResource(
                outgoingMapping);
    }

    // @Test
    // public void addMappingsToResourceManagerAndUseReturnedVersion() {
    // final List<Resource> resultMappings = new ArrayList<Resource>();
    //
    // underTest = new OntologyNodeMappingExpander(mappingService,
    // errorHandler, resourceManager) {
    // @Override
    // protected void expandNeighbourhood(VisualItem visualItem,
    // Resource concept, GraphNodeExpansionCallback graph,
    // List<Resource> mappings) {
    //
    // resultMappings.addAll(mappings);
    // };
    // };
    //
    // visualItemResources.add(concept);
    //
    // List<Resource> mappings = new ArrayList<Resource>();
    // mappings.add(outgoingMapping);
    // mappings.add(incomingMapping);
    //
    // Resource addedOutgoingMapping = Mapping.createMappingResource(
    // "mapping1", concept.getUri(), targetResource.getUri());
    // Resource addedIncomingMapping = Mapping.createMappingResource(
    // "mapping2", sourceResource.getUri(), concept.getUri());
    //
    // when(resourceManager.add(incomingMapping)).thenReturn(
    // addedIncomingMapping);
    // when(resourceManager.add(outgoingMapping)).thenReturn(
    // addedOutgoingMapping);
    //
    // expandUnderTestWithMappings(concept.getUri(), mappings,
    // new HashMap<String, Serializable>());
    //
    // assertThat(resultMappings,
    // containsExactly(ResourceSetTestUtils.toResourceSet(
    // addedIncomingMapping, addedOutgoingMapping)));
    // }

    @SuppressWarnings("unchecked")
    public void correctIdsPassedIntoMappingService() {
        visualItemResources.add(ontology);

        underTest.expand(visualItem, expansionCallback);

        verify(mappingService, times(1)).getMetrics(
                eq(Ontology.getOntologyId(ontology.getUri())),
                any(AsyncCallback.class));
    }

    @Test
    public void doNotLoadContainedConceptsWithLabel() {
        String addedResourceUri = Ontology.toOntologyURI("ontologyId2");
        Resource addedResource = new Resource(addedResourceUri);
        addedResource.putValue(Ontology.ONTOLOGY_FULL_NAME, "label");

        testLoadConcepts(0, addedResourceUri, addedResource);
    }

    // Don't think we need to prevent loading of arcs in the ontology
    // mapper...arcs don't load like nodes do...
    // @SuppressWarnings("unchecked")
    // @Test
    // public void doNotLoadMappingsIfUriListsSetAndMappingResourcesAvailable()
    // {
    // visualItemResources.add(concept);
    //
    // concept.putValue(Ontology.OUTGOING_MAPPINGS, new UriList(
    // outgoingMapping.getUri()));
    // concept.putValue(Ontology.INCOMING_MAPPINGS, new UriList(
    // incomingMapping.getUri()));
    //
    // stubResourceManagerContains(incomingMapping);
    // stubResourceManagerContainsAllResources(Ontology.INCOMING_MAPPINGS);
    // stubResourceManagerContains(outgoingMapping);
    // stubResourceManagerContainsAllResources(Ontology.OUTGOING_MAPPINGS);
    //
    // underTest.expand(visualItem, expansionCallback);
    //
    // verify(mappingService, times(0)).getMappings(
    // any(LightweightList.class), any(AsyncCallback.class));
    // }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void expandUnderTestWithMappings(String ontologyUri,
            List<Resource> mappings, Map<String, Serializable> partialProperties) {

        // LightweightList<String> ontologyIdList = LightweightCollections
        // .toList(Ontology.getOntologyId(ontologyUri));

        ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        // TODO Fix this call...argument issues
        doNothing().when(mappingService).getMetrics(
                eq(Ontology.getOntologyId(ontologyUri)), captor.capture());
        doNothing().when(mappingService).getMetrics(eq(sourceResource),
                captor.capture());
        when(sourceResource.getValue(eq(Ontology.ONTOLOGY_VERSION_ID)))
                .thenReturn((Ontology.getOntologyId(ontologyUri)));

        underTest.expand(visualItem, expansionCallback);

        AsyncCallback<ResourceNeighbourhood> callback = captor.getValue();

        callback.onSuccess(new ResourceNeighbourhood(partialProperties,
                mappings));
    }

    @Test
    public void incomingMappingsAddedToResource() {
        visualItemResources.add(ontology);

        HashMap<String, Serializable> partialMappings = new HashMap<String, Serializable>();
        partialMappings.put(Ontology.INCOMING_MAPPINGS, new UriList(
                incomingMapping.getUri()));

        expandUnderTestWithMappings(ontology.getUri(),
                new ArrayList<Resource>(), partialMappings);

        assertThat(ontology.getUriListValue(Ontology.INCOMING_MAPPINGS),
                CollectionMatchers.containsExactly(incomingMapping.getUri()));
    }

    @Test
    public void loadUncontainedConcepts() {
        testLoadConcepts(1, Ontology.toOntologyURI("ontologyId2"), null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadUncontainedSourceConceptWithoutMappingServiceCall() {
        visualItemResources.add(ontology);

        ontology.putValue(Ontology.OUTGOING_MAPPINGS, new UriList());
        ontology.putValue(Ontology.INCOMING_MAPPINGS, new UriList(
                incomingMapping.getUri()));

        stubResourceManagerContains(incomingMapping);
        stubResourceManagerContainsAllResources(Ontology.INCOMING_MAPPINGS);
        stubResourceManagerContainsAllResources(Ontology.OUTGOING_MAPPINGS);
        stubResourceManagerResolveResources(Ontology.INCOMING_MAPPINGS,
                incomingMapping);
        stubResourceManagerResolveResources(Ontology.OUTGOING_MAPPINGS,
                outgoingMapping);

        underTest.expand(visualItem, expansionCallback);

        // verify(termService, times(1)).getBasicInformation(
        // eq(Ontology.getOntologyId(sourceResource.getUri())),
        // eq(Ontology.getConceptId(sourceResource.getUri())),
        // any(AsyncCallback.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadUncontainedTargetConceptWithoutMappingServiceCall() {
        visualItemResources.add(ontology);

        ontology.putValue(Ontology.OUTGOING_MAPPINGS, new UriList(
                outgoingMapping.getUri()));
        ontology.putValue(Ontology.INCOMING_MAPPINGS, new UriList());

        stubResourceManagerContains(outgoingMapping);
        stubResourceManagerContainsAllResources(Ontology.INCOMING_MAPPINGS);
        stubResourceManagerContainsAllResources(Ontology.OUTGOING_MAPPINGS);
        stubResourceManagerResolveResources(Ontology.INCOMING_MAPPINGS,
                incomingMapping);
        stubResourceManagerResolveResources(Ontology.OUTGOING_MAPPINGS,
                outgoingMapping);

        underTest.expand(visualItem, expansionCallback);

        // verify(termService, times(1)).getBasicInformation(
        // eq(Ontology.getOntologyId(targetResource.getUri())),
        // eq(Ontology.getConceptId(targetResource.getUri())),
        // any(AsyncCallback.class));
    }

    @Test
    public void outgoingMappingsAddedToResource() {
        visualItemResources.add(ontology);

        HashMap<String, Serializable> partialMappings = new HashMap<String, Serializable>();
        partialMappings.put(Ontology.OUTGOING_MAPPINGS, new UriList(
                outgoingMapping.getUri()));

        expandUnderTestWithMappings(ontology.getUri(),
                new ArrayList<Resource>(), partialMappings);

        assertThat(ontology.getUriListValue(Ontology.OUTGOING_MAPPINGS),
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

        underTest = new AutomaticOntologyExpander(mappingService, errorHandler);

        ontology = Ontology.createOntologyResource("ontologyId");

        targetResource = Ontology.createOntologyResource("otherOntologyId1");

        sourceResource = Ontology.createOntologyResource("otherOntologyId2");

        // outgoingMapping = Mapping.createMappingResource("mapping1",
        // concept.getUri(), targetResource.getUri());
        // incomingMapping = Mapping.createMappingResource("mapping2",
        // sourceResource.getUri(), concept.getUri());

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
        when(resourceManager.containsAllReferencedResources(ontology, property))
                .thenReturn(true);
    }

    private void stubResourceManagerResolveResources(String property,
            Resource resource) {
        when(resourceManager.resolveResources(ontology, property)).thenReturn(
                CollectionUtils.toList(resource));
    }

    @SuppressWarnings("unchecked")
    private void testLoadConcepts(int expectedResourceManagerCalls,
            String addedResourceUri, Resource addedResource) {

        assert addedResource == null
                || addedResource.getUri().equals(addedResourceUri);

        String concept1Uri = Ontology.toOntologyURI("ontologyId1");

        visualItemResources.add(Ontology.createOntologyResource(Ontology
                .getOntologyId(concept1Uri)));

        // List<Resource> mappings = new ArrayList<Resource>();
        // mappings.add(Mapping.createMappingResource("mappingId1", concept1Uri,
        // addedResourceUri));

        if (addedResource != null) {
            stubResourceManagerContains(addedResource);
        } else {
            when(resourceManager.contains(addedResourceUri)).thenReturn(false);
        }

        // expandUnderTestWithMappings(concept1Uri, mappings,
        // new HashMap<String, Serializable>());

        // verify(termService, times(expectedResourceManagerCalls))
        // .getBasicInformation(
        // eq(Ontology.getOntologyId(addedResourceUri)),
        // eq(Ontology.getConceptId(addedResourceUri)),
        // any(AsyncCallback.class));
    }

    @Test
    public void updateArcsForVisualItemAfterAddingMappings() {
        visualItemResources.add(ontology);
        stubResourceManagerContains(sourceResource);

        List<Resource> mappings = new ArrayList<Resource>();
        mappings.add(incomingMapping);

        expandUnderTestWithMappings(ontology.getUri(), mappings,
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