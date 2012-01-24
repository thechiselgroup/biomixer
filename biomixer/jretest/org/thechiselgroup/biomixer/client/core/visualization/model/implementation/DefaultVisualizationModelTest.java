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
package org.thechiselgroup.biomixer.client.core.visualization.model.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TEXT_PROPERTY_1;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TEXT_PROPERTY_2;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TYPE_1;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TYPE_2;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createLabeledResources;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResource;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResources;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.toResourceSet;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemTestUtils.containsVisualItemsForExactResourceSets;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceByPropertyMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceByUriMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.behaviors.CompositeVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Status;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.FixedValueResolver;

public class DefaultVisualizationModelTest {

    private Slot slot;

    private DefaultVisualizationModel underTest;

    private DefaultVisualizationModelTestHelper helper;

    @Test
    public void addResourcesToInitialContentCreatesVisualItems() {
        DefaultVisualizationModel model = new DefaultVisualizationModel(
                helper.getViewContentDisplay(), new DefaultResourceSet(),
                new DefaultResourceSet(), new CompositeVisualItemBehavior(),
                mock(ErrorHandler.class), new DefaultResourceSetFactory(),
                new ResourceByUriMultiCategorizer());

        Resource r1 = createResource(1);
        r1.putValue(TEXT_PROPERTY_1, "value1-1");
        r1.putValue(TEXT_PROPERTY_2, "value2");

        Resource r2 = createResource(2);
        r2.putValue(TEXT_PROPERTY_1, "value1-2");
        r2.putValue(TEXT_PROPERTY_2, "value2");

        model.getContentResourceSet().addAll(toResourceSet(r1, r2));

        model.setCategorizer(new ResourceByPropertyMultiCategorizer(
                TEXT_PROPERTY_2));

        assertThat(model.getFullVisualItemContainer().getVisualItems()
                .getFirstElement().getResources(), containsExactly(r1, r2));
    }

    private VisualItem getFirstVisualItem() {
        return underTest.getFullVisualItemContainer().getVisualItems()
                .getFirstElement();
    }

    @Test
    public void grouping() {
        Resource r1 = createResource(1);
        r1.putValue(TEXT_PROPERTY_1, "value1-1");
        r1.putValue(TEXT_PROPERTY_2, "value2");

        Resource r2 = createResource(2);
        r2.putValue(TEXT_PROPERTY_1, "value1-2");
        r2.putValue(TEXT_PROPERTY_2, "value2");

        helper.getContainedResources().addAll(toResourceSet(r1, r2));

        underTest.setCategorizer(new ResourceByPropertyMultiCategorizer(
                TEXT_PROPERTY_2));

        assertThat(getFirstVisualItem().getResources(), containsExactly(r1, r2));
    }

    @Test
    public void groupingChangeChangesCategory() {
        Resource resource = createResource(1);
        resource.putValue(TEXT_PROPERTY_1, "category1");
        resource.putValue(TEXT_PROPERTY_2, "category2");

        underTest.setCategorizer(new ResourceByPropertyMultiCategorizer(
                TEXT_PROPERTY_1));
        helper.getContainedResources().add(resource);
        underTest.setCategorizer(new ResourceByPropertyMultiCategorizer(
                TEXT_PROPERTY_2));

        assertEquals("category2", getFirstVisualItem().getId());
    }

    /**
     * Removing a resource item and adding another resource item, both with the
     * same category, in one operation caused a bug.
     */
    @Test
    public void groupingChangeWithRemovingAndAddingSameCategory() {
        Resource resource = createResource(1);
        resource.putValue(TEXT_PROPERTY_1, "category1");
        resource.putValue(TEXT_PROPERTY_2, "category1");

        underTest.setCategorizer(new ResourceByPropertyMultiCategorizer(
                TEXT_PROPERTY_1));
        helper.getContainedResources().add(resource);
        underTest.setCategorizer(new ResourceByPropertyMultiCategorizer(
                TEXT_PROPERTY_2));

        assertEquals("category1", getFirstVisualItem().getId());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        slot = new Slot("1", "Description", DataType.TEXT);

        helper = new DefaultVisualizationModelTestHelper();
        helper.setSlots(slot);
        underTest = helper.createTestVisualizationModel();
        underTest.setResolver(slot, new FixedValueResolver("a", DataType.TEXT));
    }

    /**
     * @see <a
     *      href="http://code.google.com/p/choosel/issues/detail?id=149">"Issue 149"</a>
     */
    @Test
    public void visualItemHighlightingSetContainsResourcesThatAreAddedToVisualizationContentSetAndWereAlreadyInHighlightingSet() {
        ResourceSet originalResources = createResources(TYPE_1, 1);
        ResourceSet addedResources = createResources(TYPE_1, 2);

        helper.getContainedResources().addAll(originalResources);
        helper.getHighlightedResources().addAll(addedResources);
        helper.getContainedResources().addAll(addedResources);

        VisualItem item = getFirstVisualItem();

        assertEquals(Status.PARTIAL, item.getStatus(Subset.HIGHLIGHTED));
        assertThat(item.getResources(Subset.HIGHLIGHTED),
                containsExactly(addedResources));
    }

    @Test
    public void visualItemHighlightingSetContainsResourcesThatAreAddedToVisualizationHighlightingSet() {
        ResourceSet resources = createResources(1);

        helper.getContainedResources().addAll(resources);
        helper.getHighlightedResources().addAll(resources);

        assertThat(getFirstVisualItem().getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void visualItemHighlightingSetContainsResourcesThatAreInVisualizationHighlightingSetWhenVisualItemIsCreated() {
        ResourceSet resources = createResources(TYPE_1, 1, 3, 4);

        helper.getHighlightedResources().addAll(resources);
        helper.getContainedResources().addAll(resources);

        assertThat(getFirstVisualItem().getResources(Subset.HIGHLIGHTED),
                containsExactly(resources));
    }

    @Test
    public void visualItemHighlightingSetDoesNotContainResourcesThatAreRemovedFromVisualizationHighlightingSet() {
        ResourceSet resources = createResources(1);

        helper.getContainedResources().addAll(resources);
        helper.getHighlightedResources().addAll(resources);
        helper.getHighlightedResources().removeAll(resources);

        assertThat(getFirstVisualItem().getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources()));
    }

    @Test
    public void visualItemHighlightingSetOnlyContainsVisualizationContentResources() {
        ResourceSet contentResources = createResources(2);
        ResourceSet highlightedResources2 = createResources(1, 2);

        helper.getContainedResources().addAll(contentResources);
        helper.getHighlightedResources().addAll(highlightedResources2);

        assertThat(getFirstVisualItem().getResources(Subset.HIGHLIGHTED),
                containsExactly(contentResources));
    }

    /**
     * @see <a
     *      href="http://code.google.com/p/choosel/issues/detail?id=149">"Issue 149"</a>
     */
    @Test
    public void visualItemIsNotHighlightedAfterHighlightedResourcesGotRemoved() {
        ResourceSet content = createLabeledResources(TYPE_1, 1, 2);
        ResourceSet highlightedResources = createResources(TYPE_1, 2);

        helper.getHighlightedResources().addAll(highlightedResources);
        helper.getContainedResources().addAll(content);
        helper.getHighlightedResources().removeAll(highlightedResources);

        VisualItem item = getFirstVisualItem();

        assertEquals(Status.NONE, item.getStatus(Subset.HIGHLIGHTED));
        assertEquals(true, item.getResources(Subset.HIGHLIGHTED).isEmpty());
    }

    /**
     * @see <a
     *      href="http://code.google.com/p/choosel/issues/detail?id=149">"Issue 149"</a>
     */
    @Test
    public void visualItemIsNotSelectedAfterSelectedResourcesGotRemoved() {
        ResourceSet content = createLabeledResources(TYPE_1, 1, 2);
        ResourceSet selectedResources = createResources(TYPE_1, 2);

        helper.getSelectedResources().addAll(selectedResources);
        helper.getContainedResources().addAll(content);
        helper.getSelectedResources().removeAll(selectedResources);

        VisualItem item = getFirstVisualItem();

        assertEquals(Status.NONE, item.getStatus(Subset.SELECTED));
        assertEquals(true, item.getResources(Subset.SELECTED).isEmpty());
    }

    /**
     * @see <a
     *      href="http://code.google.com/p/choosel/issues/detail?id=149">"Issue 149"</a>
     */
    @Test
    public void visualItemIsSelectedAndHighlightedOnChangeWhenAddedResourcesAreAlreadySelectedAndHighlighted() {
        ResourceSet originalResources = createResources(TYPE_1, 1);
        ResourceSet addedResources = createResources(TYPE_1, 2);

        helper.getContainedResources().addAll(originalResources);
        helper.getSelectedResources().addAll(addedResources);
        helper.getHighlightedResources().addAll(addedResources);
        helper.getContainedResources().addAll(addedResources);

        VisualItem item = getFirstVisualItem();

        assertEquals(Status.PARTIAL, item.getStatus(Subset.SELECTED));
        assertThat(item.getResources(Subset.SELECTED),
                containsExactly(addedResources));

        assertEquals(Status.PARTIAL, item.getStatus(Subset.HIGHLIGHTED));
        assertThat(item.getResources(Subset.HIGHLIGHTED),
                containsExactly(addedResources));
    }

    /**
     * @see <a
     *      href="http://code.google.com/p/choosel/issues/detail?id=149">"Issue 149"</a>
     */
    @Test
    public void visualItemIsSelectedOnChangeWhenAddedResourcesAreAlreadySelected() {
        ResourceSet originalResources = createResources(TYPE_1, 1);
        ResourceSet addedResources = createResources(TYPE_1, 2);

        helper.getContainedResources().addAll(originalResources);
        helper.getSelectedResources().addAll(addedResources);
        helper.getContainedResources().addAll(addedResources);

        VisualItem item = getFirstVisualItem();

        assertEquals(Status.PARTIAL, item.getStatus(Subset.SELECTED));
        assertThat(item.getResources(Subset.SELECTED),
                containsExactly(addedResources));
    }

    /**
     * @see <a
     *      href="http://code.google.com/p/choosel/issues/detail?id=123">"Issue 123"</a>
     */
    @Test
    public void visualItemIsSelectedOnCreateWhenResourcesAreAlreadySelected() {
        ResourceSet resources = ResourceSetTestUtils.createResources(1);

        helper.getSelectedResources().addAll(resources);
        helper.getContainedResources().addAll(resources);

        VisualItem item = getFirstVisualItem();

        assertEquals(Status.FULL, item.getStatus(Subset.SELECTED));
        assertThat(item.getResources(Subset.SELECTED),
                containsExactly(resources));
    }

    @Test
    public void visualItemsAreCreatedWhenResourcesAreAdded() {
        ResourceSet resources1 = createResources(TYPE_1, 1);
        ResourceSet resources2 = createResources(TYPE_2, 2);

        helper.addToContainedResources(resources1);
        helper.addToContainedResources(resources2);

        assertThat(underTest.getFullVisualItemContainer().getVisualItems(),
                containsVisualItemsForExactResourceSets(resources1, resources2));
    }

    @Test
    public void visualItemSelectionSetContainsResourcesThatAreAddedToVisualizationSelectionSet() {
        helper.getContainedResources().add(createResource(1));

        helper.getSelectedResources().add(createResource(1));

        assertThat(getFirstVisualItem().getResources(Subset.SELECTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void visualItemSelectionSetDoesNotContainResourcesThatAreRemovedFromVisualizationSelectionSet() {
        ResourceSet resources = ResourceSetTestUtils.createResources(1);

        helper.getContainedResources().addAll(resources);

        helper.getSelectedResources().addAll(resources);
        helper.getSelectedResources().removeAll(resources);

        assertThat(getFirstVisualItem().getResources(Subset.SELECTED),
                containsExactly(createResources()));
    }

}
