/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.core.resources.CategorizableResourceGroupingChange.newGroupChangedDelta;
import static org.thechiselgroup.biomixer.client.core.resources.CategorizableResourceGroupingChange.newGroupCreatedDelta;
import static org.thechiselgroup.biomixer.client.core.resources.CategorizableResourceGroupingChange.newGroupRemovedDelta;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TYPE_1;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TYPE_2;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.captureOnResourceSetChanged;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResource;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResources;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.toResourceSet;
import static org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils.toSet;
import static org.thechiselgroup.biomixer.shared.core.test.AdvancedAsserts.assertMapKeysEqual;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.contains;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;

public class ResourceGroupingTest {

    private static final String GROUP_1_1 = "group1-1";

    private static final String GROUP_1_2 = "group1-2";

    private static final String GROUP_2_1 = "group2-1";

    private static final String GROUP_2_2 = "group2-2";

    private static final String GROUP_2_3 = "group2-3";

    private static final String GROUP_2_4 = "group2-4";

    @Mock
    private ResourceMultiCategorizer categorizer1;

    @Mock
    private ResourceMultiCategorizer categorizer2;

    @Mock
    private ResourceMultiCategorizer categorizer3;

    @Mock
    private ResourceGroupingChangedHandler changeHandler;

    private ResourceGrouping underTest;

    private ResourceSet testResources;

    @Test
    public void addAndRemoveUncategorizableAndCategorizableResourcesEventsFired() {
        setUpCategory(categorizer1, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer1, createResource(TYPE_1, 2), GROUP_1_1);
        setUpNullCategory(categorizer1, createResource(TYPE_1, 3));
        setUpNullCategory(categorizer1, createResource(TYPE_1, 4));
        setUpCategory(categorizer1, createResource(TYPE_1, 5), GROUP_1_1);
        setUpNullCategory(categorizer1, createResource(TYPE_1, 6));
        underTest.setCategorizer(categorizer1);
        testResources.addAll(createResources(TYPE_1, 1, 2, 3, 4));

        underTest.addHandler(changeHandler);

        testResources.change(createResources(TYPE_1, 5, 6),
                createResources(TYPE_1, 1, 3));

        ResourceGroupingChangedEvent event = captureResourceChangedEvent();
        LightweightList<CategorizableResourceGroupingChange> changes = event
                .getChanges();
        UncategorizableResourceGroupingChange uncategorizableResourceChanges = event
                .getUncategorizableChanges();

        assertThat(
                changes,
                containsExactly(
                        newGroupChangedDelta(GROUP_1_1,
                                createResources(TYPE_1, 2, 5),
                                createResources(TYPE_1, 5), null),
                        newGroupChangedDelta(GROUP_1_1,
                                createResources(TYPE_1, 2, 5), null,
                                createResources(TYPE_1, 1))));

        assertThat(uncategorizableResourceChanges.addedResources,
                containsExactly(createResources(TYPE_1, 6)));
        assertThat(uncategorizableResourceChanges.removedResources,
                containsExactly(createResources(TYPE_1, 3)));
    }

    @Test
    public void addAndRemoveUncategorizableAndCategorizableResourcesSetContent() {
        setUpCategory(categorizer1, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer1, createResource(TYPE_1, 2), GROUP_1_1);
        setUpNullCategory(categorizer1, createResource(TYPE_1, 3));
        setUpNullCategory(categorizer1, createResource(TYPE_1, 4));
        setUpCategory(categorizer1, createResource(TYPE_1, 5), GROUP_1_1);
        setUpNullCategory(categorizer1, createResource(TYPE_1, 6));

        underTest.setCategorizer(categorizer1);

        testResources.addAll(createResources(TYPE_1, 1, 2, 3, 4));

        testResources.change(createResources(TYPE_1, 5, 6),
                createResources(TYPE_1, 1, 3));

        assertThat(underTest.getCategorizedResourceSets().get(GROUP_1_1),
                containsExactly(createResources(TYPE_1, 2, 5)));
        assertThat(underTest.getUncategorizableResources(),
                containsExactly(createResources(4, 6)));
    }

    @Test
    public void addOneCategorizableAndOneUncategorizableResourceToEmptyGroupEventFired() {
        setUpCategory(categorizer1, createResource(TYPE_1, 1), GROUP_1_1);
        setUpNullCategory(categorizer1, createResource(TYPE_2, 2));
        underTest.setCategorizer(categorizer1);
        underTest.addHandler(changeHandler);

        ResourceSet resources = createResources(TYPE_1, 1);
        resources.add(createResource(TYPE_2, 2));
        testResources.addAll(resources);

        ResourceGroupingChangedEvent event = captureResourceChangedEvent();

        assertThat(
                event.getChanges(),
                containsExactly(newGroupCreatedDelta(GROUP_1_1,
                        createResources(TYPE_1, 1))));
        assertThat(event.getUncategorizableChanges().addedResources,
                containsExactly(createResources(TYPE_2, 2)));
        assertThat(event.getUncategorizableChanges().removedResources,
                isEmpty(Resource.class));
    }

    @Test
    public void addOneCategorizableAndOneUncategorizableResourceToEmptyGroupSetContent() {
        setUpCategory(categorizer1, createResource(TYPE_1, 1), GROUP_1_1);
        setUpNullCategory(categorizer1, createResource(TYPE_2, 2));
        underTest.setCategorizer(categorizer1);

        ResourceSet resources = createResources(TYPE_1, 1);
        resources.add(createResource(TYPE_2, 2));
        testResources.addAll(resources);

        assertMapKeysEqual(underTest.getCategorizedResourceSets(), GROUP_1_1);
        assertThat(underTest.getCategorizedResourceSets().get(GROUP_1_1),
                containsExactly(createResources(TYPE_1, 1)));
        assertThat(underTest.getUncategorizableResources(),
                containsExactly(createResources(TYPE_2, 2)));
    }

    @Test
    public void addResourceWithMultipleCategoriesCreatesMultipleCategories() {
        setUpCategory(categorizer1, 1, GROUP_1_1, GROUP_1_2);

        testResources.add(createResource(1));

        Map<String, ResourceSet> result = underTest
                .getCategorizedResourceSets();

        assertMapKeysEqual(result, GROUP_1_1, GROUP_1_2);
        assertThat(result.get(GROUP_1_1), containsExactly(createResources(1)));
        assertThat(result.get(GROUP_1_2), containsExactly(createResources(1)));
    }

    @Test
    public void addUncategorizableResourcestoEmptyGroupEventFired() {
        setUpNullCategorizer(categorizer3);
        underTest.setCategorizer(categorizer3);
        underTest.addHandler(changeHandler);

        testResources.addAll(createResources(TYPE_1, 1, 2));
        UncategorizableResourceGroupingChange changes = captureUncategorizableChanges();

        assertThat(changes.resourceSet, containsExactly(testResources));
        assertThat(changes.addedResources.toList(),
                contains(createResource(TYPE_1, 1)));
        assertThat(changes.addedResources.toList(),
                contains(createResource(TYPE_1, 2)));
        assertThat(changes.removedResources, isEmpty(Resource.class));
    }

    @Test
    public void addUncategorizableResourcestoEmptyGroupSetContent() {
        setUpNullCategorizer(categorizer3);
        underTest.setCategorizer(categorizer3);

        testResources.addAll(createResources(TYPE_1, 1, 2));

        assertThat(underTest.getUncategorizableResources(),
                containsExactly(testResources));
    }

    public List<CategorizableResourceGroupingChange> captureChanges() {
        ResourceGroupingChangedEvent resourceChangedEvent = captureResourceChangedEvent();
        return resourceChangedEvent.getChanges().toList();
    }

    public ResourceGroupingChangedEvent captureResourceChangedEvent() {
        ArgumentCaptor<ResourceGroupingChangedEvent> eventCaptor = ArgumentCaptor
                .forClass(ResourceGroupingChangedEvent.class);
        verify(changeHandler, times(1)).onResourceCategoriesChanged(
                eventCaptor.capture());
        ResourceGroupingChangedEvent resourceChangedEvent = eventCaptor
                .getValue();
        return resourceChangedEvent;
    }

    public UncategorizableResourceGroupingChange captureUncategorizableChanges() {
        ResourceGroupingChangedEvent resourceChangedEvent = captureResourceChangedEvent();
        return resourceChangedEvent.getUncategorizableChanges();
    }

    /*
     * TODO: This test was migrated here. Check if case is already covered by
     * other tests.
     */
    @Test
    public void categorizeResources() {
        ResourceSet resources1 = createResources(TYPE_1, 1, 3, 4);
        setUpCategory(categorizer1, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer1, createResource(TYPE_1, 3), GROUP_1_1);
        setUpCategory(categorizer1, createResource(TYPE_1, 4), GROUP_1_1);

        ResourceSet resources2 = createResources(TYPE_2, 2, 4);
        setUpCategory(categorizer1, createResource(TYPE_2, 2), GROUP_1_2);
        setUpCategory(categorizer1, createResource(TYPE_2, 4), GROUP_1_2);

        ResourceSet resources = toResourceSet(resources1, resources2);

        testResources.addAll(resources);
        Map<String, ResourceSet> result = underTest
                .getCategorizedResourceSets();

        assertEquals(2, result.size());
        assertTrue(result.containsKey(GROUP_1_1));
        assertTrue(result.get(GROUP_1_1).containsEqualResources(resources1));
        assertTrue(result.containsKey(GROUP_1_2));
        assertTrue(result.get(GROUP_1_2).containsEqualResources(resources2));
    }

    // XXX
    @Test
    public void changeCategorizerCreatesTwoCategorizedFromTwoUncategorizedEventsFired() {
        setUpNullCategorizer(categorizer2);
        underTest.setCategorizer(categorizer2);
        testResources.addAll(createResources(TYPE_1, 1, 2));

        setUpCategory(categorizer3, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer3, createResource(TYPE_1, 2), GROUP_1_1);

        underTest.addHandler(changeHandler);

        underTest.setCategorizer(categorizer3);

        ResourceGroupingChangedEvent event = captureResourceChangedEvent();
        UncategorizableResourceGroupingChange uncategorizableResourceChanges = event
                .getUncategorizableChanges();
        LightweightList<CategorizableResourceGroupingChange> changes = event
                .getChanges();

        assertThat(
                createResources(TYPE_1, 1, 2),
                containsExactly(uncategorizableResourceChanges.removedResources));
        assertTrue(uncategorizableResourceChanges.addedResources.isEmpty());
        assertThat(
                changes,
                containsExactly(newGroupCreatedDelta(GROUP_1_1,
                        createResources(TYPE_1, 1, 2))));
    }

    @Test
    public void changeCategorizerCreatesTwoCategorizedFromTwoUncategorizedSetContent() {
        setUpNullCategorizer(categorizer2);
        underTest.setCategorizer(categorizer2);
        testResources.addAll(createResources(TYPE_1, 1, 2));

        setUpCategory(categorizer3, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer3, createResource(TYPE_1, 2), GROUP_1_1);

        underTest.setCategorizer(categorizer3);

        assertTrue(underTest.getUncategorizableResources().isEmpty());
        assertMapKeysEqual(underTest.getCategorizedResourceSets(), GROUP_1_1);
        assertThat(underTest.getCategorizedResourceSets().get(GROUP_1_1),
                containsExactly(createResources(TYPE_1, 1, 2)));
    }

    @Test
    public void changeCategorizerCreatesTwoUncategorizedFrom2CategorizedResourcesEventFired() {
        setUpCategory(categorizer2, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer2, createResource(TYPE_1, 2), GROUP_1_1);

        underTest.setCategorizer(categorizer2);
        testResources.addAll(createResources(TYPE_1, 1, 2));

        underTest.addHandler(changeHandler);

        setUpNullCategory(categorizer3, createResource(TYPE_1, 1));
        setUpNullCategory(categorizer3, createResource(TYPE_1, 2));

        underTest.setCategorizer(categorizer3);

        UncategorizableResourceGroupingChange change = captureUncategorizableChanges();

        assertThat(change.addedResources,
                containsExactly(createResources(TYPE_1, 1, 2)));
        assertTrue(change.removedResources.isEmpty());
    }

    @Test
    public void changeCategorizerCreatesTwoUncategorizedFrom2CategorizedResourcesSetContent() {
        testResources.addAll(createResources(TYPE_1, 1, 2));
        setUpCategory(categorizer2, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer2, createResource(TYPE_1, 2), GROUP_1_1);

        underTest.setCategorizer(categorizer2);

        setUpNullCategory(categorizer3, createResource(TYPE_1, 1));
        setUpNullCategory(categorizer3, createResource(TYPE_1, 2));

        underTest.setCategorizer(categorizer3);

        ResourceSet resources = underTest.getUncategorizableResources();
        assertThat(resources, containsExactly(createResources(TYPE_1, 1, 2)));
        assertTrue(underTest.getCategorizedResourceSets().entrySet().isEmpty());
    }

    @Test
    public void changeCategorizerFiresEvents1() {
        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        underTest.addHandler(changeHandler);
        underTest.setCategorizer(categorizer2);

        List<CategorizableResourceGroupingChange> changes = captureChanges();

        assertThat(
                changes,
                containsExactly(
                        newGroupRemovedDelta(GROUP_1_1,
                                createResources(1, 2, 3)),
                        newGroupRemovedDelta(GROUP_1_2, createResources(4, 5)),
                        newGroupCreatedDelta(GROUP_2_1, createResources(1)),
                        newGroupCreatedDelta(GROUP_2_2, createResources(2)),
                        newGroupCreatedDelta(GROUP_2_3, createResources(3, 4)),
                        newGroupCreatedDelta(GROUP_2_4, createResources(4, 5))));
    }

    @Test
    public void changeCategorizerFiresEvents2() {
        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        underTest.setCategorizer(categorizer2);
        underTest.addHandler(changeHandler);
        underTest.setCategorizer(categorizer1);

        List<CategorizableResourceGroupingChange> changes = captureChanges();

        assertThat(
                changes,
                containsExactly(
                        newGroupCreatedDelta(GROUP_1_1,
                                createResources(1, 2, 3)),
                        newGroupCreatedDelta(GROUP_1_2, createResources(4, 5)),
                        newGroupRemovedDelta(GROUP_2_1, createResources(1)),
                        newGroupRemovedDelta(GROUP_2_2, createResources(2)),
                        newGroupRemovedDelta(GROUP_2_3, createResources(3, 4)),
                        newGroupRemovedDelta(GROUP_2_4, createResources(4, 5))));
    }

    @Test
    public void changeCategorizerMixedCategorizationResourcesEventsFired() {
        setUpCategory(categorizer2, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer2, createResource(TYPE_1, 2), GROUP_1_1);
        setUpCategory(categorizer2, createResource(TYPE_1, 3), GROUP_1_1);
        setUpNullCategory(categorizer2, createResource(TYPE_1, 4));
        setUpNullCategory(categorizer2, createResource(TYPE_1, 5));

        underTest.setCategorizer(categorizer2);

        testResources.addAll(createResources(TYPE_1, 1, 2, 3, 4, 5));

        setUpCategory(categorizer3, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer3, createResource(TYPE_1, 2), GROUP_1_1);
        setUpNullCategory(categorizer3, createResource(TYPE_1, 3));
        setUpNullCategory(categorizer3, createResource(TYPE_1, 4));
        setUpCategory(categorizer3, createResource(TYPE_1, 5), GROUP_1_1);

        underTest.addHandler(changeHandler);
        underTest.setCategorizer(categorizer3);

        ResourceGroupingChangedEvent event = captureResourceChangedEvent();

        assertThat(event.getUncategorizableChanges().addedResources,
                containsExactly(createResources(TYPE_1, 3, 4)));
        assertThat(event.getUncategorizableChanges().removedResources,
                containsExactly(createResources(TYPE_1, 4, 5)));
        assertThat(
                event.getChanges(),
                containsExactly(
                        newGroupRemovedDelta(GROUP_1_1,
                                createResources(TYPE_1, 1, 2, 3)),
                        newGroupCreatedDelta(GROUP_1_1,
                                createResources(TYPE_1, 1, 2, 5))));

    }

    @Test
    public void changeCategorizerMixedCategorizationResourcesSetContent() {
        setUpCategory(categorizer2, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer2, createResource(TYPE_1, 2), GROUP_1_1);
        setUpCategory(categorizer2, createResource(TYPE_1, 3), GROUP_1_1);
        setUpNullCategory(categorizer2, createResource(TYPE_1, 4));
        setUpNullCategory(categorizer2, createResource(TYPE_1, 5));

        underTest.setCategorizer(categorizer2);

        testResources.addAll(createResources(TYPE_1, 1, 2, 3, 4, 5));

        setUpCategory(categorizer3, createResource(TYPE_1, 1), GROUP_1_1);
        setUpCategory(categorizer3, createResource(TYPE_1, 2), GROUP_1_1);
        setUpNullCategory(categorizer3, createResource(TYPE_1, 3));
        setUpNullCategory(categorizer3, createResource(TYPE_1, 4));
        setUpCategory(categorizer3, createResource(TYPE_1, 5), GROUP_1_1);

        underTest.setCategorizer(categorizer3);

        Map<String, ResourceSet> categorizedResourceSets = underTest
                .getCategorizedResourceSets();

        assertThat(underTest.getUncategorizableResources(),
                containsExactly(createResources(TYPE_1, 3, 4)));
        assertMapKeysEqual(categorizedResourceSets, GROUP_1_1);
        assertThat(categorizedResourceSets.get(GROUP_1_1),
                containsExactly(createResources(TYPE_1, 1, 2, 5)));
    }

    @Test
    public void changeCategorizerUpdatesCategories1() {
        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        underTest.setCategorizer(categorizer2);

        Map<String, ResourceSet> result = underTest
                .getCategorizedResourceSets();

        assertMapKeysEqual(result, GROUP_2_1, GROUP_2_2, GROUP_2_3, GROUP_2_4);
        assertThat(result.get(GROUP_2_1), containsExactly(createResources(1)));
        assertThat(result.get(GROUP_2_2), containsExactly(createResources(2)));
        assertThat(result.get(GROUP_2_3),
                containsExactly(createResources(3, 4)));
        assertThat(result.get(GROUP_2_4),
                containsExactly(createResources(4, 5)));
    }

    @Test
    public void changeCategorizerUpdatesCategories2() {
        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        underTest.setCategorizer(categorizer2);
        underTest.setCategorizer(categorizer1);

        Map<String, ResourceSet> result = underTest
                .getCategorizedResourceSets();

        assertMapKeysEqual(result, GROUP_1_1, GROUP_1_2);
        assertThat(result.get(GROUP_1_1),
                containsExactly(createResources(1, 2, 3)));
        assertThat(result.get(GROUP_1_2),
                containsExactly(createResources(4, 5)));
    }

    @Test
    public void changeCategorizerUpdatesCategoriesAfterAddAllTwiceAndRemoveAll() {
        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        testResources.addAll(createResources(1, 2));
        testResources.removeAll(createResources(1, 2));
        underTest.setCategorizer(categorizer2);

        Map<String, ResourceSet> result = underTest
                .getCategorizedResourceSets();

        assertMapKeysEqual(result, GROUP_2_3, GROUP_2_4);
        assertThat(result.get(GROUP_2_3),
                containsExactly(createResources(3, 4)));
        assertThat(result.get(GROUP_2_4),
                containsExactly(createResources(4, 5)));
    }

    @Test
    public void changeCategorizerUpdatesCategoriesAfterRemoveAll() {
        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        testResources.removeAll(createResources(1, 2));
        underTest.setCategorizer(categorizer2);

        Map<String, ResourceSet> result = underTest
                .getCategorizedResourceSets();

        assertMapKeysEqual(result, GROUP_2_3, GROUP_2_4);
        assertThat(result.get(GROUP_2_3),
                containsExactly(createResources(3, 4)));
        assertThat(result.get(GROUP_2_4),
                containsExactly(createResources(4, 5)));
    }

    @Test
    public void changeToSameCategorizerDoesNotFireEvent() {
        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        underTest.addHandler(changeHandler);
        underTest.setCategorizer(categorizer1);

        verify(changeHandler, times(0)).onResourceCategoriesChanged(
                any(ResourceGroupingChangedEvent.class));
    }

    @Test
    public void createCategories() {
        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));

        Map<String, ResourceSet> result = underTest
                .getCategorizedResourceSets();

        assertMapKeysEqual(result, GROUP_1_1, GROUP_1_2);
        assertThat(result.get(GROUP_1_1),
                containsExactly(createResources(1, 2, 3)));
        assertThat(result.get(GROUP_1_2),
                containsExactly(createResources(4, 5)));
    }

    @Test
    public void doNotFireResourceCategoryChangesWhenNothingChangesOnRemove() {
        underTest.addHandler(changeHandler);
        testResources.removeAll(Collections.<Resource> emptyList());

        verify(changeHandler, times(0)).onResourceCategoriesChanged(
                any(ResourceGroupingChangedEvent.class));
    }

    @Test
    public void fireResourceCategoryAddedAndChangeChangeOnAdd() {
        Resource resource = createResource(1);

        underTest.addHandler(changeHandler);
        testResources.add(resource);

        List<CategorizableResourceGroupingChange> changes = captureChanges();

        assertThat(
                changes,
                containsExactly(newGroupCreatedDelta(GROUP_1_1,
                        ResourceSetTestUtils.toResourceSet(resource))));
    }

    // TODO test for add all --> multiple categories

    @Test
    public void fireResourceCategoryAddedAndChangedOnAddAll() {
        testResources.addAll(createResources(1, 2));
        underTest.addHandler(changeHandler);
        testResources.addAll(createResources(3, 4, 5));

        List<CategorizableResourceGroupingChange> changes = captureChanges();

        assertThat(
                changes,
                containsExactly(
                        newGroupChangedDelta(GROUP_1_1,
                                createResources(1, 2, 3), createResources(3),
                                null),
                        newGroupCreatedDelta(GROUP_1_2, createResources(4, 5))));
    }

    /**
     * Tests that the resource category added event is fired and contains all
     * resources when fired (and not just later on).
     */
    @Test
    public void fireResourceCategoryAddedEventOnAdd() {
        final boolean[] called = { false };

        underTest.addHandler(new ResourceGroupingChangedHandler() {
            @Override
            public void onResourceCategoriesChanged(
                    ResourceGroupingChangedEvent e) {

                assertThat(
                        e.getChanges(),
                        containsExactly(newGroupCreatedDelta(GROUP_1_1,
                                createResources(1))));

                called[0] = true;
            }
        });

        testResources.add(createResource(1));

        assertEquals(true, called[0]);
    }

    /**
     * Tests that the resource category added event is fired and contains the
     * resource when fired (and not just later on).
     */
    @Test
    public void fireResourceCategoryAddedEventOnAddAll() {
        final boolean[] called = { false };

        underTest.addHandler(new ResourceGroupingChangedHandler() {
            @Override
            public void onResourceCategoriesChanged(
                    ResourceGroupingChangedEvent e) {

                assertThat(
                        e.getChanges().toList(),
                        containsExactly(newGroupCreatedDelta(GROUP_1_1,
                                createResources(1, 2, 3))));

                called[0] = true;
            }
        });

        testResources.addAll(createResources(1, 2, 3));

        assertEquals(true, called[0]);
    }

    @Test
    public void fireResourceCategoryRemovedAndUpdateChangesWhenRemovingOneAndAHalfCategories() {
        ResourceSet allResources = new DefaultResourceSet();
        allResources.addAll(createResources(1, 2, 3, 4));

        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        underTest.addHandler(changeHandler);
        testResources.removeAll(allResources);

        List<CategorizableResourceGroupingChange> changes = captureChanges();

        assertThat(
                changes,
                containsExactly(
                        newGroupRemovedDelta(GROUP_1_1,
                                createResources(1, 2, 3)),
                        newGroupChangedDelta(GROUP_1_2, createResources(5),
                                null, createResources(4))));
    }

    @Test
    public void fireResourceCategoryRemovedChangeOnRemove() {
        Resource resource = createResource(1);

        testResources.add(resource);
        underTest.addHandler(changeHandler);
        testResources.remove(resource);

        List<CategorizableResourceGroupingChange> changes = captureChanges();

        assertThat(
                changes,
                containsExactly(newGroupRemovedDelta(GROUP_1_1,
                        toResourceSet(resource))));
    }

    @Test
    public void fireResourceCategoryRemovedChangeOnRemoveAll() {
        testResources.addAll(createResources(1, 2, 3));
        underTest.addHandler(changeHandler);
        testResources.removeAll(createResources(1, 2, 3));

        List<CategorizableResourceGroupingChange> changes = captureChanges();

        assertThat(
                changes,
                containsExactly(newGroupRemovedDelta(GROUP_1_1,
                        createResources(1, 2, 3))));
    }

    @Test
    public void fireResourceCategoryRemovedChangesWhenRemovingTwoCategories() {
        ResourceSet allResources = new DefaultResourceSet();
        allResources.addAll(createResources(1, 2, 3, 4, 5));

        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        underTest.addHandler(changeHandler);
        testResources.removeAll(allResources);

        List<CategorizableResourceGroupingChange> changes = captureChanges();

        assertThat(
                changes,
                containsExactly(
                        newGroupRemovedDelta(GROUP_1_1,
                                createResources(1, 2, 3)),
                        newGroupRemovedDelta(GROUP_1_2, createResources(4, 5))));
    }

    @Test
    public void getGroupsAfterAddAllAndCategorizerChange() {
        setUpCategory(categorizer1, 1, GROUP_1_1);
        setUpCategory(categorizer1, 2, GROUP_1_2);
        setUpCategory(categorizer1, 3, GROUP_1_1);

        setUpCategory(categorizer2, 1, GROUP_2_2);
        setUpCategory(categorizer2, 2, GROUP_1_2);
        setUpCategory(categorizer2, 3, GROUP_2_1);

        ResourceSet resources = createResources(1, 2, 3);
        testResources.addAll(resources);

        underTest.setCategorizer(categorizer2);

        Set<String> result = underTest.getGroupIds(createResources(2, 3));

        assertThat(result, containsExactly(GROUP_2_1, GROUP_1_2));
    }

    @Test
    public void getGroupsAfterAddAllAndRemove() {
        setUpCategory(categorizer1, 1, GROUP_1_1);
        setUpCategory(categorizer1, 2, GROUP_1_2);
        setUpCategory(categorizer1, 3, GROUP_1_1);
        ResourceSet resources = createResources(1, 2, 3);
        testResources.addAll(resources);
        testResources.remove(createResource(3));

        // 3 is not contained any more
        Set<String> result = underTest.getGroupIds(createResources(2, 3));

        assertThat(result, containsExactly(GROUP_1_2));
    }

    @Test
    public void getGroupsAfterAddAllReturningExcludingOneGroup() {
        setUpCategory(categorizer1, 1, GROUP_1_1);
        setUpCategory(categorizer1, 2, GROUP_1_2);
        ResourceSet resources = createResources(1, 2);
        testResources.addAll(resources);

        Set<String> result = underTest.getGroupIds(createResources(1));

        assertThat(result, containsExactly(GROUP_1_1));
    }

    @Test
    public void getGroupsAfterAddAllReturningNothingForNotIncludedResource() {
        setUpCategory(categorizer1, 1, GROUP_1_1);
        ResourceSet resources = createResources(1);
        resources.addAll(resources);

        assertTrue(underTest.getGroupIds(createResources(2)).isEmpty());
    }

    @Test
    public void getGroupsAfterAddAllReturningOneGroupForSeveralResources() {
        setUpCategory(categorizer1, 1, GROUP_1_1);
        setUpCategory(categorizer1, 2, GROUP_1_1);
        ResourceSet resources = createResources(1, 2);
        testResources.addAll(resources);

        assertThat(underTest.getGroupIds(resources), containsExactly(GROUP_1_1));
    }

    @Test
    public void getGroupsAfterAddAllReturningSingleGroupForSingleResource() {
        setUpCategory(categorizer1, 1, GROUP_1_1);
        ResourceSet resources = createResources(1);
        testResources.addAll(resources);

        assertThat(underTest.getGroupIds(resources), containsExactly(GROUP_1_1));
    }

    @Test
    public void getGroupsAfterAddAllReturningTwoGroupsForSingleResource() {
        setUpCategory(categorizer1, 1, GROUP_1_1, GROUP_1_2);
        ResourceSet resources = createResources(1);
        testResources.addAll(resources);

        assertThat(underTest.getGroupIds(resources),
                containsExactly(GROUP_1_1, GROUP_1_2));
    }

    @Test
    public void noResourceSetEventsFiredOnCompleteCategoryRemovalViaRemove() {
        testResources.add(createResource(1));
        ResourceSet categorizedResources = underTest
                .getCategorizedResourceSets().get(GROUP_1_1);
        ResourceSetChangedEventHandler resourcesChangedHandler = mock(ResourceSetChangedEventHandler.class);
        categorizedResources.addEventHandler(resourcesChangedHandler);
        testResources.remove(createResource(1));

        captureOnResourceSetChanged(0, resourcesChangedHandler);
    }

    @Test
    public void noResourceSetEventsFiredOnCompleteCategoryRemovalViaRemoveAll() {
        testResources.addAll(createResources(1, 2, 3));
        ResourceSet categorizedResources = underTest
                .getCategorizedResourceSets().get(GROUP_1_1);
        ResourceSetChangedEventHandler resourcesChangedHandler = mock(ResourceSetChangedEventHandler.class);
        categorizedResources.addEventHandler(resourcesChangedHandler);
        testResources.removeAll(createResources(1, 2, 3));

        captureOnResourceSetChanged(0, resourcesChangedHandler);
    }

    @Test
    public void removeAndAddUncategorizableToSetWithTwoUncategorizableEventsFired() {
        setUpNullCategorizer(categorizer3);
        underTest.setCategorizer(categorizer3);
        testResources.addAll(createResources(TYPE_1, 1, 2));
        underTest.addHandler(changeHandler);

        testResources.change(createResources(TYPE_1, 3),
                createResources(TYPE_1, 1));
        UncategorizableResourceGroupingChange changes = captureUncategorizableChanges();

        assertThat(changes.addedResources,
                containsExactly(createResources(TYPE_1, 3)));
        assertThat(changes.removedResources,
                containsExactly(createResources(TYPE_1, 1)));
    }

    @Test
    public void removeAndAddUncategorizableToSetWithTwoUncategorizableSetContent() {
        setUpNullCategorizer(categorizer3);
        underTest.setCategorizer(categorizer3);
        testResources.addAll(createResources(TYPE_1, 1, 2));

        testResources.change(createResources(TYPE_1, 3),
                createResources(TYPE_1, 1));

        assertThat(underTest.getUncategorizableResources(),
                containsExactly(createResources(TYPE_1, 2, 3)));
    }

    @Test
    public void removeOneUncategorizedResourceAndOneCategorizedResourceFromGroupingEventFired() {
        setUpCategory(categorizer1, createResource(TYPE_1, 1), GROUP_1_1);
        setUpNullCategory(categorizer1, createResource(TYPE_1, 2));
        underTest.setCategorizer(categorizer1);
        testResources.addAll(createResources(TYPE_1, 1, 2));

        underTest.addHandler(changeHandler);
        testResources.removeAll(createResources(TYPE_1, 1, 2));

        ResourceGroupingChangedEvent event = captureResourceChangedEvent();

        assertThat(
                event.getChanges(),
                containsExactly(newGroupRemovedDelta(GROUP_1_1,
                        createResources(TYPE_1, 1))));
        assertTrue(event.getUncategorizableChanges().addedResources.isEmpty());
        assertThat(event.getUncategorizableChanges().removedResources,
                containsExactly(createResources(TYPE_1, 2)));
    }

    @Test
    public void removeOneUncategorizedResourceAndOneCategorizedResourceFromGroupingSetContent() {
        setUpCategory(categorizer1, createResource(TYPE_1, 1), GROUP_1_1);
        setUpNullCategory(categorizer1, createResource(TYPE_1, 2));
        underTest.setCategorizer(categorizer1);
        testResources.addAll(createResources(TYPE_1, 1, 2));

        testResources.removeAll(createResources(TYPE_1, 1, 2));

        assertThat(underTest.containsGroup(GROUP_1_1), is(false));
        assertTrue(underTest.getUncategorizableResources().isEmpty());
    }

    @Test
    public void removeResourceSet() {
        testResources.addAll(ResourceSetTestUtils
                .createResources(1, 2, 3, 4, 5));
        testResources.removeAll(createResources(1, 2, 3));

        Map<String, ResourceSet> result = underTest
                .getCategorizedResourceSets();

        assertEquals(1, result.size());
        assertTrue(result.containsKey(GROUP_1_2));
        assertThat(result.get(GROUP_1_2),
                containsExactly(createResources(4, 5)));
    }

    @Test
    public void removeUncategorizableResourcesFromGroupFireEvent() {
        testResources.addAll(createResources(TYPE_1, 1, 2));
        setUpNullCategorizer(categorizer3);
        underTest.setCategorizer(categorizer3);

        underTest.addHandler(changeHandler);
        testResources.removeAll(createResources(TYPE_1, 1, 2));

        UncategorizableResourceGroupingChange changes = captureUncategorizableChanges();

        assertThat(changes.removedResources,
                containsExactly(createResources(TYPE_1, 1, 2)));
        assertTrue(changes.addedResources.isEmpty());
    }

    @Test
    public void removeUncategorizableResourcesFromGroupSetContent() {
        setUpNullCategorizer(categorizer3);
        underTest.setCategorizer(categorizer3);
        testResources.addAll(createResources(TYPE_1, 1, 2));

        testResources.removeAll(createResources(TYPE_1, 1, 2));

        ResourceSet uncategorizableResources = underTest
                .getUncategorizableResources();

        assertTrue(uncategorizableResources.isEmpty());
        assertTrue(underTest.getResourceSet().isEmpty());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new ResourceGrouping(categorizer1,
                new DefaultResourceSetFactory());

        testResources = underTest.getResourceSet();

        setUpCategory(categorizer1, 1, GROUP_1_1);
        setUpCategory(categorizer1, 2, GROUP_1_1);
        setUpCategory(categorizer1, 3, GROUP_1_1);
        setUpCategory(categorizer1, 4, GROUP_1_2);
        setUpCategory(categorizer1, 5, GROUP_1_2);

        setUpCategory(categorizer2, 1, GROUP_2_1);
        setUpCategory(categorizer2, 2, GROUP_2_2);
        setUpCategory(categorizer2, 3, GROUP_2_3);
        setUpCategory(categorizer2, 4, GROUP_2_3, GROUP_2_4);
        setUpCategory(categorizer2, 5, GROUP_2_4);

        setUpNullCategorizer(categorizer3);
    }

    private void setUpCategory(ResourceMultiCategorizer categorizer,
            int resourceId, String... category) {

        setUpCategory(categorizer, createResource(resourceId), category);
    }

    public void setUpCategory(ResourceMultiCategorizer categorizer,
            Resource resource, String... category) {

        when(categorizer.getCategories(resource)).thenReturn(toSet(category));
        when(categorizer.canCategorize(resource)).thenReturn(true);
    }

    public void setUpNullCategorizer(ResourceMultiCategorizer categorizer) {
        when(categorizer.getCategories(any(Resource.class))).thenReturn(null);
        when(categorizer.canCategorize(any(Resource.class))).thenReturn(false);
    }

    public void setUpNullCategory(ResourceMultiCategorizer categorizer,
            Resource resource) {
        when(categorizer.getCategories(resource)).thenReturn(null);
        when(categorizer.canCategorize(resource)).thenReturn(false);
    }
}
