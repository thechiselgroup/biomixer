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
package org.thechiselgroup.biomixer.client.core.visualization.model.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.verifyOnResourcesAdded;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.verifyOnResourcesRemoved;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.label.SelectionModelLabelFactory;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.resources.persistence.DefaultResourceSetCollector;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.DefaultSelectionModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceSetActivatedEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceSetActivatedEventHandler;

import com.google.gwt.event.shared.HandlerRegistration;

public class DefaultSelectionModelTest {

    @Mock
    private ResourceSet selection;

    @Mock
    private HandlerRegistration selectionHandlerRegistration;

    private DefaultSelectionModel underTest;

    @Mock
    private ResourceSetChangedEventHandler resourceSetChangedHandler;

    @Mock
    private ResourceSetActivatedEventHandler activatedHandler;

    @Mock
    private PersistableRestorationService restorationService;

    private DefaultSelectionModel createDefaultSelectionModel() {
        return spy(new DefaultSelectionModel(new SelectionModelLabelFactory(),
                new DefaultResourceSetFactory()));
    }

    /**
     * Issue 58.
     */
    @Test
    public void fireActivatedEventWhenRestoringFromMemento() {
        ResourceSet selection1 = ResourceSetTestUtils.createResources(1);
        DefaultResourceSetCollector resourceSetCollector = new DefaultResourceSetCollector();

        // create selection
        underTest.addSelectionSet(selection1);
        underTest.setSelection(selection1);

        // store old view state & restore it on new view
        Memento memento = underTest.save(resourceSetCollector);
        DefaultSelectionModel newModel = createDefaultSelectionModel();
        newModel.addEventHandler(activatedHandler);
        newModel.restore(memento, restorationService, resourceSetCollector);

        verifyActivatedEventFired(selection1);
    }

    @Test
    public void fireActivatedEventWhenSelectionChanges() {
        ResourceSet selection1 = ResourceSetTestUtils.createResources(1);
        ResourceSet selection2 = ResourceSetTestUtils.createResources();

        underTest.addSelectionSet(selection1);
        underTest.addSelectionSet(selection2);

        underTest.setSelection(selection1);
        underTest.addEventHandler(activatedHandler);
        underTest.setSelection(selection2);

        verifyActivatedEventFired(selection2);
    }

    @Test
    public void fireActivatedEventWhenSelectionCreatedOnSwitch() {
        underTest.setSelection(null);
        underTest.addEventHandler(activatedHandler);
        underTest.switchSelection(ResourceSetTestUtils.createResources(1));

        verifyActivatedEventFired(underTest.getSelectionSets().get(0));
    }

    @Test
    public void fireResourcesAddedWhenResourceAddedToSelection() {
        selection = ResourceSetTestUtils.createResources();
        underTest.addSelectionSet(selection);
        underTest.setSelection(selection);
        underTest.addEventHandler(resourceSetChangedHandler);

        selection.add(ResourceSetTestUtils.createResource(1));

        verifyOnResourcesAdded(ResourceSetTestUtils.createResources(1), resourceSetChangedHandler);
    }

    @Test
    public void fireResourcesAddedWhenSelectionChanges() {
        selection = ResourceSetTestUtils.createResources(1);
        underTest.addSelectionSet(selection);
        underTest.addEventHandler(resourceSetChangedHandler);
        underTest.setSelection(selection);

        verifyOnResourcesAdded(ResourceSetTestUtils.createResources(1), resourceSetChangedHandler);

    }

    @Test
    public void fireResourceSetAddedEventWhenResourceSetAdded() {
        ResourceSet selection = ResourceSetTestUtils.createResources(1);
        ResourceSetAddedEventHandler resourceSetAddedHandler = mock(ResourceSetAddedEventHandler.class);

        underTest.addEventHandler(resourceSetAddedHandler);
        underTest.addSelectionSet(selection);

        ArgumentCaptor<ResourceSetAddedEvent> captor = ArgumentCaptor
                .forClass(ResourceSetAddedEvent.class);
        verify(resourceSetAddedHandler, times(1)).onResourceSetAdded(
                captor.capture());
        assertThat(captor.getValue().getResourceSet(),
                containsExactly(selection));
    }

    @Test
    public void fireResourceSetRemovedEventWhenResourceSetRemoved() {
        ResourceSet selection = ResourceSetTestUtils.createResources(1);
        ResourceSetRemovedEventHandler resourceSetaddedHandler = mock(ResourceSetRemovedEventHandler.class);

        underTest.addSelectionSet(selection);
        underTest.addEventHandler(resourceSetaddedHandler);
        underTest.removeSelectionSet(selection);

        ArgumentCaptor<ResourceSetRemovedEvent> captor = ArgumentCaptor
                .forClass(ResourceSetRemovedEvent.class);
        verify(resourceSetaddedHandler, times(1)).onResourceSetRemoved(
                captor.capture());
        assertThat(captor.getValue().getResourceSet(),
                containsExactly(selection));
    }

    @Test
    public void fireResourcesRemoveEventWhenResourceRemovedFromSelection() {
        selection = ResourceSetTestUtils.createResources();
        underTest.addSelectionSet(selection);
        underTest.setSelection(selection);

        selection.add(ResourceSetTestUtils.createResource(1));
        underTest.addEventHandler(resourceSetChangedHandler);
        selection.remove(ResourceSetTestUtils.createResource(1));

        verifyOnResourcesRemoved(ResourceSetTestUtils.createResources(1), resourceSetChangedHandler);
    }

    @Test
    public void fireResourcesRemoveEventWhenSelectionChanges() {
        ResourceSet resources1 = ResourceSetTestUtils.createResources(1);
        ResourceSet resources2 = ResourceSetTestUtils.createResources();

        underTest.addSelectionSet(resources1);
        underTest.addSelectionSet(resources2);

        underTest.setSelection(resources1);
        underTest.addEventHandler(resourceSetChangedHandler);
        underTest.setSelection(resources2);

        verifyOnResourcesRemoved(ResourceSetTestUtils.createResources(1), resourceSetChangedHandler);
    }

    @Test
    public void partialSelectionIsChangedToFullSelectionOnSwitch() {
        ResourceSet resources1 = ResourceSetTestUtils.createResources(1);
        ResourceSet resources2 = ResourceSetTestUtils.createResources(1, 2);

        underTest.switchSelection(resources1);
        underTest.switchSelection(resources2);

        assertThat(underTest.getSelection(), containsExactly(resources2));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = createDefaultSelectionModel();

        when(
                selection
                        .addEventHandler(any(ResourceSetChangedEventHandler.class)))
                .thenReturn(selectionHandlerRegistration);
    }

    @Test
    public void switchSelectionCreatesSelectionIfNoneExists() {
        underTest.setSelection(null);
        underTest.switchSelection(ResourceSetTestUtils.createResources(1));

        List<ResourceSet> selectionSets = underTest.getSelectionSets();
        assertEquals(1, selectionSets.size());
        assertEquals(true, selectionSets.get(0).contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(true, underTest.getSelection().contains(ResourceSetTestUtils.createResource(1)));
    }

    private void verifyActivatedEventFired(ResourceSet selection) {
        ArgumentCaptor<ResourceSetActivatedEvent> captor = ArgumentCaptor
                .forClass(ResourceSetActivatedEvent.class);
        verify(activatedHandler, times(1)).onResourceSetActivated(
                captor.capture());
        assertThat(captor.getValue().getResourceSet(),
                containsExactly(selection));
    }
}
