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
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.resources.UnmodifiableResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ui.HighlightingResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class HighlightingResourceSetAvatarFactoryTest {

    @Mock
    private ResourceSetAvatar avatar;

    @Mock
    private ResourceSetAvatarFactory delegate;

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
        verify(avatar, times(1)).addDisposable(argument.capture());

        argument.getValue().dispose();

        verify(handlerRegistration, times(2)).removeHandler();
    }

    @Test
    public void highlightIfUnmodifiableWrapperGetsHighlighted() {
        ResourceSet wrappedSet = ResourceSetTestUtils.createResources(1);
        UnmodifiableResourceSet unmodifiableWrapper = new UnmodifiableResourceSet(
                wrappedSet);

        when(avatar.getResourceSet()).thenReturn(wrappedSet);

        underTest.createAvatar(wrappedSet);

        hoverModel.setHighlightedResourceSet(unmodifiableWrapper);

        verify(avatar, times(1)).setHover(true);
    }

    @Test
    public void highlightUnmodifiableWrapperIfOtherUnmodifiableWrapperGetsHighlighted() {
        ResourceSet wrappedSet = ResourceSetTestUtils.createResources(1);
        UnmodifiableResourceSet unmodifiableWrapper1 = new UnmodifiableResourceSet(
                wrappedSet);
        UnmodifiableResourceSet unmodifiableWrapper2 = new UnmodifiableResourceSet(
                wrappedSet);

        when(avatar.getResourceSet()).thenReturn(unmodifiableWrapper1);

        underTest.createAvatar(unmodifiableWrapper1);

        hoverModel.setHighlightedResourceSet(unmodifiableWrapper2);

        verify(avatar, times(1)).setHover(true);
    }

    @Test
    public void highlightUnmodifiableWrappersIfWrappedSetGetsHighlighted() {
        ResourceSet wrappedSet = ResourceSetTestUtils.createResources(1);
        UnmodifiableResourceSet unmodifiableWrapper = new UnmodifiableResourceSet(
                wrappedSet);

        when(avatar.getResourceSet()).thenReturn(unmodifiableWrapper);

        underTest.createAvatar(unmodifiableWrapper);

        hoverModel.setHighlightedResourceSet(wrappedSet);

        verify(avatar, times(1)).setHover(true);
    }

    @Test
    public void mouseOutRemovesResourcesFromHover() {
        underTest.createAvatar(resources);

        when(avatar.getResourceSet()).thenReturn(resources);

        ArgumentCaptor<MouseOutHandler> captor = ArgumentCaptor
                .forClass(MouseOutHandler.class);
        verify(avatar, times(1)).addMouseOutHandler(captor.capture());

        MouseOutHandler mouseOutHandler = captor.getValue();

        mouseOutHandler.onMouseOut(mock(MouseOutEvent.class));

        verify(hoverModel, times(1)).setHighlightedResourceSet(
                ((ResourceSet) isNull()));
    }

    @Test
    public void mouseOverAddsResourcesToHover() {
        underTest.createAvatar(resources);

        when(avatar.getResourceSet()).thenReturn(resources);

        ArgumentCaptor<MouseOverHandler> captor = ArgumentCaptor
                .forClass(MouseOverHandler.class);
        verify(avatar, times(1)).addMouseOverHandler(captor.capture());

        MouseOverHandler mouseOverHandler = captor.getValue();

        mouseOverHandler.onMouseOver(mock(MouseOverEvent.class));

        verify(hoverModel, times(1)).setHighlightedResourceSet(eq(resources));
    }

    @Test
    public void setDragAvatarHoverOnResourceSetContainerEvent() {
        underTest.createAvatar(resources);
        hoverModel.setHighlightedResourceSet(resources);

        verify(avatar, times(1)).setHover(true);
    }

    @Test
    public void setDragAvatarHoverOnResourceSetContainerEventToNull() {
        underTest.createAvatar(resources);

        hoverModel.setHighlightedResourceSet(resources);
        hoverModel.setHighlightedResourceSet(null);

        verify(avatar, times(1)).setHover(false);
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        resources = spy(ResourceSetTestUtils.createResources(1));
        hoverModel = spy(new HighlightingModel());

        underTest = new HighlightingResourceSetAvatarFactory(delegate,
                hoverModel);

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
