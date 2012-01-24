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
package org.thechiselgroup.biomixer.client.core.visualization.model.extensions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;

public class DefaultResourceModelTest {

    private DefaultResourceModel underTest;

    @Mock
    private PersistableRestorationService restorationManager;

    @Test
    public void addResourcesAddsToAllResources() {
        ResourceSet resources1 = ResourceSetTestUtils.createResources("test",
                1, 2, 3);
        ResourceSet resources2 = ResourceSetTestUtils.createResources("test",
                3, 4, 5);

        underTest.addUnnamedResources(resources1);
        underTest.addUnnamedResources(resources2);
        ResourceSet allResources = underTest.getResources();

        assertEquals(5, allResources.size());
        for (Resource resource : resources1) {
            assertTrue(allResources.contains(resource));
        }
        for (Resource resource : resources2) {
            assertTrue(allResources.contains(resource));
        }
    }

    @Test
    public void addResourceSetsAddsToAllResources() {
        ResourceSet resources1 = ResourceSetTestUtils.createResources("test",
                1, 2, 3);
        ResourceSet resources2 = ResourceSetTestUtils.createResources("test",
                3, 4, 5);

        underTest.addResourceSet(resources1);
        underTest.addResourceSet(resources2);
        ResourceSet allResources = underTest.getResources();

        assertEquals(5, allResources.size());
        for (Resource resource : resources1) {
            assertTrue(allResources.contains(resource));
        }
        for (Resource resource : resources2) {
            assertTrue(allResources.contains(resource));
        }
    }

    @Test
    public void addUnlabeledSetContentsToAutomaticResourceSet() {
        ResourceSet resources = ResourceSetTestUtils.createResources(1);
        assertEquals(false, resources.hasLabel());

        underTest.addResourceSet(resources);

        assertThat(
                underTest.getAutomaticResourceSet().contains(
                        ResourceSetTestUtils.createResource(1)), is(true));
    }

    @Test
    public void containsAddedLabeledResources() {
        ResourceSet resources = ResourceSetTestUtils.createLabeledResources(1,
                2, 3);

        underTest.addResourceSet(resources);

        assertEquals(true, underTest.containsResourceSet(resources));
    }

    @Test
    public void containsAddedResources() {
        ResourceSet resources = ResourceSetTestUtils.createResources(1, 2, 3);

        underTest.addUnnamedResources(resources);

        assertEquals(true, underTest.containsResources(resources));
    }

    @Test
    public void disposeShouldRemoveResourceHooks() {
        DefaultResourceSet resources = new DefaultResourceSet();
        resources.add(ResourceSetTestUtils.createResource(3));
        resources.add(ResourceSetTestUtils.createResource(4));

        underTest.addResourceSet(resources);
        underTest.dispose();

        assertEquals(0, resources.getHandlerCount(ResourceSetChangedEvent.TYPE));
    }

    @Test
    public void doesNotContainResourceSetAfterAddingResources() {
        ResourceSet resources = ResourceSetTestUtils.createResources(1, 2, 3);
        resources.setLabel("test");

        underTest.addUnnamedResources(resources);

        assertEquals(false, underTest.containsResourceSet(resources));
    }

    @Test
    public void getResourcesHasLabel() {
        assertEquals(true, underTest.getResources().hasLabel());
    }

    @Test
    public void removeLabeledResourceSetDoesNotRemoveDuplicateResources() {
        ResourceSet resources1 = ResourceSetTestUtils.createLabeledResources(1,
                2, 3);
        ResourceSet resources2 = ResourceSetTestUtils.createLabeledResources(3,
                4, 5);

        underTest.addResourceSet(resources1);
        underTest.addResourceSet(resources2);
        underTest.removeResourceSet(resources2);
        ResourceSet allResources = underTest.getResources();

        assertEquals(3, allResources.size());
        for (Resource resource : resources1) {
            assertTrue(allResources.contains(resource));
        }
    }

    @Test
    public void removeResourcesDoesRemoveDuplicateResources() {
        ResourceSet resources1 = ResourceSetTestUtils.createResources(1, 2, 3);
        ResourceSet resources2 = ResourceSetTestUtils.createResources(3, 4, 5);

        underTest.addUnnamedResources(resources1);
        underTest.addUnnamedResources(resources2);
        underTest.removeUnnamedResources(resources2);
        ResourceSet allResources = underTest.getResources();

        assertEquals(2, allResources.size());
        assertEquals(true,
                allResources.contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(true,
                allResources.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(false,
                allResources.contains(ResourceSetTestUtils.createResource(3)));
        assertEquals(false,
                allResources.contains(ResourceSetTestUtils.createResource(4)));
        assertEquals(false,
                allResources.contains(ResourceSetTestUtils.createResource(5)));
    }

    @Test
    public void restoreFromMementoAddsAutomaticResourcesToAllResources() {
        Memento state = new Memento();

        state.setValue(DefaultResourceModel.MEMENTO_AUTOMATIC_RESOURCES, 0);
        state.setValue(DefaultResourceModel.MEMENTO_RESOURCE_SET_COUNT, 0);

        ResourceSetAccessor accessor = mock(ResourceSetAccessor.class);
        when(accessor.getResourceSet(0)).thenReturn(
                ResourceSetTestUtils.createResources(1));
        when(accessor.getResourceSet(1)).thenReturn(
                ResourceSetTestUtils.createResources());

        underTest.restore(state, restorationManager, accessor);

        assertEquals(
                true,
                underTest.getResources().contains(
                        ResourceSetTestUtils.createResource(1)));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new DefaultResourceModel(new DefaultResourceSetFactory());
    }

}
