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
package org.thechiselgroup.biomixer.client.core.visualization.model.implementation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsAnything.any;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TYPE_1;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TYPE_2;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResource;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResources;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.toResourceSet;
import static org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory.createLightweightList;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemTestUtils.containsVisualItemsForExactResourceSets;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockResolver;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockResolverThatCanAlwaysResolve;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockResolverThatCanNeverResolve;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockResolverThatCanResolveExactResourceSet;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemWithResourcesMatcher.containsEqualResources;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;

/**
 * <p>
 * Tests the error free {@link VisualItemContainer} exposed by
 * {@link VisualizationModel}.
 * </p>
 * 
 * @author Lars Grammel
 */
// TODO add tests for handler notification
public class DefaultVisualizationModelErrorFreeVisualItemContainerTest {

    /**
     * Convert input to {@code LightWeightCollection<Delta>}
     */
    private static LightweightList<Delta<VisualItem>> cast(
            List<VisualItemContainerChangeEvent> original) {
        LightweightList<Delta<VisualItem>> result = CollectionFactory
                .createLightweightList();
        for (VisualItemContainerChangeEvent event : original) {
            result.add(event.getDelta());
        }
        return result;
    }

    private Slot slot;

    private DefaultVisualizationModel visualizationModel;

    private DefaultVisualizationModelTestHelper helper;

    private VisualItemContainer underTest;

    private Delta<VisualItem> captureDelta(
            VisualItemContainerChangeEventHandler handler) {
        return captureDeltas(handler, 1).getFirstElement();
    }

    private LightweightList<Delta<VisualItem>> captureDeltas(
            VisualItemContainerChangeEventHandler handler,
            int wantedNumberOfInvocations) {

        ArgumentCaptor<VisualItemContainerChangeEvent> captor = ArgumentCaptor
                .forClass(VisualItemContainerChangeEvent.class);
        verify(handler, times(wantedNumberOfInvocations))
                .onVisualItemContainerChanged(captor.capture());
        return cast(captor.getAllValues());
    }

    @Test
    public void containsVisualItemsReturnsFalseForVisualItemsWithErrors() {
        ResourceSet resources = createResources(TYPE_1, 1);

        visualizationModel.setResolver(slot, mockResolverThatCanNeverResolve());
        helper.addToContainedResources(resources);

        assertThat(
                underTest.containsVisualItem(visualizationModel
                        .getFullVisualItemContainer().getVisualItems()
                        .getFirstElement().getId()), is(false));
    }

    @Test
    public void containsVisualItemsReturnsTrueForVisualItemsWithoutErrors() {
        ResourceSet resources = createResources(TYPE_1, 1);

        visualizationModel
                .setResolver(slot, mockResolverThatCanAlwaysResolve());
        helper.addToContainedResources(resources);

        assertThat(
                underTest.containsVisualItem(visualizationModel
                        .getFullVisualItemContainer().getVisualItems()
                        .getFirstElement().getId()), is(true));
    }

    /**
     * <p>
     * delta=added, current_state=errors ==&gt; ignore
     * </p>
     * <p>
     * 2 view items get added to trigger call, we check that one is ignored
     * </p>
     */
    @Test
    public void eventHandlerAddedVisualItemsWithErrorsGetIgnoredWhenResourcesChange() {
        VisualItemContainerChangeEventHandler handler = registerHandler();

        Resource validResource = createResource(TYPE_1, 1);

        setCanResolveIfContainsResourceExactlyResolver(toResourceSet(validResource));

        helper.addToContainedResources(toResourceSet(validResource,
                createResource(TYPE_2, 1)));

        assertThat(captureDelta(handler).getAddedElements(),
                containsEqualResources(validResource));
    }

    /**
     * delta=removed, old_state=errors ==&gt; ignore
     */
    @Test
    public void eventHandlerRemovedVisualItemsWithErrorsGetIgnoredWhenResourcesChange() {
        VisualItemContainerChangeEventHandler handler = registerHandler();

        Resource validResource = createResource(TYPE_1, 1);
        Resource errorResource = createResource(ResourceSetTestUtils.TYPE_2, 1);

        setCanResolveIfContainsResourceExactlyResolver(ResourceSetTestUtils
                .toResourceSet(validResource));

        helper.addToContainedResources(ResourceSetTestUtils.toResourceSet(
                validResource, errorResource));

        /*
         * at this point, the view item with errorResource is invalid as per
         * invalidVisualItemDoesNotGetAddedToAddedDelta test
         */

        helper.getContainedResources().removeAll(
                ResourceSetTestUtils
                        .toResourceSet(validResource, errorResource));

        assertThat(captureDeltas(handler, 2).get(1).getRemovedElements(),
                containsEqualResources(validResource));
    }

    /**
     * delta=updated, old_state=errors, current_state=valid ==&gt; add
     */
    @Test
    public void eventHandlerUpdatedVisualItemsChangingFromErrorsToValidGetAddedWhenResourcesChange() {
        VisualItemContainerChangeEventHandler handler = registerHandler();

        Resource resource1 = createResource(TYPE_1, 1);
        Resource resource2 = createResource(TYPE_1, 2);

        setCanResolveIfContainsResourceExactlyResolver(ResourceSetTestUtils
                .toResourceSet(resource1, resource2));

        helper.addToContainedResources(resource1);

        /* should not have been added yet - 0 update calls so far */

        helper.addToContainedResources(resource2);

        assertThat(captureDelta(handler).getAddedElements(),
                containsEqualResources(resource1, resource2));
    }

    /**
     * delta=updated, old_state=valid, current_state=errors ==&gt; remove
     */
    @Test
    public void eventHandlerUpdatedVisualItemsChangingFromValidToErrorsGetRemovedWhenResourcesChange() {
        VisualItemContainerChangeEventHandler handler = registerHandler();

        Resource resource1 = createResource(TYPE_1, 1);
        Resource resource2 = createResource(TYPE_1, 2);

        setCanResolveIfContainsResourceExactlyResolver(toResourceSet(resource1,
                resource2));

        helper.addToContainedResources(toResourceSet(resource1, resource2));

        /* should now have 1 valid view item */

        helper.getContainedResources().remove(resource2);

        assertThat(captureDeltas(handler, 2).get(1).getRemovedElements(),
                containsEqualResources(resource1));
    }

    /**
     * delta=updated, old_state=valid, current_state=valid ==&gt; updated
     */
    @Test
    public void eventHandlerUpdatedVisualItemsThatAreValidNowAndBeforeGetUpdatedWhenResourcesChange() {
        VisualItemContainerChangeEventHandler handler = registerHandler();

        Resource resource1 = createResource(TYPE_1, 1);
        Resource resource2 = createResource(TYPE_1, 2);

        visualizationModel
                .setResolver(slot, mockResolverThatCanAlwaysResolve());

        helper.addToContainedResources(resource1);

        // update call
        helper.addToContainedResources(resource2);

        assertThat(captureDeltas(handler, 2).get(1).getUpdatedElements(),
                containsEqualResources(resource1, resource2));
    }

    /**
     * delta=updated, old_state=errors, current_state=errors ==&gt; ignore
     */
    @Test
    public void eventHandlerUpdatedVisualItemsWithErrorsNowAndBeforeGetIgnoredWhenResourcesChange() {
        VisualItemContainerChangeEventHandler handler = registerHandler();

        Resource validResource = createResource(TYPE_1, 3);

        setCanResolveIfContainsResourceExactlyResolver(ResourceSetTestUtils
                .toResourceSet(validResource));

        // adds error view item and correct view item
        helper.addToContainedResources(createResource(TYPE_1, 1));

        // updates error view item
        helper.addToContainedResources(createResource(TYPE_1, 2));

        // neither adding nor updating view item should have triggered calls to
        // update
        verify(handler, never()).onVisualItemContainerChanged(
                argThat(any(VisualItemContainerChangeEvent.class)));
    }

    @Test
    public void eventHandlerVisualItemsThatBecomeInvalidAfterSlotResolverChangeGetRemoved() {
        visualizationModel
                .setResolver(slot, mockResolverThatCanAlwaysResolve());
        helper.addToContainedResources(createResource(1));

        VisualItemContainerChangeEventHandler handler = registerHandler();
        visualizationModel.setResolver(slot, mockResolverThatCanNeverResolve());

        assertThat(captureDelta(handler).getRemovedElements(),
                containsVisualItemsForExactResourceSets(createResources(1)));
    }

    @Test
    public void getByIDReturnsCorrectVisualItem() {
        ResourceSet resources = createResources(TYPE_1, 1);

        visualizationModel
                .setResolver(slot, mockResolverThatCanAlwaysResolve());
        helper.addToContainedResources(resources);

        LightweightList<VisualItem> visualItems = createLightweightList();

        // get view items that are in the content display
        visualItems.add(underTest.getVisualItem(visualizationModel
                .getFullVisualItemContainer().getVisualItems()
                .getFirstElement().getId()));

        assertThat(visualItems,
                containsVisualItemsForExactResourceSets(resources));
    }

    @Test
    public void getByResourcesIsEmptyForResourceInvalid() {
        ResourceSet resources = createResources(TYPE_1, 1);

        visualizationModel.setResolver(slot, mockResolverThatCanNeverResolve());

        helper.addToContainedResources(resources);

        assertTrue(underTest.getVisualItems(resources).isEmpty());
    }

    @Test
    public void getByResourcesOnlyReturnsValidVisualItems() {
        ResourceSet resources1 = createResources(TYPE_1, 1);
        ResourceSet resources2 = createResources(TYPE_2, 2);

        visualizationModel.setResolver(slot,
                mockResolverThatCanResolveExactResourceSet(resources1));

        ResourceSet allResources = createResources(TYPE_1);
        allResources.addAll(resources1);
        allResources.addAll(resources2);

        helper.addToContainedResources(allResources);

        assertThat(underTest.getVisualItems(allResources),
                containsVisualItemsForExactResourceSets(resources1));
    }

    @Test
    public void getByResourcesReturnsValidVisualItemForOneResource() {
        ResourceSet resources = createResources(TYPE_1, 1);

        visualizationModel
                .setResolver(slot, mockResolverThatCanAlwaysResolve());

        helper.addToContainedResources(resources);

        assertThat(underTest.getVisualItems(resources),
                containsVisualItemsForExactResourceSets(resources));
    }

    @Test
    public void getVisualItemsExcludesVisualItemsWithErrors() {
        ResourceSet resources1 = createResources(TYPE_1, 1);
        ResourceSet resources2 = createResources(TYPE_2, 1);

        setCanResolverIfContainsResourceExactlyResolver(resources1);
        helper.addToContainedResources(toResourceSet(resources1, resources2));

        assertThat(underTest.getVisualItems(),
                containsVisualItemsForExactResourceSets(resources1));
    }

    @Test
    public void getVisualItemsReturnsAddedVisualItem() {
        ResourceSet resources = createResources(TYPE_1, 1);

        visualizationModel
                .setResolver(slot, mockResolverThatCanAlwaysResolve());
        helper.addToContainedResources(resources);

        assertThat(underTest.getVisualItems(),
                containsVisualItemsForExactResourceSets(resources));
    }

    private VisualItemContainerChangeEventHandler registerHandler() {
        VisualItemContainerChangeEventHandler handler = mock(VisualItemContainerChangeEventHandler.class);
        underTest.addHandler(handler);
        return handler;
    }

    private void setCanResolveIfContainsResourceExactlyResolver(
            ResourceSet resourceSet) {

        VisualItemValueResolver resolver = mockResolverThatCanResolveExactResourceSet(resourceSet);
        visualizationModel.setResolver(slot, resolver);
    }

    private void setCanResolverIfContainsResourceExactlyResolver(
            ResourceSet resourceSet) {

        VisualItemValueResolver resolver = mockResolverThatCanResolveExactResourceSet(resourceSet);
        visualizationModel.setResolver(slot, resolver);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        helper = new DefaultVisualizationModelTestHelper();
        slot = helper.createSlots(DataType.TEXT)[0];

        visualizationModel = helper.createTestVisualizationModel();
        visualizationModel.setResolver(slot, mockResolver());

        underTest = visualizationModel.getErrorFreeVisualItemContainer();
    }

}