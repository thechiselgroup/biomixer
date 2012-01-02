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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.createSlots;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.model.initialization.TestSlotMappingInitializer;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualItemResolutionErrorModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemTestUtils;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.DefaultManagedSlotMappingConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedSlotMapping;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedSlotMappingConfigurationChangedEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedSlotMappingConfigurationChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedVisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.SlotMappingInitializer;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.VisualItemValueResolverFactory;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.VisualItemValueResolverFactoryProvider;

/**
 * 
 * @author Lars Grammel
 * @author Patrick Gorman
 */

public class ManagedSlotMappingConfigurationTest {

    private static final String RESOLVER_ID_1 = "resolver-id-1";

    private static final String RESOLVER_ID_2 = "resolver-id-2";

    public static Matcher<LightweightCollection<ManagedSlotMapping>> uiModelsContainSlots(
            final Slot... slots) {

        return new TypeSafeMatcher<LightweightCollection<ManagedSlotMapping>>() {
            @Override
            public void describeTo(Description description) {
                for (Slot slot : slots) {
                    description.appendValue(slot);
                }
            }

            @Override
            public boolean matchesSafely(
                    LightweightCollection<ManagedSlotMapping> uiModels) {
                if (uiModels.size() != slots.length) {
                    return false;
                }

                for (Slot slot : slots) {
                    boolean found = false;
                    for (ManagedSlotMapping uiModel : uiModels) {
                        if (uiModel.getSlot().equals(slot)) {
                            found = true;
                        }
                    }

                    if (!found) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private DefaultManagedSlotMappingConfiguration underTest;

    @Mock
    private VisualItemValueResolverFactoryProvider resolverProvider;

    @Mock
    private SlotMappingInitializer slotMappingInitializer;

    @Mock
    private VisualizationModel visualizationModel;

    private Slot[] slots;

    @Mock
    private VisualItemValueResolverFactory factory1;

    @Mock
    private ManagedVisualItemValueResolver resolver1;

    @Mock
    private ManagedVisualItemValueResolver resolver2;

    private DefaultVisualItemResolutionErrorModel errorModel;

    @Mock
    private VisualItemValueResolverFactory factory2;

    @Mock
    private VisualItemContainer visualItemContainer;

    private SlotMappingChangedEvent captureSlotMappingChangedEvent(
            SlotMappingChangedHandler handler) {
        ArgumentCaptor<SlotMappingChangedEvent> captor = ArgumentCaptor
                .forClass(SlotMappingChangedEvent.class);
        verify(handler, times(1)).onSlotMappingChanged(captor.capture());
        return captor.getValue();
    }

    private SlotMappingChangedHandler captureSlotMappingChangedHandler() {
        ArgumentCaptor<SlotMappingChangedHandler> captor = ArgumentCaptor
                .forClass(SlotMappingChangedHandler.class);
        verify(visualizationModel, times(1)).addHandler(captor.capture());
        return captor.getValue();
    }

    private VisualItemContainerChangeEventHandler captureVisualItemContainerChangeEventHandler() {
        ArgumentCaptor<VisualItemContainerChangeEventHandler> captor = ArgumentCaptor
                .forClass(VisualItemContainerChangeEventHandler.class);
        verify(visualItemContainer, times(1)).addHandler(captor.capture());
        return captor.getValue();
    }

    @Test
    public void configurationUIModelContainsUIModelForEachSlotInViewModel() {
        setUpSlots(DataType.TEXT, DataType.NUMBER);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);
        LightweightCollection<ManagedSlotMapping> uiModels = underTest
                .getManagedSlotMappings();

        assertThat(uiModels, uiModelsContainSlots(slots));
    }

    @Test
    public void configurationUIModelContainsUIModelForOneSlotInViewModel() {
        setUpSlots(DataType.TEXT);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);
        LightweightList<ManagedSlotMapping> uiModels = underTest
                .getManagedSlotMappings();

        assertThat(uiModels, uiModelsContainSlots(slots));
    }

    @Test
    public void errorInErrorModelResolverInViewModelNotApplicable() {
        setUpSlots(DataType.TEXT);
        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);

        VisualItem visualItem = mock(VisualItem.class);
        when(visualItem.getId()).thenReturn("a");
        errorModel.reportError(slots[0], visualItem);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        assertThat(underTest.getSlotsWithInvalidResolvers(),
                containsExactly(slots[0]));
    }

    @Test
    public void getResolverFromViewModelContext() {
        setUpSlots(DataType.TEXT);
        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        assertEquals(resolver1, underTest.getCurrentResolver(slots[0]));
    }

    @SuppressWarnings("unchecked")
    public void mockFactory(VisualItemValueResolverFactory factory, String id,
            ManagedVisualItemValueResolver resolver) {
        when(factory.getId()).thenReturn(id);
        when(
                factory.canCreateApplicableResolver(any(Slot.class),
                        any(LightweightList.class))).thenReturn(true);

        when(factory.create(any(LightweightList.class))).thenReturn(resolver);
    }

    private void mockResolversAndFactories() {
        mockFactory(factory1, RESOLVER_ID_1, resolver1);
        mockFactory(factory2, RESOLVER_ID_2, resolver2);
        when(resolver1.getId()).thenReturn(RESOLVER_ID_1);
        when(
                resolver1.canResolve(any(VisualItem.class),
                        any(VisualItemValueResolverContext.class))).thenReturn(
                true);
        when(resolver2.getId()).thenReturn(RESOLVER_ID_2);
        when(
                resolver2.canResolve(any(VisualItem.class),
                        any(VisualItemValueResolverContext.class))).thenReturn(
                true);
    }

    @Test
    public void nonAllowableResolverInViewModelNotApplicable() {
        setUpSlots(DataType.TEXT);
        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver2);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        assertEquals(1, underTest.getSlotsWithInvalidResolvers().size());
        assertEquals(slots[0], underTest.getSlotsWithInvalidResolvers().get(0));
    }

    @Test
    public void nonManagedResolverInViewModelNotApplicable() {
        setUpSlots(DataType.TEXT);
        VisualItemValueResolver unManagedResolver = mock(VisualItemValueResolver.class);
        when(visualizationModel.getResolver(slots[0])).thenReturn(
                unManagedResolver);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        assertThat(underTest.getSlotsWithInvalidResolvers(),
                containsExactly(slots[0]));
    }

    @Test
    public void resolversInitializedWhenVisualItemsAdded() {
        setUpSlots(DataType.TEXT);

        Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        initialSlotMapping.put(slots[0], resolver1);
        SlotMappingInitializer initializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                initializer, visualizationModel, errorModel);

        VisualItemContainerChangeEventHandler handler = captureVisualItemContainerChangeEventHandler();

        when(visualizationModel.getFullVisualItemContainer()).thenReturn(
                visualItemContainer);
        when(visualItemContainer.getVisualItems()).thenReturn(
                CollectionFactory.<VisualItem> createLightweightList());

        LightweightList<Slot> badSlots = CollectionFactory
                .createLightweightList();
        badSlots.add(slots[0]);
        when(visualizationModel.getSlotsWithErrors()).thenReturn(badSlots);

        LightweightCollection<VisualItem> addedElements = VisualItemTestUtils
                .createVisualItems(1);

        Delta<VisualItem> delta = Delta.createAddedDelta(addedElements);
        // XXX right now underTest does not care what the event is, but it may
        // in the future, feel free to implement the event in this test in the
        // future
        handler.onVisualItemContainerChanged(new VisualItemContainerChangeEvent(
                visualItemContainer, delta));

        // verify that we set the viewModel
        verify(visualizationModel, times(1)).setResolver(slots[0], resolver1);
    }

    @Test
    public void resolversNotSetBeforeVisualItemsAdded() {
        setUpSlots(DataType.TEXT);

        Map<Slot, VisualItemValueResolver> initialSlotMapping = new HashMap<Slot, VisualItemValueResolver>();
        initialSlotMapping.put(slots[0], resolver1);
        SlotMappingInitializer initializer = new TestSlotMappingInitializer(
                initialSlotMapping);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                initializer, visualizationModel, errorModel);

        assertThat(underTest.getSlotsWithInvalidResolvers(),
                containsExactly(slots[0]));
    }

    @Test
    public void setResolverOnconfigurationUIModelUpdatesViewModel() {
        setUpSlots(DataType.TEXT);

        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        underTest.setCurrentResolver(slots[0], resolver2);
        verify(visualizationModel, times(1)).setResolver(slots[0], resolver2);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockResolversAndFactories();
        setUpResolverProvider(factory1);

        when(visualizationModel.getFullVisualItemContainer()).thenReturn(
                visualItemContainer);

        errorModel = new DefaultVisualItemResolutionErrorModel();
    }

    public void setUpResolverProvider(
            VisualItemValueResolverFactory... factories) {

        LightweightList<VisualItemValueResolverFactory> factoryList = CollectionFactory
                .createLightweightList();

        for (VisualItemValueResolverFactory factory : factories) {
            factoryList.add(factory);
        }

        when(resolverProvider.getAll()).thenReturn(factoryList);
    }

    private void setUpSlots(DataType... dataTypes) {
        slots = createSlots(dataTypes);
        when(visualizationModel.getSlots()).thenReturn(slots);
        when(visualizationModel.containsSlot(any(Slot.class))).thenReturn(true);
    }

    @Test
    public void slotMappingChangedEventWithChangeFiresEventToUI() {
        setUpSlots(DataType.TEXT);
        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);
        when(visualizationModel.getSlotsWithErrors()).thenReturn(
                CollectionFactory.<Slot> createLightweightList());

        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1);
        when(visualItemContainer.getVisualItems()).thenReturn(visualItems);

        setUpResolverProvider(factory1, factory2);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        VisualItemContainerChangeEventHandler visualItemContainerHandler = captureVisualItemContainerChangeEventHandler();

        ManagedSlotMappingConfigurationChangedEventHandler handler = mock(ManagedSlotMappingConfigurationChangedEventHandler.class);
        underTest
                .addManagedSlotMappingConfigurationChangedEventHandler(handler);

        VisualItemContainerChangeEvent event = new VisualItemContainerChangeEvent(
                visualItemContainer, Delta.createAddedDelta(visualItems));
        visualItemContainerHandler.onVisualItemContainerChanged(event);

        verify(handler, times(1)).onSlotMappingStateChanged(
                any(ManagedSlotMappingConfigurationChangedEvent.class));
    }

    @Test
    public void slotMappingChangedEventWithNoChangeDoesNotFireNewEventToUI() {
        setUpSlots(DataType.TEXT);
        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);
        when(visualizationModel.getSlotsWithErrors()).thenReturn(
                CollectionFactory.<Slot> createLightweightList());

        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1);
        when(visualItemContainer.getVisualItems()).thenReturn(visualItems);

        setUpResolverProvider(factory1, factory2);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        VisualItemContainerChangeEventHandler visualItemContainerHandler = captureVisualItemContainerChangeEventHandler();

        ManagedSlotMappingConfigurationChangedEventHandler handler = mock(ManagedSlotMappingConfigurationChangedEventHandler.class);
        underTest
                .addManagedSlotMappingConfigurationChangedEventHandler(handler);

        VisualItemContainerChangeEvent event = new VisualItemContainerChangeEvent(
                visualItemContainer, Delta.createAddedDelta(CollectionFactory
                        .<VisualItem> createLightweightList()));
        visualItemContainerHandler.onVisualItemContainerChanged(event);

        verify(handler, never()).onSlotMappingStateChanged(
                any(ManagedSlotMappingConfigurationChangedEvent.class));
    }

    @Test
    public void viewModelFiredSlotMappingChangedEventFiresEventOnUnderTest() {
        setUpSlots(DataType.TEXT);

        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);
        setUpResolverProvider(factory1, factory2);
        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        ManagedSlotMapping slotMapping = underTest
                .getManagedSlotMapping(slots[0]);
        SlotMappingChangedHandler uiModelHandler = mock(SlotMappingChangedHandler.class);
        slotMapping.addSlotMappingEventHandler(uiModelHandler);

        SlotMappingChangedHandler handler = captureSlotMappingChangedHandler();
        SlotMappingChangedEvent event = new SlotMappingChangedEvent(slots[0],
                resolver1, resolver2);
        handler.onSlotMappingChanged(event);

        SlotMappingChangedEvent resultingEvent = captureSlotMappingChangedEvent(uiModelHandler);
        assertEquals(resultingEvent.getCurrentResolver(), resolver2);
        assertEquals(resultingEvent.getSlot(), slots[0]);
    }

    @Test
    public void viewModelResolverChangesAreReflectedInUIModelThroughContext() {
        setUpSlots(DataType.TEXT);

        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);

        setUpResolverProvider(factory1, factory2);
        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver2);
        assertEquals(resolver2, underTest.getCurrentResolver(slots[0]));
        assertThat(underTest.getSlotsWithInvalidResolvers(),
                containsExactly(CollectionFactory
                        .<Slot> createLightweightList()));
    }

    @Test
    public void viewModelResolverChangesToInvalidStateReflectedInUIModel() {
        setUpSlots(DataType.TEXT);

        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);

        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver2);
        assertThat(underTest.getSlotsWithInvalidResolvers(),
                containsExactly(slots));
    }

    @Test
    public void visualItemsChangedEventWithChangeFiresEventToUI() {
        setUpSlots(DataType.TEXT);

        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);
        setUpResolverProvider(factory1, factory2);
        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        SlotMappingChangedHandler slotMappingChangeHandler = captureSlotMappingChangedHandler();
        ManagedSlotMappingConfigurationChangedEventHandler handler = mock(ManagedSlotMappingConfigurationChangedEventHandler.class);
        underTest
                .addManagedSlotMappingConfigurationChangedEventHandler(handler);

        slotMappingChangeHandler
                .onSlotMappingChanged(new SlotMappingChangedEvent(slots[0],
                        resolver1, resolver2));

        verify(handler, times(1)).onSlotMappingStateChanged(
                any(ManagedSlotMappingConfigurationChangedEvent.class));

    }

    @Test
    public void visualItemsChangedEventWithNoChangeDoesNotFireNewEventToUI() {
        setUpSlots(DataType.TEXT);

        when(visualizationModel.getResolver(slots[0])).thenReturn(resolver1);
        setUpResolverProvider(factory1, factory2);
        underTest = new DefaultManagedSlotMappingConfiguration(resolverProvider,
                slotMappingInitializer, visualizationModel, errorModel);

        SlotMappingChangedHandler slotMappingChangeHandler = captureSlotMappingChangedHandler();
        ManagedSlotMappingConfigurationChangedEventHandler handler = mock(ManagedSlotMappingConfigurationChangedEventHandler.class);
        underTest
                .addManagedSlotMappingConfigurationChangedEventHandler(handler);

        slotMappingChangeHandler
                .onSlotMappingChanged(new SlotMappingChangedEvent(slots[0],
                        resolver1, resolver1));

        verify(handler, times(0)).onSlotMappingStateChanged(
                any(ManagedSlotMappingConfigurationChangedEvent.class));
    }
}
