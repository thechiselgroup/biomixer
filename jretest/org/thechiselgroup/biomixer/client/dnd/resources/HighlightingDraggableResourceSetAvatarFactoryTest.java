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
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
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
import org.thechiselgroup.biomixer.client.dnd.test.DndTestHelpers;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.ResourceSetTestUtils;
import org.thechiselgroup.choosel.core.client.resources.ui.HighlightingResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.choosel.core.client.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.choosel.core.client.util.Disposable;
import org.thechiselgroup.choosel.core.client.visualization.model.extensions.HighlightingModel;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class HighlightingDraggableResourceSetAvatarFactoryTest {

    @Mock
    private ResourceSetAvatar avatar;

    @Mock
    private ResourceSetAvatarFactory delegate;

    @Mock
    private ResourceSetAvatarDragController dragController;

    @Mock
    private HandlerRegistration handlerRegistration;

    private HighlightingModel hoverModel;

    private ResourceSet resources;

    private HighlightingResourceSetAvatarFactory underTest;

    @Test
    public void addDisposeHook() {
        underTest.createAvatar(ResourceSetTestUtils.createResources(1));

        ArgumentCaptor<Disposable> argument = ArgumentCaptor
                .forClass(Disposable.class);
        verify(avatar, times(2)).addDisposable(argument.capture());

        argument.getAllValues().get(1).dispose();

        verify(dragController, times(1)).removeDragHandler(
                any(DragHandler.class));
    }

    @Test
    public void hoverClearedAtDragEnd() {
        underTest.createAvatar(resources);

        ArgumentCaptor<DragHandler> argument = ArgumentCaptor
                .forClass(DragHandler.class);
        verify(dragController, times(1)).addDragHandler(argument.capture());

        DragHandler dragHandler = argument.getValue();

        dragHandler.onDragEnd(mock(DragEndEvent.class));

        verify(hoverModel, times(1)).setHighlightedResourceSet(
                ((ResourceSet) isNull()));
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge bridge = MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);
        DndTestHelpers.mockDragClientBundle(bridge);

        resources = spy(ResourceSetTestUtils.createResources(1));
        hoverModel = spy(new HighlightingModel());

        underTest = new HighlightingDraggableResourceSetAvatarFactory(delegate,
                hoverModel, dragController);

        when(delegate.createAvatar(any(ResourceSet.class))).thenReturn(avatar);

        when(avatar.getResourceSet()).thenReturn(resources);

        when(avatar.addMouseOutHandler(any(MouseOutHandler.class))).thenReturn(
                handlerRegistration);
        when(avatar.addMouseOverHandler(any(MouseOverHandler.class)))
                .thenReturn(handlerRegistration);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }
}
