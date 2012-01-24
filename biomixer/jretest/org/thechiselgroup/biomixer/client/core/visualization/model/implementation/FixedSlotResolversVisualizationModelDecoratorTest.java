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

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.equalsArray;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;

public class FixedSlotResolversVisualizationModelDecoratorTest {

    private FixedSlotResolversVisualizationModelDecorator underTest;

    private Slot fixedSlot;

    private Slot otherSlot;

    @Mock
    private VisualizationModel delegate;

    @Mock
    private VisualItemValueResolver resolver;

    private SlotMappingChangedEvent fireSlotMappingChangeOnDelegate(Slot slot) {
        // keep listeners on delegate
        ArgumentCaptor<SlotMappingChangedHandler> captor = ArgumentCaptor
                .forClass(SlotMappingChangedHandler.class);
        verify(delegate, atLeastOnce()).addHandler(captor.capture());

        // fire event for other slots on listeners for delegate
        SlotMappingChangedEvent event = new SlotMappingChangedEvent(slot,
                mock(VisualItemValueResolver.class),
                mock(VisualItemValueResolver.class));
        for (SlotMappingChangedHandler registeredHandler : captor
                .getAllValues()) {
            registeredHandler.onSlotMappingChanged(event);
        }
        return event;
    }

    @Test
    public void getSlotsExcludesFixedSlots() {
        initUnderTest(fixedSlot, otherSlot);

        assertThat(underTest.getSlots(), equalsArray(otherSlot));
    }

    private void initUnderTest(Slot... availableSlots) {
        Map<Slot, VisualItemValueResolver> fixedSlotResolvers = new HashMap<Slot, VisualItemValueResolver>();
        fixedSlotResolvers.put(fixedSlot, resolver);

        when(delegate.getSlots()).thenReturn(availableSlots);

        underTest = new FixedSlotResolversVisualizationModelDecorator(delegate,
                fixedSlotResolvers);
    }

    @Test
    public void setFixedSlotResolver() {
        initUnderTest(fixedSlot);

        verify(delegate, times(1)).setResolver(fixedSlot, resolver);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        fixedSlot = new Slot("s1", "", DataType.NUMBER);
        otherSlot = new Slot("s2", "", DataType.NUMBER);
    }

    @Test
    public void slotChangeEventsOnFixedSlotsGetFilteredOut() {
        initUnderTest(fixedSlot, otherSlot);

        SlotMappingChangedHandler handler = mock(SlotMappingChangedHandler.class);
        underTest.addHandler(handler);

        SlotMappingChangedEvent event = fireSlotMappingChangeOnDelegate(fixedSlot);

        verify(handler, never()).onSlotMappingChanged(event);
    }

    @Test
    public void slotChangeEventsOnOtherSlotsGetNotFilteredOut() {
        initUnderTest(fixedSlot, otherSlot);

        SlotMappingChangedHandler handler = mock(SlotMappingChangedHandler.class);
        underTest.addHandler(handler);

        SlotMappingChangedEvent event = fireSlotMappingChangeOnDelegate(otherSlot);

        verify(handler, times(1)).onSlotMappingChanged(event);
    }
}