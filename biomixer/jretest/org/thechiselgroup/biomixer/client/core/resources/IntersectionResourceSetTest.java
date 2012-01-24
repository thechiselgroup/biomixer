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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.captureOnResourceSetChanged;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.verifyOnResourcesAdded;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IntersectionResourceSetTest {

    @Mock
    private ResourceSetChangedEventHandler resourceSetChangedHandler;

    private IntersectionResourceSet underTest;

    @Test
    public void addMultipleResourcesToContainedResourceSet() {
        ResourceSet resources = ResourceSetTestUtils.createLabeledResources(1,
                2, 3);

        underTest.addResourceSet(resources);

        resources.addAll(ResourceSetTestUtils.createResources(4, 5));

        assertTrue(underTest.contains(ResourceSetTestUtils.createResource(4)));
        assertTrue(underTest.contains(ResourceSetTestUtils.createResource(5)));
    }

    @Test
    public void addResourceSetCreatesIntersection() {
        underTest.addResourceSet(ResourceSetTestUtils.createLabeledResources(1,
                2, 3));
        underTest.addResourceSet(ResourceSetTestUtils.createLabeledResources(3,
                4, 5));

        assertThat(underTest,
                containsExactly(ResourceSetTestUtils.createResources(3)));
    }

    @Test
    public void containsResourcesAddedToChildren() {
        ResourceSet resources = ResourceSetTestUtils.createLabeledResources(1,
                2, 3);

        underTest.addResourceSet(resources);
        resources.add(ResourceSetTestUtils.createResource(5));

        assertTrue(underTest.containsEqualResources(resources));
    }

    @Test
    public void doesNotContainResourcesRemovedFromChildren() {
        ResourceSet resources = ResourceSetTestUtils.createLabeledResources(1,
                2, 3);

        underTest.addResourceSet(resources);
        resources.remove(ResourceSetTestUtils.createResource(1));

        assertEquals(2, underTest.size());
        assertTrue(underTest.containsAll(resources));
        assertFalse(underTest.contains(ResourceSetTestUtils.createResource(1)));
    }

    @Test
    public void doesRemoveDuplicateResourceOnRemoveInOneResourceSet() {
        ResourceSet resources = ResourceSetTestUtils.createLabeledResources(1,
                2, 3);

        underTest.addResourceSet(resources);
        underTest.addResourceSet(ResourceSetTestUtils.createLabeledResources(3,
                4, 5));
        resources.remove(ResourceSetTestUtils.createResource(3));

        assertThat(underTest,
                containsExactly(ResourceSetTestUtils.createResources()));
    }

    @Test
    public void doNotFireEventWhenResourceSetAddedButNoRemovedResources() {
        underTest.addResourceSet(ResourceSetTestUtils.createLabeledResources(1,
                2, 3));
        underTest.addEventHandler(resourceSetChangedHandler);
        underTest.addResourceSet(ResourceSetTestUtils.createLabeledResources(1,
                2, 3, 4));

        captureOnResourceSetChanged(0, resourceSetChangedHandler);
    }

    @Test
    public void fireEventWhenResourceSetAdded() {
        underTest.addEventHandler(resourceSetChangedHandler);
        underTest.addResourceSet(ResourceSetTestUtils.createLabeledResources(1,
                2, 3));

        verifyOnResourcesAdded(ResourceSetTestUtils.createResources(1, 2, 3),
                resourceSetChangedHandler);
    }

    @Test
    public void noContainmenChangeWhenResourceAddedAfterRemove() {
        Resource addedResource = ResourceSetTestUtils.createResource(8);
        ResourceSet resources = ResourceSetTestUtils.createLabeledResources(1,
                2, 3);

        underTest.addResourceSet(resources);
        underTest.removeResourceSet(resources);
        resources.add(addedResource);

        assertThat(underTest,
                containsExactly(ResourceSetTestUtils.createResources()));
    }

    @Test
    public void noFailureOnRemoveInvalidResourceSet() {
        ResourceSet resources1 = ResourceSetTestUtils.createLabeledResources(1,
                2, 3);

        underTest.removeResourceSet(resources1);
    }

    @Test
    public void removeResourcesDoesNotRemoveDuplicateResource() {
        ResourceSet resources = ResourceSetTestUtils.createLabeledResources(3,
                4, 5);

        underTest.addResourceSet(ResourceSetTestUtils.createLabeledResources(1,
                2, 3));
        underTest.addResourceSet(resources);
        underTest.removeResourceSet(resources);

        assertThat(underTest,
                containsExactly(ResourceSetTestUtils.createResources(1, 2, 3)));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new IntersectionResourceSet(new DefaultResourceSet());
    }
}
