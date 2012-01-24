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
package org.thechiselgroup.biomixer.client.core.visualization.ui;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetsPresenter;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceSetActivatedEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceSetActivatedEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.SelectionModel;

public class DefaultSelectionModelPresenterTest {

    @Mock
    private ResourceSetsPresenter selectionDropPresenter;

    @Mock
    private ResourceSetsPresenter selectionPresenter;

    private DefaultSelectionModelPresenter underTest;

    @Mock
    private SelectionModel selectionModel;

    @Test
    public void addResourceSetToSelectionPresenterWhenAddedToModel() {
        ResourceSet selection = ResourceSetTestUtils.createResources(1);

        captureResourceSetAddedHandler().onResourceSetAdded(
                new ResourceSetAddedEvent(selection));

        verify(selectionPresenter, times(1)).addResourceSet(selection);
    }

    private ResourceSetActivatedEventHandler captureResourceSetActivatedHandler() {
        ArgumentCaptor<ResourceSetActivatedEventHandler> captor = ArgumentCaptor
                .forClass(ResourceSetActivatedEventHandler.class);
        verify(selectionModel, times(1)).addEventHandler(captor.capture());
        return captor.getValue();
    }

    private ResourceSetAddedEventHandler captureResourceSetAddedHandler() {
        ArgumentCaptor<ResourceSetAddedEventHandler> captor = ArgumentCaptor
                .forClass(ResourceSetAddedEventHandler.class);
        verify(selectionModel, times(1)).addEventHandler(captor.capture());
        return captor.getValue();
    }

    private ResourceSetRemovedEventHandler captureResourceSetRemovedHandler() {
        ArgumentCaptor<ResourceSetRemovedEventHandler> captor = ArgumentCaptor
                .forClass(ResourceSetRemovedEventHandler.class);
        verify(selectionModel, times(1)).addEventHandler(captor.capture());
        return captor.getValue();
    }

    @Test
    public void disposeSelectionDropPresenter() {
        underTest.dispose();

        verify(selectionDropPresenter, times(1)).dispose();
    }

    @Test
    public void disposeSelectionPresenter() {
        underTest.dispose();

        verify(selectionPresenter, times(1)).dispose();
    }

    @Test
    public void removeResourceSetFromSelectionPresenterWhenRemovedFromModel() {
        ResourceSet selection = ResourceSetTestUtils.createResources(1);

        captureResourceSetRemovedHandler().onResourceSetRemoved(
                new ResourceSetRemovedEvent(selection));

        verify(selectionPresenter, times(1)).removeResourceSet(selection);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = spy(new DefaultSelectionModelPresenter(
                selectionDropPresenter, selectionPresenter, selectionModel));

        underTest.init();
    }

    @Test
    public void updateSelectionPresenterWhenSelectionChanges() {
        ResourceSet selection = ResourceSetTestUtils.createResources(1);

        captureResourceSetActivatedHandler().onResourceSetActivated(
                new ResourceSetActivatedEvent(selection));

        verify(selectionPresenter, times(1)).setSelectedResourceSet(selection);
    }
}
