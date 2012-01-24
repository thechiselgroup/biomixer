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
package org.thechiselgroup.biomixer.client.core.visualization.model.managed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResource;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockResolverThatCanAlwaysResolve;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.model.initialization.TestSlotMappingInitializer;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceByUriMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualizationModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualizationModelTestHelper;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.FirstResourcePropertyResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.FixedValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.SubsetDelegatingValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.FirstResourcePropertyResolverFactory;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.FixedVisualItemResolverFactory;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.ManagedVisualItemValueResolverDecorator;

// TODO think about test name schema - unit, module, system, integration test etc
public class SlotMappingIntegrationTest {

    private SlotMappingInitializer slotMappingInitializer;

    private DefaultVisualItemResolverFactoryProvider resolverProvider = new DefaultVisualItemResolverFactoryProvider();

    @Mock
    private VisualItemBehavior visualItemBehavior;

    @Mock
    private ErrorHandler errorHandler;

    private static final String resolverId1 = "resolver-id-1";

    private static final String property1 = "property1";

    private static final String resolverId2 = "resolver-id-2";

    private static final String property2 = "property2";

    private final String property3 = "property3";

    private DefaultVisualizationModelTestHelper helper;

    /**
     * <h3>Changing Property Select</h3>
     * 
     * Resolvers: 1 resolver, able to resolve a Number based on PropertyName<br>
     * Slots: 1 Number slot <br>
     * Grouping: each resource is grouped alone <br>
     * Data: [(x:1, y:2)]<br>
     * 
     * Expected Output: [1] => [2]<br>
     * 
     * <p>
     * We create the view with the resolver and slots as above. The resolver is
     * automatically set to resolve based on property name “x”, and so 2
     * VisualItems are created each with value = 1. We then change the property
     * that is selected to property “y”. The VisualItems are replaced with 2 new
     * VisualItems each with value = 2.
     * </p>
     */
    @Test
    public void changeSelectedPropertyChangesVisualItems() {
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        VisualItemValueResolver resolver = new ManagedVisualItemValueResolverDecorator(
                resolverId1, new FirstResourcePropertyResolver(property1,
                        DataType.NUMBER));
        initialSlotMapping.put(requiredSlots[0], resolver);

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        Resource resource = createResource(1);
        resource.putValue(property1, 1);
        resource.putValue(property2, 2);
        model.getContentResourceSet().add(resource);

        VisualItemValueResolver resolver2 = new ManagedVisualItemValueResolverDecorator(
                resolverId1, new FirstResourcePropertyResolver(property2,
                        DataType.NUMBER));
        model.setResolver(requiredSlots[0], resolver2);

        LightweightCollection<VisualItem> visualItems = model
                .getFullVisualItemContainer().getVisualItems();

        assertEquals(visualItems.size(), 1);
        Iterator<VisualItem> iterator = visualItems.iterator();
        VisualItem first = iterator.next();
        assertEquals(first.getValue(requiredSlots[0]), 2);
    }

    @Test
    public void changingResolverManuallyChangesResolution() {
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId2, DataType.NUMBER));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        initialSlotMapping.put(requiredSlots[0],
                new ManagedVisualItemValueResolverDecorator(resolverId1,
                        new FirstResourcePropertyResolver(property1,
                                DataType.NUMBER)));

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        model.setContentResourceSet(new DefaultResourceSet());

        Resource resource1 = ResourceSetTestUtils.createResource(1);
        resource1.putValue(property1, 1);
        resource1.putValue(property2, 2);

        model.getContentResourceSet().add(resource1);

        /* Should have 1 View Item with Value 1 */
        model.setResolver(requiredSlots[0],
                new ManagedVisualItemValueResolverDecorator(resolverId2,
                        new FirstResourcePropertyResolver(property2,
                                DataType.NUMBER)));

        /* Should have 1 View Item with Value 2 */
        LightweightCollection<VisualItem> visualItems = model
                .getFullVisualItemContainer().getVisualItems();
        assertTrue(visualItems.size() == 1);
        VisualItem item = visualItems.iterator().next();
        assertEquals(2, item.getValue(requiredSlots[0]));
    }

    private DefaultVisualizationModel createViewModel(
            ResourceMultiCategorizer categorizer) {

        DefaultVisualizationModel model = new DefaultVisualizationModel(
                helper.getViewContentDisplay(), new DefaultResourceSet(),
                new DefaultResourceSet(), visualItemBehavior, errorHandler,
                new DefaultResourceSetFactory(), categorizer);
        new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, model, model);
        return model;
    }

    @Test
    public void delegatingResolverHasErrorWhenDelegateUnconfigured() {
        Slot[] slots = helper.createSlots(DataType.NUMBER, DataType.NUMBER);
        DefaultVisualizationModel underTest = helper
                .createTestVisualizationModel();

        SubsetDelegatingValueResolver delegatingResolver = new SubsetDelegatingValueResolver(
                slots[1], Subset.HIGHLIGHTED);

        underTest.setResolver(slots[0], delegatingResolver);

        underTest.getContentResourceSet().add(
                ResourceSetTestUtils.createResource(1));

        assertThat(underTest.getSlotsWithErrors(), containsExactly(slots));
    }

    @Test
    public void delegatingResolverLosesErrorWhenDelegateConfigured() {
        Slot[] slots = helper.createSlots(DataType.NUMBER, DataType.NUMBER);
        DefaultVisualizationModel underTest = helper
                .createTestVisualizationModel();

        SubsetDelegatingValueResolver delegatingResolver = new SubsetDelegatingValueResolver(
                slots[1], Subset.HIGHLIGHTED);

        underTest.setResolver(slots[0], delegatingResolver);

        underTest.getContentResourceSet().add(
                ResourceSetTestUtils.createResource(1));

        underTest.setResolver(slots[1], mockResolverThatCanAlwaysResolve());

        assertThat(underTest.getSlotsWithErrors(),
                containsExactly(CollectionFactory
                        .<Slot> createLightweightList()));
    }

    /**
     * <h3>Scenario 6: Failed Resolution on Multiple Slots</h3>
     * 
     * Resolvers: 1 resolver, able to resolve Number on all resources. 1
     * resolver not able to resolve Text. <br>
     * Slots: 1 Text slot, 1 Number slot <br>
     * Grouping: each resource is grouped alone <br>
     * Data: [x,x]<br>
     * 
     * Expected Output: error thrown<br>
     * 
     * <p>
     * We create the view with the resolver and slots as above. We then add the
     * data, and because there are no applicable resolvers for the Text slot, an
     * error should be thrown to show that the data cannot be shown.
     * </p>
     */
    @Test
    public void errorStateSetWhenResolversCannotResolveOneOfTwoSlots()
            throws Throwable {
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER,
                DataType.TEXT);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId2, DataType.TEXT));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        initialSlotMapping.put(requiredSlots[0],
                new ManagedVisualItemValueResolverDecorator(resolverId1,
                        new FirstResourcePropertyResolver(property1,
                                DataType.NUMBER)));
        initialSlotMapping.put(requiredSlots[1],
                new ManagedVisualItemValueResolverDecorator(resolverId2,
                        new FirstResourcePropertyResolver(property3,
                                DataType.TEXT)));

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        Resource resource = createResource(1);
        resource.putValue(property1, 1);
        // unresolvable by either resolvers
        resource.putValue(property2, "a");
        model.getContentResourceSet().add(resource);

        assertTrue(model.hasErrors());
        assertThat(model.getSlotsWithErrors(),
                containsExactly(requiredSlots[1]));
    }

    /**
     * <h3>Scenario 5: Failed Resolution of Resources on 1 Slot</h3>
     * 
     * Resolvers: 1 resolver, not applicable to anything <br>
     * Slots: 1 Text slot<br>
     * Grouping: each resource is grouped alone<br>
     * Data: [1]<br>
     * 
     * Expected Output: error thrown<br>
     * 
     * <p>
     * We create the view with the resolver and slots as above. We then add the
     * data, and because there are no applicable resolvers, an error should be
     * thrown to show that the data cannot be shown.
     * </p>
     */
    public void errorStateSetWhenResolvingOneUnresolvableResource()
            throws Throwable {

        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        initialSlotMapping.put(requiredSlots[0],
                new ManagedVisualItemValueResolverDecorator(resolverId1,
                        new FirstResourcePropertyResolver(property1,
                                DataType.NUMBER)));

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        model.getContentResourceSet().add(createResource(1));

        assertTrue(model.hasErrors());
        assertThat(model.getSlotsWithErrors(),
                containsExactly(requiredSlots[0]));
    }

    /**
     * <h3>Automatically Change Resolver When Selected Property Not Applicable</h3>
     * 
     * Resolvers: 1 resolver, which resolves a Number based on a property, 1
     * default resolver that resolves to 2<br>
     * Slots: 1 Number slot <br>
     * Grouping: each resource is grouped alone <br>
     * Data: [(x:1, y:2),(y:2)]<br>
     * 
     * Expected Output: [1] => [2,2]<br>
     * 
     * <p>
     * We create the view with the single slot and set up the resolver to select
     * on property x. We then add the first resource, and the View will create 1
     * VisualItem with value 1. We then add the second resource, and the
     * resolver will automatically change to resolve on property y, and the View
     * will have 2 VisualItems in it each with value 2.
     * </p>
     */
    /*
     * XXX The property of the resolver gets switched in this test case. Since
     * resolvers are immutable, this needs to change - it should be impossible
     * to switch to an invalid property. Instead, we should test that the UI
     * does not provide the possibility to switch to an invalid resolver.
     */
    // TODO fix issue 156
    @Ignore("issue 156 need to fix")
    @Test
    public void reinitialzeResolverWhenPropertySelectedIsNotValid() {
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        VisualItemValueResolver resolver = new ManagedVisualItemValueResolverDecorator(
                resolverId1, new FirstResourcePropertyResolver(property1,
                        DataType.NUMBER));
        initialSlotMapping.put(requiredSlots[0],
                new ManagedVisualItemValueResolverDecorator(resolverId1,
                        new FixedValueResolver(1, DataType.NUMBER)));

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        model.setResolver(requiredSlots[0], resolver);

        Resource resource = ResourceSetTestUtils.createResource(1);
        resource.putValue(property1, 2);
        model.getContentResourceSet().add(resource);

        // resolver.setProperty(property2);

        LightweightCollection<VisualItem> visualItems = model
                .getFullVisualItemContainer().getVisualItems();

        assertEquals(visualItems.size(), 1);
        Iterator<VisualItem> iterator = visualItems.iterator();
        VisualItem first = iterator.next();
        assertEquals(1, first.getValue(requiredSlots[0]));
    }

    @Test
    public void removingAllResourceDoesNotChangeResolver() {
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId2, DataType.NUMBER));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        VisualItemValueResolver resolver = new ManagedVisualItemValueResolverDecorator(
                resolverId1, new FirstResourcePropertyResolver(property1,
                        DataType.NUMBER));
        initialSlotMapping.put(requiredSlots[0], resolver);

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        model.setContentResourceSet(new DefaultResourceSet());
        Resource resource1 = ResourceSetTestUtils.createResource(1);
        resource1.putValue(property1, 1);
        resource1.putValue(property2, 2);

        model.getContentResourceSet().add(resource1);
        /* Should have 1 View Item with Value 1 */
        model.setContentResourceSet(new DefaultResourceSet());

        assertEquals(resolver, model.getResolver(requiredSlots[0]));
    }

    @Test
    public void removingAllResourcesResultsInNoVisualItems() {
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId2, DataType.NUMBER));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        VisualItemValueResolver resolver = new ManagedVisualItemValueResolverDecorator(
                resolverId1, new FirstResourcePropertyResolver(property1,
                        DataType.NUMBER));
        initialSlotMapping.put(requiredSlots[0], resolver);

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        Resource resource = createResource(1);
        resource.putValue(property1, 1);
        resource.putValue(property2, 2);

        model.getContentResourceSet().add(resource);

        /* Should have 1 View Item with Value 1 */
        model.setContentResourceSet(new DefaultResourceSet());

        assertEquals(
                model.getFullVisualItemContainer().getVisualItems().size(), 0);
    }

    /**
     * <h3>Scenario 2: Simple Resolution of Number Property (Tested)</h3>
     * 
     * Resolvers: 1 resolver: can only resolve items with a Number field, just
     * takes the slot values<br>
     * Slots: only 1, val1 => Number<br>
     * Grouping: each Resource is grouped by itself <br>
     * Data: 1 Resource each with Number Slot [2]<br>
     * 
     * Expected Outcome: [2]<br>
     * 
     * <p>
     * We create a simple view that has one resolver and one slot. The slot and
     * resolver are as above. We then initialize the View and add the data item.
     * Each of those resources should be converted into a VisualItem, with a
     * slot value = 2.
     * </p>
     */
    @Test
    public void resolverWithNumberPropertyResolver() {
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        initialSlotMapping.put(requiredSlots[0],
                new ManagedVisualItemValueResolverDecorator(resolverId1,
                        new FirstResourcePropertyResolver(property1,
                                DataType.NUMBER)));

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        Resource resource = ResourceSetTestUtils.createResource(1);
        resource.putValue(property1, 1);
        model.getContentResourceSet().add(resource);

        LightweightCollection<VisualItem> visualItems = model
                .getFullVisualItemContainer().getVisualItems();

        assertEquals(visualItems.size(), 1);

        Iterator<VisualItem> iterator = visualItems.iterator();
        VisualItem first = iterator.next();
        assertEquals(first.getValue(requiredSlots[0]), 1);

    }

    /**
     * <h3>Scenario 3: Resolution of 2 Fields of different Type (Tested)</h3>
     * 
     * Resolvers: 2 resolvers, one for a NumberSlot and one for a TextSlot<br>
     * Slots: val1 => Number, val2 => Text <br>
     * Grouping: each Resource grouped on its own <br>
     * Data: 1 resource [ (1,”a”)]<br>
     * 
     * Expected Outcome: [ (1,”a”)]<br>
     * 
     * <p>
     * We create the view with the resolver and slots as above. We then add the
     * 2 resources above. Each of the resources result in a similar VisualItem
     * being created.
     * </p>
     */
    @Test
    public void resolveTwoFieldsWithTwoResolvers() {
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER,
                DataType.TEXT);

        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId2, DataType.TEXT));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        initialSlotMapping.put(requiredSlots[0],
                new ManagedVisualItemValueResolverDecorator(resolverId1,
                        new FirstResourcePropertyResolver(property1,
                                DataType.NUMBER)));
        initialSlotMapping.put(requiredSlots[1],
                new ManagedVisualItemValueResolverDecorator(resolverId2,
                        new FirstResourcePropertyResolver(property2,
                                DataType.TEXT)));

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        Resource resource1 = ResourceSetTestUtils.createResource(1);
        resource1.putValue(property1, 1);
        resource1.putValue(property2, "a");

        model.getContentResourceSet().add(resource1);

        /* Test results */
        LightweightCollection<VisualItem> visualItems = model
                .getFullVisualItemContainer().getVisualItems();
        assertTrue(visualItems.size() == 1);

        Iterator<VisualItem> iterator = visualItems.iterator();
        VisualItem first = iterator.next();
        assertEquals(first.getValue(requiredSlots[0]), 1);
        assertEquals(first.getValue(requiredSlots[1]), "a");

    }

    /**
     * <h3>Scenario 1: Simple Resolution (Tested)</h3>
     * 
     * Resolvers: only one, can resolve anything (Fixed to 1)<br>
     * Slots: only one, val1 => Number<br>
     * Grouping: each Resource is grouped by itself <br>
     * Data: 1 Resource, any data allowed<br>
     * 
     * Expected Outcome: [1]<br>
     * 
     * <p>
     * We create a simple view that has one resolver and one slot. The slot is a
     * Number and the resolver is a FixedResolver, so it can resolve any
     * ResourceSet . We then initialize the View and add the data items. Each of
     * those resources should be converted into a VisualItem, with a slot value
     * of 1.
     * </p>
     */
    @Test
    public void resolveWithOneFixedResolver() {
        /* set up the slots */
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        /* set up the provider to return the correct resolvers */
        resolverProvider.register(new FixedVisualItemResolverFactory(
                resolverId1, DataType.NUMBER, 1));

        /* define and create initializer */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        initialSlotMapping.put(requiredSlots[0],
                new ManagedVisualItemValueResolverDecorator(resolverId1,
                        new FixedValueResolver(1, DataType.NUMBER)));

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        /*
         * create the ViewModel, as well as initialize the
         * slotMappingConfiguration
         */

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        model.getContentResourceSet().add(createResource(1));

        /* Test results */
        LightweightCollection<VisualItem> visualItems = model
                .getFullVisualItemContainer().getVisualItems();
        assertTrue(visualItems.size() == 1);

        VisualItem item = visualItems.iterator().next();
        assertEquals(1, item.getValue(requiredSlots[0]));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        helper = new DefaultVisualizationModelTestHelper();
    }

    @Test
    public void unconfiguredSlotsGetConfugredAutomaticallyWhenResourcesAdded() {
        Slot[] requiredSlots = helper.createSlots(DataType.NUMBER);
        when(helper.getViewContentDisplay().getSlots()).thenReturn(
                requiredSlots);

        resolverProvider.register(new FirstResourcePropertyResolverFactory(
                resolverId1, DataType.NUMBER));

        /* define initialization mapping */
        final Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        VisualItemValueResolver resolver = new ManagedVisualItemValueResolverDecorator(
                resolverId1, new FirstResourcePropertyResolver(property1,
                        DataType.NUMBER));
        initialSlotMapping.put(requiredSlots[0], resolver);

        slotMappingInitializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        DefaultVisualizationModel model = createViewModel(new ResourceByUriMultiCategorizer());

        Resource resource = createResource(1);
        resource.putValue(property1, 1);
        model.getContentResourceSet().add(resource);

        LightweightCollection<VisualItem> visualItems = model
                .getFullVisualItemContainer().getVisualItems();

        assertEquals(1, visualItems.size());
        Iterator<VisualItem> iterator = visualItems.iterator();
        VisualItem first = iterator.next();
        assertEquals(first.getValue(requiredSlots[0]), 1);
    }

}
