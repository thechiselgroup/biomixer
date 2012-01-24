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
package org.thechiselgroup.biomixer.client.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.captureOnResourceSetChanged;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResources;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers;

public class ProxyResourceSetTest {

    @Mock
    private ResourceSetDelegateChangedEventHandler delegateChangedHandler;

    private ProxyResourceSet underTest;

    private ResourceSet[] resourceSets;

    @Mock
    private ResourceSetChangedEventHandler resourcesChangedHandler;

    @Test
    public void doNotFireDelegateChangeIfSameDelegateIsSet() {
        underTest.addEventHandler(delegateChangedHandler);
        underTest.setDelegate(resourceSets[0]);
        underTest.setDelegate(resourceSets[0]);

        ArgumentCaptor<ResourceSetDelegateChangedEvent> argument = ArgumentCaptor
                .forClass(ResourceSetDelegateChangedEvent.class);
        verify(delegateChangedHandler, times(1)).onResourceSetContainerChanged(
                argument.capture());

        assertEquals(resourceSets[0], argument.getValue().getResourceSet());
    }

    @Test
    public void doNotFireDelegateChangeIfSameNullDelegateIsSet() {
        underTest.addEventHandler(delegateChangedHandler);
        underTest.setDelegate(null);

        verify(delegateChangedHandler, never()).onResourceSetContainerChanged(
                any(ResourceSetDelegateChangedEvent.class));
    }

    @Test
    public void doNotFireResourcesAddedEventOnResourcesAddedAfterDispose() {
        underTest.setDelegate(resourceSets[0]);
        underTest.addEventHandler(resourcesChangedHandler);
        underTest.dispose();

        resourceSets[0].addAll(ResourceSetTestUtils.createResources(3, 4));

        captureOnResourceSetChanged(0, resourcesChangedHandler);
    }

    @Test
    public void doNotFireResourcesAddedEventOnResourcesAddedToFormerDelegate() {
        underTest.setDelegate(resourceSets[0]);
        underTest.setDelegate(resourceSets[1]);
        underTest.addEventHandler(resourcesChangedHandler);

        resourceSets[0].addAll(ResourceSetTestUtils.createResources(3, 4));

        captureOnResourceSetChanged(0, resourcesChangedHandler);
    }

    @Test
    public void doNotFireResourcesRemovedEventOnResourcesRemovedAfterDispose() {
        underTest.setDelegate(resourceSets[0]);
        underTest.addEventHandler(resourcesChangedHandler);
        underTest.dispose();

        resourceSets[0].removeAll(ResourceSetTestUtils.createResources(1, 2));

        captureOnResourceSetChanged(0, resourcesChangedHandler);
    }

    @Test
    public void doNotFireResourcesRemovedEventOnResourcesRemovedFromFormerDelegate() {
        underTest.setDelegate(resourceSets[0]);
        underTest.setDelegate(resourceSets[1]);
        underTest.addEventHandler(resourcesChangedHandler);

        resourceSets[0].removeAll(ResourceSetTestUtils.createResources(1, 2));

        captureOnResourceSetChanged(0, resourcesChangedHandler);
    }

    @Test
    public void fireDelegateChangedEventWhenDelegateChanges() {
        underTest.addEventHandler(delegateChangedHandler);
        underTest.setDelegate(resourceSets[0]);

        ArgumentCaptor<ResourceSetDelegateChangedEvent> argument = ArgumentCaptor
                .forClass(ResourceSetDelegateChangedEvent.class);
        verify(delegateChangedHandler, times(1)).onResourceSetContainerChanged(
                argument.capture());

        assertEquals(resourceSets[0], argument.getValue().getResourceSet());
    }

    @Test
    public void fireResourcesAddedEventOnResourcesAddedToDelegate() {
        underTest.setDelegate(resourceSets[0]);
        underTest.addEventHandler(resourcesChangedHandler);

        resourceSets[0].addAll(ResourceSetTestUtils.createResources(3, 4));

        ResourceSetChangedEvent firedEvent = captureOnResourceSetChanged(1,
                resourcesChangedHandler).getValue();

        assertThat(firedEvent.getAddedResources().toList(),
                CollectionMatchers.containsExactly(ResourceSetTestUtils
                        .createResources(3, 4)));
        assertSame(underTest, firedEvent.getTarget());
    }

    @Test
    public void fireResourcesAddedEventOnResourcesAddedToNewDelegate() {
        underTest.setDelegate(resourceSets[0]);
        underTest.setDelegate(resourceSets[1]);
        underTest.addEventHandler(resourcesChangedHandler);

        resourceSets[1].addAll(ResourceSetTestUtils.createResources(4, 5));

        ResourceSetChangedEvent firedEvent = captureOnResourceSetChanged(1,
                resourcesChangedHandler).getValue();

        assertThat(firedEvent.getAddedResources().toList(),
                CollectionMatchers.containsExactly(ResourceSetTestUtils
                        .createResources(4, 5)));
        assertSame(underTest, firedEvent.getTarget());
    }

    @Test
    public void fireResourcesAddedEventWhenAddingToCustomDelegate() {
        DefaultResourceSet delegate = new DefaultResourceSet();

        underTest = new ProxyResourceSet(delegate);

        underTest.addEventHandler(resourcesChangedHandler);
        delegate.addAll(createResources(3, 4));

        ResourceSetChangedEvent firedEvent = captureOnResourceSetChanged(1,
                resourcesChangedHandler).getValue();

        assertThat(firedEvent.getAddedResources(),
                containsExactly(createResources(3, 4)));
        assertSame(underTest, firedEvent.getTarget());
    }

    @Test
    public void fireResourcesEventOnDelegateChange() {
        underTest.setDelegate(resourceSets[2]);
        underTest.addEventHandler(resourcesChangedHandler);

        underTest.setDelegate(resourceSets[3]);

        ResourceSetChangedEvent event = captureOnResourceSetChanged(1,
                resourcesChangedHandler).getValue();

        assertThat(event.getAddedResources().toList(),
                CollectionMatchers.containsExactly(ResourceSetTestUtils
                        .createResources(4, 5)));
        assertThat(event.getRemovedResources().toList(),
                CollectionMatchers.containsExactly(ResourceSetTestUtils
                        .createResources(1, 2)));

        assertSame(underTest, event.getTarget());
    }

    @Test
    public void fireResourcesRemovedEventOnResourcesRemovedFromDelegate() {
        underTest.setDelegate(resourceSets[0]);
        underTest.addEventHandler(resourcesChangedHandler);

        resourceSets[0].removeAll(ResourceSetTestUtils.createResources(1, 2));

        ResourceSetChangedEvent firedEvent = captureOnResourceSetChanged(1,
                resourcesChangedHandler).getValue();

        assertThat(firedEvent.getRemovedResources().toList(),
                CollectionMatchers.containsExactly(ResourceSetTestUtils
                        .createResources(1, 2)));
        assertSame(underTest, firedEvent.getTarget());
    }

    @Test
    public void fireResourcesRemovedEventOnResourcesRemovedFromNewDelegate() {
        underTest.setDelegate(resourceSets[0]);
        underTest.setDelegate(resourceSets[1]);
        underTest.addEventHandler(resourcesChangedHandler);

        resourceSets[1].removeAll(ResourceSetTestUtils.createResources(2, 3));

        ResourceSetChangedEvent firedEvent = captureOnResourceSetChanged(1,
                resourcesChangedHandler).getValue();

        assertThat(firedEvent.getRemovedResources().toList(),
                CollectionMatchers.containsExactly(ResourceSetTestUtils
                        .createResources(2, 3)));
        assertSame(underTest, firedEvent.getTarget());
    }

    @Test
    public void getDelegateReturnsCurrentDelegate1() {
        underTest.setDelegate(resourceSets[0]);

        assertEquals(resourceSets[0], underTest.getDelegate());
    }

    @Test
    public void getDelegateReturnsCurrentDelegate2() {
        underTest.setDelegate(resourceSets[0]);
        underTest.setDelegate(resourceSets[1]);

        assertEquals(resourceSets[1], underTest.getDelegate());
    }

    @Test
    public void hasDelegateAfterSettingDelegate() {
        underTest.setDelegate(resourceSets[0]);

        assertEquals(true, underTest.hasDelegate());
    }

    @Test
    public void hasNoDelegateIfSetToNull() {
        underTest.setDelegate(resourceSets[0]);
        underTest.setDelegate(null);

        assertEquals(false, underTest.hasDelegate());
    }

    @Test
    public void hasNoDelegateInitially() {
        assertEquals(false, underTest.hasDelegate());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new ProxyResourceSet();

        resourceSets = new ResourceSet[4];
        resourceSets[0] = ResourceSetTestUtils.createResources(1, 2);
        resourceSets[1] = ResourceSetTestUtils.createResources(2, 3);
        resourceSets[2] = ResourceSetTestUtils.createResources(1, 2, 3);
        resourceSets[3] = ResourceSetTestUtils.createResources(3, 4, 5);
    }
}
