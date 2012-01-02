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
import static org.junit.Assert.assertThat;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TYPE_1;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TYPE_2;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResource;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.toResourceSet;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockDelegatingResolver;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockResolverThatCanAlwaysResolve;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockResolverThatCanResolveExactResourceSet;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemWithResourcesMatcher.containsEqualResources;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemResolutionErrorModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualizationModel;

/**
 * Tests the error model {@link VisualItemResolutionErrorModel} of the
 * {@link VisualizationModel} subsystem implemented using
 * {@link DefaultVisualizationModel}.
 * 
 * @author Lars Grammel
 */
public class DefaultVisualizationModelErrorTest {

    private DefaultVisualizationModelTestHelper helper;

    private Resource resource1;

    private Resource resource2;

    private DefaultVisualizationModel underTest;

    private void addResourcesToUndertest() {
        getResourceSetFromUnderTest().add(resource1);
        getResourceSetFromUnderTest().add(resource2);
    }

    @Test
    public void allSlotsHaveResolversCausesNoErrors() {
        Slot[] slots = helper.createSlots(DataType.TEXT);
        underTest = helper.createTestVisualizationModel();

        underTest.setResolver(slots[0], mockResolverThatCanAlwaysResolve());

        getResourceSetFromUnderTest().add(
                ResourceSetTestUtils.createResource(1));

        assertThat(underTest.hasErrors(), is(false));
    }

    @Test
    public void delegatingResolverHasErrorWhenDelegateUnconfigured() {
        Slot[] slots = helper.createSlots(DataType.NUMBER, DataType.NUMBER);
        underTest = helper.createTestVisualizationModel();

        VisualItemValueResolver delegatingResolver = mockDelegatingResolver(slots[1]);

        underTest.setResolver(slots[0], delegatingResolver);

        underTest.getContentResourceSet().add(createResource(1));

        assertThat(underTest.getSlotsWithErrors(), containsExactly(slots));
    }

    @Test
    public void delegatingResolverLosesErrorWhenDelegateConfigured() {
        Slot[] slots = helper.createSlots(DataType.NUMBER, DataType.NUMBER);
        underTest = helper.createTestVisualizationModel();

        VisualItemValueResolver delegatingResolver = mockDelegatingResolver(slots[1]);

        underTest.setResolver(slots[0], delegatingResolver);
        underTest.getContentResourceSet().add(createResource(1));

        underTest.setResolver(slots[1], mockResolverThatCanAlwaysResolve());

        assertThat(underTest.hasErrors(), is(false));
    }

    private ResourceSet getResourceSetFromUnderTest() {
        return underTest.getContentResourceSet();
    }

    @Test
    public void resolverCannotResolveSomeVisualItemsFixedByChangingResolverReturnsNoErrors() {
        Slot[] slots = helper.createSlots(DataType.TEXT);
        underTest = helper.createTestVisualizationModel();

        setResolver(slots, resource1);
        addResourcesToUndertest();

        /*
         * there are currently errors on resource2 as per
         * resolverCannotResolveSomeVisualItemsThrowsErrors test
         */

        underTest.setResolver(slots[0], mockResolverThatCanAlwaysResolve());
        assertThat(underTest.hasErrors(), is(false));
    }

    @Test
    public void resolverCannotResolveSomeVisualItemsFixedByChangingVisualItemsReturnsNoErrors() {
        Slot[] slots = helper.createSlots(DataType.TEXT);
        underTest = helper.createTestVisualizationModel();

        setResolver(slots, resource1);
        addResourcesToUndertest();

        /*
         * there are currently errors on resource2 as per
         * resolverCannotResolveSomeVisualItemsThrowsErrors test
         */
        getResourceSetFromUnderTest().remove(resource2);
        assertThat(underTest.hasErrors(), is(false));
    }

    @Test
    public void resolverCannotResolveSomeVisualItemsThrowsErrors() {
        Slot[] slots = helper.createSlots(DataType.TEXT);
        underTest = helper.createTestVisualizationModel();

        setResolver(slots, resource1);

        addResourcesToUndertest();

        assertThat(underTest.hasErrors(), is(true));
        assertThat(underTest.getSlotsWithErrors(), containsExactly(slots[0]));
        assertThat(underTest.getVisualItemsWithErrors(),
                containsEqualResources(resource2));
    }

    private void setResolver(Slot[] slots, Resource... resources) {
        VisualItemValueResolver resolver = mockResolverThatCanResolveExactResourceSet(toResourceSet(resources));
        underTest.setResolver(slots[0], resolver);
    }

    @Test
    public void setResolversFromUnconfiguredToValidReturnsNoErrors() {
        Slot[] slots = helper.createSlots(DataType.TEXT);
        underTest = helper.createTestVisualizationModel();

        getResourceSetFromUnderTest().add(createResource(1));

        /*
         * Model currently in a error state as per
         * slotWithoutResolverCausesError test
         */
        VisualItemValueResolver resolver = mockResolverThatCanAlwaysResolve();

        underTest.setResolver(slots[0], resolver);
        assertThat(underTest.hasErrors(), is(false));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        helper = new DefaultVisualizationModelTestHelper();

        resource1 = createResource(TYPE_1, 1);
        resource2 = createResource(TYPE_2, 2);
    }

    @Test
    public void slotWithoutResolverCausesError() {
        Slot[] slots = helper.createSlots(DataType.TEXT);
        underTest = helper.createTestVisualizationModel();

        getResourceSetFromUnderTest().add(
                ResourceSetTestUtils.createResource(1));

        assertThat(underTest.hasErrors(), is(true));
        assertThat(underTest.getSlotsWithErrors(), containsExactly(slots[0]));
    }

    @Test
    public void unconfiguredSlotsHaveErrors() {
        Slot[] slots = helper.createSlots(DataType.TEXT, DataType.NUMBER);

        underTest = helper.createTestVisualizationModel();

        getResourceSetFromUnderTest().add(createResource(1));

        underTest.setResolver(slots[0], mockResolverThatCanAlwaysResolve());

        assertThat(underTest.getUnconfiguredSlots(), containsExactly(slots[1]));
        assertThat(underTest.hasErrors(), is(true));
        assertThat(underTest.getSlotsWithErrors(), containsExactly(slots[1]));
    }

}