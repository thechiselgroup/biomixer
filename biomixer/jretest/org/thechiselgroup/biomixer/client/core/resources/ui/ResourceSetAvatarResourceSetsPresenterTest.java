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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;

public class ResourceSetAvatarResourceSetsPresenterTest {

    @Mock
    private ResourceSetAvatar dragAvatar;

    @Mock
    private ResourceSetAvatarFactory dragAvatarFactory;

    private ResourceSetAvatarResourceSetsPresenter dragAvatarResourceSetsPresenter;

    private ResourceSet resources;

    @Test
    public void disposeDragAvatarOnDispose() {
        dragAvatarResourceSetsPresenter.addResourceSet(resources);
        dragAvatarResourceSetsPresenter.dispose();

        verify(dragAvatar, times(1)).dispose();
    }

    @Test
    public void replaceResourceSetCallsDragAvatar() {
        String newLabel = "new-label";
        ResourceSet newResources = ResourceSetTestUtils.createResources(3);
        newResources.setLabel(newLabel);

        dragAvatarResourceSetsPresenter.addResourceSet(resources);
        dragAvatarResourceSetsPresenter.replaceResourceSet(resources,
                newResources);

        verify(dragAvatar, times(1)).setResourceSet(eq(newResources));
        verify(dragAvatar, times(1)).setText(eq(newLabel));
    }

    @Test
    public void setResourceSetEnabledCallsDragAvatar() {
        dragAvatarResourceSetsPresenter.addResourceSet(resources);

        dragAvatarResourceSetsPresenter.setResourceSetEnabled(resources, false);
        verify(dragAvatar, times(0)).setEnabled(eq(true));
        verify(dragAvatar, times(1)).setEnabled(eq(false));

        dragAvatarResourceSetsPresenter.setResourceSetEnabled(resources, true);
        verify(dragAvatar, times(1)).setEnabled(eq(true));
        verify(dragAvatar, times(1)).setEnabled(eq(false));
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        resources = spy(ResourceSetTestUtils.createResources(1));

        dragAvatarResourceSetsPresenter = new ResourceSetAvatarResourceSetsPresenter(
                dragAvatarFactory);

        when(dragAvatarFactory.createAvatar(any(ResourceSet.class)))
                .thenReturn(dragAvatar);

        dragAvatarResourceSetsPresenter.init();
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }
}
