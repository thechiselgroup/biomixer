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
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockResolver;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;

public class SlotMappingConfigurationTest {

    private DefaultSlotMappingConfiguration underTest;

    private Slot slot1;

    private Slot slot2;

    @Test
    public void fireChangesForDelegatingSlotResolversWhenTargetResolverIsChanged() {
        VisualItemValueResolver delegatingResolver = mock(VisualItemValueResolver.class);
        when(delegatingResolver.getTargetSlots()).thenReturn(
                LightweightCollections.toCollection(slot1));

        underTest.setResolver(slot1, mockResolver());
        underTest.setResolver(slot2, delegatingResolver);

        SlotMappingChangedHandler handler = mock(SlotMappingChangedHandler.class);
        underTest.addHandler(handler);

        /*
         * changing slot 1 should trigger events for the delegating resolver at
         * slot 2, because it refers to slot 1.
         */
        underTest.setResolver(slot1, mockResolver());

        verify(handler, times(1)).onSlotMappingChanged(
                argThat(new IsChangeForSlotMatcher(slot2)));
    }

    @Test
    public void getUnconfiguredSlots() {
        underTest.setResolver(slot1, mockResolver());

        assertThat(underTest.getUnconfiguredSlots(), containsExactly(slot2));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        slot1 = new Slot("s1", "", DataType.NUMBER);
        slot2 = new Slot("s2", "", DataType.NUMBER);

        underTest = new DefaultSlotMappingConfiguration(new Slot[] { slot1,
                slot2 });
    }

}