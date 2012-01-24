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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResources;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Status;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteractionHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;

public class DefaultVisualItemTest {

    private static final String VIEW_ITEM_ID = "visualItemCategory";

    private ResourceSet resources;

    private DefaultVisualItem underTest;

    private Slot numberSlot;

    @Mock
    private VisualItemValueResolverContext resolverContext;

    @Mock
    private VisualItemValueResolver resolver;

    @Test
    public void getHighlightedResourcesAfterAddHighlightingNoContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(5, 6),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources()));
    }

    @Test
    public void getHighlightedResourcesAfterAddHighlightingOneContainedContainedResourceOutOfTwoResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1, 5),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void getHighlightedResourcesAfterAddHighlightingOnePlusOneContainedContainedResourceOutOfTwoResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(2, 5),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources(1, 2)));
    }

    @Test
    public void getHighlightedResourcesAfterAddHighlightingOnePlusTwoContainedResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(2, 3),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources(1, 2, 3)));
    }

    @Test
    public void getHighlightedResourcesAfterAddHighlightingOnePlusZeroContainedResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(5, 6),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void getHighlightedResourcesAfterAddHighlightingTwoContainedResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources(1, 2)));
    }

    @Test
    public void getHighlightedResourcesAfterHighlightedSubsetIsRemoved() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        resources.removeAll(createResources(1, 2));
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources()));
    }

    @Test
    public void getHighlightedResourcesAfterRemoveHighlightingNoContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(5, 6));
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources()));
    }

    @Test
    public void getHighlightedResourcesAfterRemoveHighlightingOneFromOneContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1, 6));
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources()));
    }

    @Test
    public void getHighlightedResourcesAfterRemoveHighlightingOneFromTwoContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(2, 5));
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void getHighlightedResourcesAfterRemoveHighlightingTwoFromTwoContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1, 2));
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources()));
    }

    @Test
    public void getHighlightedResourcesAfterRemoveHighlightingZeroFromOneContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(5, 6));
        assertThat(underTest.getResources(Subset.HIGHLIGHTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void getSelectedResourcesAfterAddSelectingNoContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(5, 6),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources()));
    }

    @Test
    public void getSelectedResourcesAfterAddSelectingOneContainedContainedResourceOutOfTwoResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1, 5),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void getSelectedResourcesAfterAddSelectingOnePlusOneContainedContainedResourceOutOfTwoResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED, createResources(2, 5),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources(1, 2)));
    }

    @Test
    public void getSelectedResourcesAfterAddSelectingOnePlusTwoContainedResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED, createResources(2, 3),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources(1, 2, 3)));
    }

    @Test
    public void getSelectedResourcesAfterAddSelectingOnePlusZeroContainedResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED, createResources(5, 6),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void getSelectedResourcesAfterAddSelectingTwoContainedResources() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources(1, 2)));
    }

    @Test
    public void getSelectedResourcesAfterRemoveSelectingNoContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(5, 6));
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources()));
    }

    @Test
    public void getSelectedResourcesAfterRemoveSelectingOneFromOneContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1, 6));
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources()));
    }

    @Test
    public void getSelectedResourcesAfterRemoveSelectingOneFromTwoContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(2, 5));
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void getSelectedResourcesAfterRemoveSelectingTwoFromTwoContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1, 2));
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources()));
    }

    @Test
    public void getSelectedResourcesAfterRemoveSelectingZeroFromOneContainedResource() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(5, 6));
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources(1)));
    }

    @Test
    public void getSelectedResourcesAfterSelectedSubsetIsRemoved() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        resources.removeAll(createResources(1, 2));
        assertThat(underTest.getResources(Subset.SELECTED),
                containsExactly(createResources()));
    }

    @Test
    public void getSlotValue() {
        resources.addAll(createResources(1, 2, 3, 4));

        when(resolver.resolve(underTest, resolverContext)).thenReturn(2d);

        Object result = underTest.getValue(numberSlot);
        assertEquals(2d, result);
    }

    @Test
    public void getSlotValueClearCacheForAllResourcesOnResourceSetChange() {
        resources.addAll(createResources(1, 2, 3, 4));

        when(resolver.resolve(underTest, resolverContext)).thenReturn(2d, 3d);

        underTest.getValue(numberSlot);
        resources.removeAll(createResources(1));
        Object result = underTest.getValue(numberSlot);
        assertEquals(3d, result);
    }

    @Test
    public void getSlotValueClearCacheForAllResourcesOnSlotChange() {
        resources.addAll(createResources(1, 2, 3, 4));

        when(resolver.resolve(underTest, resolverContext)).thenReturn(2d, 3d);

        underTest.getValue(numberSlot); // cache value
        underTest.clearValueCache(numberSlot);

        assertEquals(3d, underTest.getValue(numberSlot));
    }

    @Test
    public void highlightStatusAfterHighlightedSubsetIsRemoved() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        resources.removeAll(createResources(1, 2));
        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void highlightStatusCompleteWhenTwoOutOfTwoResourcesHighlighted() {
        resources.addAll(createResources(1, 2));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());

        assertEquals(Status.FULL, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void highlightStatusNoneWhenZeroOutOfTwoResourcesHighlighted() {
        resources.addAll(createResources(1, 2));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(),
                LightweightCollections.<Resource> emptyCollection());

        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void highlightStatusNoneWhenZeroOutOfZeroResourcesHighlighted() {
        resources.addAll(createResources());
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(),
                LightweightCollections.<Resource> emptyCollection());

        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void highlightStatusPartialWhenOneOutOfTwoResourcesHighlighted() {
        resources.addAll(createResources(1, 2));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());

        assertEquals(Status.PARTIAL, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void isHighlightedIsFalseAfterInit() {
        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void isSelectedIsFalseAfterInit() {
        // assertEquals(false, underTest.isSelected());
        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
    }

    @Test
    public void selectionStatusAfterSelectedSubsetIsRemoved() {
        resources.addAll(createResources(1, 2, 3, 4));
        underTest.updateSubset(Subset.SELECTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        resources.removeAll(createResources(1, 2));
        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
    }

    @Test
    public void selectStatusCompleteWhenTwoOutOfTwoResourcesSelected() {
        resources.addAll(createResources(1, 2));
        underTest.updateSubset(Subset.SELECTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());

        assertEquals(Status.FULL, underTest.getStatus(Subset.SELECTED));
    }

    @Test
    public void selectStatusNoneWhenZeroOutOfTwoResourcesSelected() {
        resources.addAll(createResources(1, 2));
        underTest.updateSubset(Subset.SELECTED, createResources(),
                LightweightCollections.<Resource> emptyCollection());

        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
    }

    @Test
    public void selectStatusNoneWhenZeroOutOfZeroResourcesSelected() {
        resources.addAll(createResources());
        underTest.updateSubset(Subset.SELECTED, createResources(),
                LightweightCollections.<Resource> emptyCollection());

        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
    }

    @Test
    public void selectStatusPartialWhenOneOutOfTwoResourcesSelected() {
        resources.addAll(createResources(1, 2));
        underTest.updateSubset(Subset.SELECTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());

        assertEquals(Status.PARTIAL, underTest.getStatus(Subset.SELECTED));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        numberSlot = new Slot("id-2", "number-slot", DataType.NUMBER);
        resources = new DefaultResourceSet();
        underTest = spy(new DefaultVisualItem(VIEW_ITEM_ID, resources,
                resolverContext, mock(VisualItemInteractionHandler.class)));

        when(resolverContext.getResolver(numberSlot)).thenReturn(resolver);
    }

    @Test
    public void statusIsDefault() {
        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusIsHighlighted() {
        resources.addAll(createResources(1));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.FULL, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusIsHighlightedSelected() {
        resources.addAll(createResources(1));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        assertEquals(Status.FULL, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.FULL, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusIsNotHighlightedAfterRemovingHighlightedResources() {
        resources.addAll(createResources(1));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1));
        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusIsNotHighlightedOnEmptyAdd() {
        resources.addAll(createResources(1));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(2),
                LightweightCollections.<Resource> emptyCollection());
        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusIsNotSelectedAfterRemovingSelectedResources() {
        resources.addAll(createResources(1));
        underTest.updateSubset(Subset.SELECTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1));
        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusIsNotSelectedOnEmptyAdd() {
        resources.addAll(createResources(1));
        underTest.updateSubset(Subset.SELECTED, createResources(2),
                LightweightCollections.<Resource> emptyCollection());
        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusIsSelected() {
        resources.addAll(createResources(1));
        underTest.updateSubset(Subset.SELECTED, createResources(1),
                LightweightCollections.<Resource> emptyCollection());
        assertEquals(Status.FULL, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusPartiallyHighlightedAfterOneResourceIsRemovedFromHighlight() {
        resources.addAll(createResources(1, 2));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1));
        assertEquals(Status.NONE, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.PARTIAL, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusPartiallyHighlightedSelectedAfterOneResourceIsRemovedFromHighlightAndSelection() {
        resources.addAll(createResources(1, 2));
        underTest.updateSubset(Subset.HIGHLIGHTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.HIGHLIGHTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1));
        underTest.updateSubset(Subset.SELECTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1));
        assertEquals(Status.PARTIAL, underTest.getStatus(Subset.SELECTED));
        assertEquals(Status.PARTIAL, underTest.getStatus(Subset.HIGHLIGHTED));
    }

    @Test
    public void statusPartiallySelectedAfterOneResourceIsRemovedFromSelect() {
        resources.addAll(createResources(1, 2));
        underTest.updateSubset(Subset.SELECTED, createResources(1, 2),
                LightweightCollections.<Resource> emptyCollection());
        underTest.updateSubset(Subset.SELECTED,
                LightweightCollections.<Resource> emptyCollection(),
                createResources(1));
        assertEquals(Status.NONE, underTest.getStatus(Subset.HIGHLIGHTED));
        assertEquals(Status.PARTIAL, underTest.getStatus(Subset.SELECTED));
    }

}
