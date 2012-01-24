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
package org.thechiselgroup.biomixer.client.core.resources.ui;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;

import com.google.gwt.event.shared.HandlerRegistration;

public class DisableResourceSetAvatarIfEmptyManagerTest {

    @Mock
    private ResourceSetAvatar avatar;

    @Mock
    private HandlerRegistration handlerRegistration;

    private ResourceSet resources;

    private DisableResourceSetAvatarIfEmptyManager underTest;

    @Test
    public void disabledIfEmptyByDefault() {
        underTest.init();

        verify(avatar, times(1)).setEnabled(false);
    }

    @Test
    public void disableIfResourceRemoved() {
        resources.add(ResourceSetTestUtils.createResource(2));
        underTest.init();
        resources.remove(ResourceSetTestUtils.createResource(2));

        verify(avatar, times(1)).setEnabled(false);
    }

    @Test
    public void enabledIfNotEmptyByDefault() {
        resources.add(ResourceSetTestUtils.createResource(2));
        underTest.init();

        verify(avatar, times(1)).setEnabled(true);
    }

    @Test
    public void enableIfResourceAdded() {
        underTest.init();
        resources.add(ResourceSetTestUtils.createResource(2));

        verify(avatar, times(1)).setEnabled(true);
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        resources = spy(ResourceSetTestUtils.createResources());

        when(avatar.getResourceSet()).thenReturn(resources);
        when(
                avatar.addResourceChangedHandler(any(ResourceSetAvatarResourcesChangedEventHandler.class)))
                .thenReturn(handlerRegistration);

        underTest = new DisableResourceSetAvatarIfEmptyManager(avatar);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }

    @Test
    public void updateDragAvatarOnResourcesChange() {
        underTest.init();

        ArgumentCaptor<ResourceSetAvatarResourcesChangedEventHandler> argument = ArgumentCaptor
                .forClass(ResourceSetAvatarResourcesChangedEventHandler.class);
        verify(avatar, times(1)).addResourceChangedHandler(argument.capture());

        argument.getValue().onResourcesChanged(
                new ResourceSetAvatarResourcesChangedEvent(avatar,
                        ResourceSetTestUtils.createResources(1), resources));

        verify(avatar, times(1)).setEnabled(false);
    }
}
