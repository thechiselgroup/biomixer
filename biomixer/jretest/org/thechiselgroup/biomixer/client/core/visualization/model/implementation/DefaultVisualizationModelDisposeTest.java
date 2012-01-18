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

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualizationModelTestHelper.stubHandlerRegistration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualizationModel;

import com.google.gwt.event.shared.HandlerRegistration;

/* 
 * TODO we don't count how often event handler get registered. Every added event
 * handler should also get removed.
 */
public class DefaultVisualizationModelDisposeTest {

    private DefaultVisualizationModel underTest;

    @Mock
    private HandlerRegistration containedResourcesHandlerRegistration;

    @Mock
    private HandlerRegistration highlightedResourcesHandlerRegistration;

    @Mock
    private HandlerRegistration selectedResourcesHandlerRegistration;

    private DefaultVisualizationModelTestHelper helper;

    @Test
    public void disposeContainedResourcesEventHandler() {
        verify(containedResourcesHandlerRegistration, times(1)).removeHandler();
    }

    @Test
    public void disposeContentDisplay() {
        verify(helper.getViewContentDisplay(), times(1)).dispose();
    }

    @Test
    public void removesAllRegisteredHighlightedResourcesEventHandlersOnDispose() {
        ArgumentCaptor<ResourceSetChangedEventHandler> captor = ArgumentCaptor
                .forClass(ResourceSetChangedEventHandler.class);
        verify(helper.getHighlightedResources(), atLeastOnce())
                .addEventHandler(captor.capture());

        verify(highlightedResourcesHandlerRegistration,
                times(captor.getAllValues().size())).removeHandler();
    }

    @Test
    public void removesAllRegisteredSelectedResourcesEventHandlersOnDispose() {
        ArgumentCaptor<ResourceSetChangedEventHandler> captor = ArgumentCaptor
                .forClass(ResourceSetChangedEventHandler.class);
        verify(helper.getSelectedResources(), atLeastOnce()).addEventHandler(
                captor.capture());

        verify(selectedResourcesHandlerRegistration,
                times(captor.getAllValues().size())).removeHandler();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        helper = new DefaultVisualizationModelTestHelper();
        helper.mockContainedResources();
        helper.mockHighlightedResources();
        helper.mockSelectedResources();
        stubHandlerRegistration(helper.getContainedResources(),
                containedResourcesHandlerRegistration);
        stubHandlerRegistration(helper.getSelectedResources(),
                selectedResourcesHandlerRegistration);
        stubHandlerRegistration(helper.getHighlightedResources(),
                highlightedResourcesHandlerRegistration);

        underTest = helper.createTestVisualizationModel();

        underTest.dispose();
    }

}