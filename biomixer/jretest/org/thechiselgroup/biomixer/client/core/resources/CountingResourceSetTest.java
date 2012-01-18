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
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.captureOnResourceSetChanged;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.verifyOnResourcesAdded;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.verifyOnResourcesRemoved;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.CountingResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;

public class CountingResourceSetTest {

    @Mock
    private ResourceSetChangedEventHandler changedHandler;

    private Resource resource;

    private CountingResourceSet underTest;

    @Test
    public void addAllFiresEvent() {
        ResourceSet resources = ResourceSetTestUtils.createResources(1, 2);

        underTest.addEventHandler(changedHandler);
        underTest.addAll(resources);

        verifyOnResourcesAdded(resources, changedHandler);
    }

    @Test
    public void addAllWithContainedResourcesDoesNotFireEvent() {
        ResourceSet containedResources = ResourceSetTestUtils.createResources(1, 2, 3);

        underTest.addAll(containedResources);
        underTest.addEventHandler(changedHandler);
        underTest.addAll(containedResources);

        captureOnResourceSetChanged(0, changedHandler);
    }

    @Test
    public void addedFiredOnceIfAddedTwice() {
        underTest.addEventHandler(changedHandler);
        underTest.add(resource);
        underTest.add(resource);

        captureOnResourceSetChanged(1, changedHandler);
    }

    @Test
    public void eventNotFiredOnRemoveAllOfDoubleContainedResources() {
        ResourceSet resources = ResourceSetTestUtils.createResources(1, 2, 3);

        underTest.addAll(resources);
        underTest.addAll(resources);
        underTest.addEventHandler(changedHandler);
        underTest.removeAll(resources);

        captureOnResourceSetChanged(0, changedHandler);
    }

    @Test
    public void mixedAddAllFiresEventWithNewResources() {
        ResourceSet containedResources = ResourceSetTestUtils.createResources(1);
        ResourceSet resources = ResourceSetTestUtils.createResources(1, 2, 3);

        underTest.addAll(containedResources);
        underTest.addEventHandler(changedHandler);
        underTest.addAll(resources);

        verifyOnResourcesAdded(ResourceSetTestUtils.createResources(2, 3), changedHandler);
    }

    @Test
    public void mixedRemoveAllFiresEventWithActuallyRemovedResources() {
        ResourceSet doubleResources = ResourceSetTestUtils.createResources(1);
        ResourceSet resources = ResourceSetTestUtils.createResources(1, 2, 3);

        underTest.addAll(resources);
        underTest.addAll(doubleResources);
        underTest.addEventHandler(changedHandler);
        underTest.removeAll(resources);

        verifyOnResourcesRemoved(ResourceSetTestUtils.createResources(2, 3), changedHandler);
    }

    @Test
    public void removeAllFiresEvent() {
        ResourceSet resources = ResourceSetTestUtils.createResources(1, 2);

        underTest.addAll(resources);
        underTest.addEventHandler(changedHandler);
        underTest.removeAll(resources);

        verifyOnResourcesRemoved(resources, changedHandler);
    }

    @Test
    public void removeFiredOnceIfRemovedTwiceOnceAfterAddedTwice() {
        underTest.add(resource);
        underTest.add(resource);
        underTest.addEventHandler(changedHandler);
        underTest.remove(resource);
        underTest.remove(resource);

        captureOnResourceSetChanged(1, changedHandler);
    }

    @Test
    public void removeNotFiredIfOnlyRemovedOnceAfterAddedTwice() {
        underTest.add(resource);
        underTest.add(resource);
        underTest.addEventHandler(changedHandler);
        underTest.remove(resource);

        captureOnResourceSetChanged(0, changedHandler);
    }

    @Test
    public void resourceAddedTwiceNotContainedAfter2ndRemove() {
        underTest.add(resource);
        underTest.add(resource);
        underTest.remove(resource);
        underTest.remove(resource);

        assertEquals(false, underTest.contains(resource));
    }

    @Test
    public void resourceAddedTwiceStillContainedAfterRemove() {
        underTest.add(resource);
        underTest.add(resource);
        underTest.remove(resource);

        assertEquals(true, underTest.contains(resource));
    }

    @Test
    public void retainAll() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3, 4));
        boolean result = underTest.retainAll(ResourceSetTestUtils.createResources(1, 2));

        assertEquals(true, result);
        assertEquals(2, underTest.size());
        assertEquals(true, underTest.contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(true, underTest.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(false, underTest.contains(ResourceSetTestUtils.createResource(3)));
        assertEquals(false, underTest.contains(ResourceSetTestUtils.createResource(4)));
    }

    @Test
    public void retainAllFiresResourcesRemovedEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3, 4));
        underTest.addEventHandler(changedHandler);
        underTest.retainAll(ResourceSetTestUtils.createResources(1, 2));

        List<Resource> removedResources = captureOnResourceSetChanged(1,
                changedHandler).getValue().getRemovedResources().toList();

        assertEquals(2, removedResources.size());
        assertEquals(false, removedResources.contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(false, removedResources.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(true, removedResources.contains(ResourceSetTestUtils.createResource(3)));
        assertEquals(true, removedResources.contains(ResourceSetTestUtils.createResource(4)));
    }

    @Test
    public void retainAllWithDoubleResource() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3, 4));
        underTest.addAll(ResourceSetTestUtils.createResources(3));
        boolean result = underTest.retainAll(ResourceSetTestUtils.createResources(1, 2));

        assertEquals(true, result);
        assertEquals(3, underTest.size());
        assertEquals(true, underTest.contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(true, underTest.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(true, underTest.contains(ResourceSetTestUtils.createResource(3)));
        assertEquals(false, underTest.contains(ResourceSetTestUtils.createResource(4)));
    }

    @Test
    public void retainAllWithDoubleResourceFiresResourcesRemovedEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3, 4));
        underTest.addAll(ResourceSetTestUtils.createResources(3));
        underTest.addEventHandler(changedHandler);
        underTest.retainAll(ResourceSetTestUtils.createResources(1, 2));

        List<Resource> removedResources = captureOnResourceSetChanged(1,
                changedHandler).getValue().getRemovedResources().toList();

        assertEquals(1, removedResources.size());
        assertEquals(false, removedResources.contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(false, removedResources.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(false, removedResources.contains(ResourceSetTestUtils.createResource(3)));
        assertEquals(true, removedResources.contains(ResourceSetTestUtils.createResource(4)));
    }

    @Test
    public void retainAllWithoutChangesDoesNotFireResourcesRemovedEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2));
        underTest.addEventHandler(changedHandler);
        underTest.retainAll(ResourceSetTestUtils.createResources(1, 2, 3));

        captureOnResourceSetChanged(0, changedHandler);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        resource = ResourceSetTestUtils.createResource(1);
        underTest = new CountingResourceSet();
    }
}
