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
package org.thechiselgroup.biomixer.client.core.visualization.model;

import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.event.PrioritizedEventHandler;

import com.google.gwt.event.shared.HandlerRegistration;

//TODO rename to SlotMappingConfiguration
public interface SlotMappingConfiguration extends
        VisualItemValueResolverContext {

    /**
     * Adds an event handler that gets called when mappings change.
     * 
     * @param handler
     *            {@link SlotMappingChangedHandler} that gets called when a slot
     *            mapping changes. Supports {@link PrioritizedEventHandler}.
     */
    HandlerRegistration addHandler(SlotMappingChangedHandler handler);

    /**
     * @return {@code true} if {@code slot} is allowed in this
     *         {@link SlotMappingConfiguration}.
     */
    boolean containsSlot(Slot slot);

    /**
     * @param slotId
     *            id of the slot
     * @return {@link Slot} with the ID {@code slotId}
     */
    // TODO throws InvalidSlotException
    Slot getSlotById(String slotId);

    /**
     * @return {@link Slot}s from {@link #getSlots()} for which no
     *         {@link VisualItemValueResolver} are configured.
     * 
     * @see #isConfigured(Slot)
     */
    LightweightCollection<Slot> getUnconfiguredSlots();

    // TODO document
    // TODO throws InvalidSlotException
    void setResolver(Slot slot, VisualItemValueResolver resolver);

}