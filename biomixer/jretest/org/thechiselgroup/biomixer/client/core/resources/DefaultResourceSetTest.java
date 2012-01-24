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
import static org.junit.Assert.assertThat;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.captureOnResourceSetChanged;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.verifyOnResourcesRemoved;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers;

public class DefaultResourceSetTest extends AbstractResourceSetTest {

    private ResourceSet underTest;

    @Test
    public void addAllFiresEvent() {
        underTest.addEventHandler(changedHandler);
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));

        captureOnResourceSetChanged(1, changedHandler);
    }

    @Test
    public void addAllWithoutChangesDoesNotFireEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));
        underTest.addEventHandler(changedHandler);
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));

        verifyChangeHandlerNotCalled();
    }

    @Test
    public void addFiresEvent() {
        registerEventHandler();
        underTest.add(ResourceSetTestUtils.createResource(1));

        captureOnResourceSetChanged(1, changedHandler);
    }

    @Test
    public void addResourceAffectsContainsAndSize() {
        underTest.add(ResourceSetTestUtils.createResource(1));

        assertEquals(1, underTest.size());
        assertEquals(true,
                underTest.contains(ResourceSetTestUtils.createResource(1)));
    }

    @Test
    public void addResourcesAffectsContainsAndSize() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));

        assertEquals(3, underTest.size());
        assertEquals(true,
                underTest.contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(true,
                underTest.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(true,
                underTest.contains(ResourceSetTestUtils.createResource(3)));
    }

    @Test
    public void changeAddsAndRemovesResources() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2));
        underTest.change(ResourceSetTestUtils.createResources(3),
                ResourceSetTestUtils.createResources(1));

        assertSizeEquals(2);
        assertContainsResource(1, false);
        assertContainsResource(2, true);
        assertContainsResource(3, true);
    }

    @Test
    public void changeFiresSingleEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2));
        registerEventHandler();
        underTest.change(ResourceSetTestUtils.createResources(3),
                ResourceSetTestUtils.createResources(1));

        captureOnResourceSetChanged(1, changedHandler);
    }

    @Test
    public void hasLabelIsFalseWhenLabelNull() {
        underTest.setLabel(null);

        assertEquals(false, underTest.hasLabel());
    }

    @Test
    public void hasLabelIsTrueWhenLabelText() {
        underTest.setLabel("some text");

        assertEquals(true, underTest.hasLabel());
    }

    @Test
    public void intersectionMixedResourcesUnderTest() {
        underTest.addAll(ResourceSetTestUtils.createResources(3, 1, 4, 2));

        LightweightList<Resource> paramList = CollectionFactory
                .createLightweightList();
        paramList.add(ResourceSetTestUtils.createResource(3));
        paramList.add(ResourceSetTestUtils.createResource(4));
        paramList.add(ResourceSetTestUtils.createResource(5));

        LightweightList<Resource> intersection = underTest
                .getIntersection(paramList);

        assertEquals(2, intersection.size());
        assertEquals(ResourceSetTestUtils.createResource(3),
                intersection.get(0));
        assertEquals(ResourceSetTestUtils.createResource(4),
                intersection.get(1));
    }

    @Test
    public void intersectionMixedResourcesUnderTestAndParameter() {
        underTest.addAll(ResourceSetTestUtils.createResources(3, 1, 4, 2, 11,
                12, 15, 13, 10));

        LightweightList<Resource> paramList = CollectionFactory
                .createLightweightList();
        paramList.add(ResourceSetTestUtils.createResource(1));
        paramList.add(ResourceSetTestUtils.createResource(11));
        paramList.add(ResourceSetTestUtils.createResource(13));
        paramList.add(ResourceSetTestUtils.createResource(3));
        paramList.add(ResourceSetTestUtils.createResource(4));
        paramList.add(ResourceSetTestUtils.createResource(5));
        paramList.add(ResourceSetTestUtils.createResource(9));

        LightweightList<Resource> intersection = underTest
                .getIntersection(paramList);

        assertEquals(5, intersection.size());
        assertEquals(ResourceSetTestUtils.createResource(1),
                intersection.get(0));
        assertEquals(ResourceSetTestUtils.createResource(11),
                intersection.get(1));
        assertEquals(ResourceSetTestUtils.createResource(13),
                intersection.get(2));
        assertEquals(ResourceSetTestUtils.createResource(3),
                intersection.get(3));
        assertEquals(ResourceSetTestUtils.createResource(4),
                intersection.get(4));
    }

    @Test
    public void intersectionOrder() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3, 4));

        LightweightList<Resource> paramList = CollectionFactory
                .createLightweightList();
        paramList.add(ResourceSetTestUtils.createResource(2));
        paramList.add(ResourceSetTestUtils.createResource(4));
        paramList.add(ResourceSetTestUtils.createResource(5));

        LightweightList<Resource> intersection = underTest
                .getIntersection(paramList);

        assertEquals(2, intersection.size());
        assertEquals(ResourceSetTestUtils.createResource(2),
                intersection.get(0));
        assertEquals(ResourceSetTestUtils.createResource(4),
                intersection.get(1));

    }

    @Test
    public void intersectionSimple() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));

        LightweightList<Resource> paramList = CollectionFactory
                .createLightweightList();
        paramList.add(ResourceSetTestUtils.createResource(3));
        paramList.add(ResourceSetTestUtils.createResource(4));
        paramList.add(ResourceSetTestUtils.createResource(5));

        LightweightList<Resource> intersection = underTest
                .getIntersection(paramList);

        assertEquals(1, intersection.size());
        assertEquals(ResourceSetTestUtils.createResource(3),
                intersection.get(0));

    }

    @Test
    public void invertAllAffectsContainsAndSize() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2));
        underTest.invertAll(ResourceSetTestUtils.createResources(2, 3));

        assertEquals(2, underTest.size());
        assertEquals(true,
                underTest.contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(false,
                underTest.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(true,
                underTest.contains(ResourceSetTestUtils.createResource(3)));
    }

    @Test
    public void invertAllFiresEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2));
        underTest.addEventHandler(changedHandler);
        underTest.invertAll(ResourceSetTestUtils.createResources(2, 3));

        ResourceSetChangedEvent event = captureOnResourceSetChanged(1,
                changedHandler).getValue();

        assertThat(event.getAddedResources().toList(),
                CollectionMatchers.containsExactly(ResourceSetTestUtils
                        .createResources(3)));
        assertThat(event.getRemovedResources().toList(),
                CollectionMatchers.containsExactly(ResourceSetTestUtils
                        .createResources(2)));
    }

    @Test
    public void removeAllFiresEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));
        underTest.addEventHandler(changedHandler);
        underTest.removeAll(ResourceSetTestUtils.createResources(1, 2, 3));

        captureOnResourceSetChanged(1, changedHandler);
    }

    @Test
    public void removeAllWithoutChangesDoesNotFireEvent() {
        underTest.addEventHandler(changedHandler);
        underTest.removeAll(ResourceSetTestUtils.createResources(1, 2, 3));

        verifyChangeHandlerNotCalled();
    }

    @Test
    public void removeFiresEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));
        underTest.addEventHandler(changedHandler);
        underTest.remove(ResourceSetTestUtils.createResource(1));

        captureOnResourceSetChanged(1, changedHandler);
    }

    @Test
    public void removeResourceAffectsContainsAndSize() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));
        underTest.remove(ResourceSetTestUtils.createResource(1));

        assertEquals(2, underTest.size());
        assertEquals(false,
                underTest.contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(true,
                underTest.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(true,
                underTest.contains(ResourceSetTestUtils.createResource(3)));
    }

    @Test
    public void removeResourcesAffectsContainsAndSize() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));
        underTest.removeAll(ResourceSetTestUtils.createResources(1, 2, 3));

        assertEquals(0, underTest.size());
        assertEquals(false,
                underTest.contains(ResourceSetTestUtils.createResource(1)));
        assertEquals(false,
                underTest.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(false,
                underTest.contains(ResourceSetTestUtils.createResource(3)));
    }

    @Test
    public void resourcesAddedEventOnlyContainAddedResources() {
        underTest.add(ResourceSetTestUtils.createResource(1));
        underTest.addEventHandler(changedHandler);
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3));

        verifyOnResourcesAdded(2, 3);
    }

    @Test
    public void resourcesRemovedEventOnlyContainsRemovedResources() {
        underTest.addAll(ResourceSetTestUtils.createResources(2, 3));
        underTest.addEventHandler(changedHandler);
        underTest.removeAll(ResourceSetTestUtils.createResources(1, 2, 3));

        verifyOnResourcesRemoved(ResourceSetTestUtils.createResources(2, 3),
                changedHandler);
    }

    @Test
    public void retainAll() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3, 4));
        boolean result = underTest.retainAll(ResourceSetTestUtils
                .createResources(1, 2));

        assertEquals(true, result);
        assertEquals(2, underTest.size());
        assertContainsResource(1, true);
        assertEquals(true,
                underTest.contains(ResourceSetTestUtils.createResource(2)));
        assertEquals(false,
                underTest.contains(ResourceSetTestUtils.createResource(3)));
        assertEquals(false,
                underTest.contains(ResourceSetTestUtils.createResource(4)));
    }

    @Test
    public void retainAllFiresEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2, 3, 4));
        underTest.addEventHandler(changedHandler);
        underTest.retainAll(ResourceSetTestUtils.createResources(1, 2));

        verifyOnResourcesRemoved(ResourceSetTestUtils.createResources(3, 4),
                changedHandler);
    }

    @Test
    public void retainAllWithoutChangesDoesNotFireEvent() {
        underTest.addAll(ResourceSetTestUtils.createResources(1, 2));
        underTest.addEventHandler(changedHandler);
        underTest.retainAll(ResourceSetTestUtils.createResources(1, 2, 3));

        verifyChangeHandlerNotCalled();
    }

    @Test
    public void returnEmptyStringIfLabelNull() {
        underTest.setLabel(null);

        assertEquals("", underTest.getLabel());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new DefaultResourceSet();
        underTestAsResourceSet = underTest;
    }
}
