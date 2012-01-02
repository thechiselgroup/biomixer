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
package org.thechiselgroup.biomixer.client.dnd.resources;

import static org.mockito.Matchers.any;
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
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.dnd.test.DndTestHelpers;

public class DropTargetResourceSetAvatarFactoryTest {

    @Mock
    private ResourceSetAvatarFactory delegate;

    @Mock
    private ResourceSetAvatar dragAvatar;

    @Mock
    private ResourceSetAvatarDropTargetManager dropTargetManager;

    private DropTargetResourceSetAvatarFactory underTest;

    @Test
    public void addDisposeHook() {
        underTest.createAvatar(ResourceSetTestUtils.createResources(1));

        ArgumentCaptor<Disposable> argument = ArgumentCaptor
                .forClass(Disposable.class);
        verify(dragAvatar, times(1)).addDisposable(argument.capture());

        argument.getValue().dispose();

        verify(dropTargetManager, times(1)).disableDropTarget(dragAvatar);
    }

    @Test
    public void enableDropTarget() {
        underTest.createAvatar(ResourceSetTestUtils.createResources(1));

        verify(dropTargetManager, times(1)).enableDropTarget(dragAvatar);
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge bridge = MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);
        DndTestHelpers.mockDragClientBundle(bridge);

        underTest = new DropTargetResourceSetAvatarFactory(delegate,
                dropTargetManager);

        when(delegate.createAvatar(any(ResourceSet.class))).thenReturn(
                dragAvatar);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }
}
