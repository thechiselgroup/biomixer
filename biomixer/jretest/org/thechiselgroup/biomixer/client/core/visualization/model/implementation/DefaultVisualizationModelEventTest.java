/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.visualization.model.implementation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualizationModel;

import com.google.gwt.event.shared.HandlerRegistration;

// TODO test for events on view item updates
// TODO test for single event when view items are added, removed, updated at the same time
public class DefaultVisualizationModelEventTest {

    private DefaultVisualizationModelTestHelper helper;

    private DefaultVisualizationModel underTest;

    private VisualItemContainerChangeEvent captureEvent(
            VisualItemContainerChangeEventHandler handler) {
        ArgumentCaptor<VisualItemContainerChangeEvent> captor = ArgumentCaptor
                .forClass(VisualItemContainerChangeEvent.class);
        verify(handler, times(1))
                .onVisualItemContainerChanged(captor.capture());
        return captor.getValue();
    }

    @Test
    public void fireVisualItemContainerChangeEventWhenVisualItemIsAdded() {
        VisualItemContainerChangeEventHandler handler = mock(VisualItemContainerChangeEventHandler.class);

        underTest.getFullVisualItemContainer().addHandler(handler);

        Resource resource = ResourceSetTestUtils.createResource(1);
        helper.getContainedResources().add(resource);

        LightweightCollection<VisualItem> addedVisualItems = captureEvent(handler)
                .getDelta().getAddedElements();

        assertEquals(1, addedVisualItems.size());

        ResourceSet resources = addedVisualItems.iterator().next().getResources();

        assertEquals(1, resources.size());
        assertEquals(true, resources.contains(resource));
    }

    @Test
    public void fireVisualItemContainerChangeEventWhenVisualItemIsRemoved() {
        VisualItemContainerChangeEventHandler handler = mock(VisualItemContainerChangeEventHandler.class);

        HandlerRegistration registration = underTest
                .getFullVisualItemContainer().addHandler(handler);

        Resource resource = ResourceSetTestUtils.createResource(1);
        helper.getContainedResources().add(resource);

        VisualItem visualItem = captureEvent(handler).getDelta()
                .getAddedElements().iterator().next();

        VisualItemContainerChangeEventHandler handler2 = mock(VisualItemContainerChangeEventHandler.class);

        registration.removeHandler();
        underTest.getFullVisualItemContainer().addHandler(handler2);

        helper.getContainedResources().remove(resource);

        VisualItemContainerChangeEvent event2 = captureEvent(handler2);
        LightweightCollection<VisualItem> removedVisualItems = event2.getDelta()
                .getRemovedElements();
        assertEquals(1, removedVisualItems.size());
        assertEquals(true, removedVisualItems.contains(visualItem));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        helper = new DefaultVisualizationModelTestHelper();
        helper.createSlots();
        underTest = helper.createTestVisualizationModel();
    }

}