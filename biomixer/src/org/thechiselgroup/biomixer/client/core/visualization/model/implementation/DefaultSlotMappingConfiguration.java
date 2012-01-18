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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiableSet;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.event.PrioritizedHandlerManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.NoResolverForSlotException;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;

import com.google.gwt.event.shared.HandlerRegistration;

public class DefaultSlotMappingConfiguration implements
        SlotMappingConfiguration {

    private transient PrioritizedHandlerManager handlerManager;

    /**
     * Configured slots and their resolvers.
     */
    private Map<Slot, VisualItemValueResolver> slotsToResolvers = new HashMap<Slot, VisualItemValueResolver>();

    /**
     * All allowed slots by their ids.
     */
    private IdentifiableSet<Slot> slotsByID = new IdentifiableSet<Slot>();

    private Slot[] slots;

    public DefaultSlotMappingConfiguration(Slot[] slots) {
        assert slots != null;

        this.handlerManager = new PrioritizedHandlerManager(this);
        this.slots = slots;

        slotsByID.put(slots);

        assertInvariants();
    }

    @Override
    public HandlerRegistration addHandler(SlotMappingChangedHandler handler) {
        assert handler != null;
        return handlerManager.addHandler(SlotMappingChangedEvent.TYPE, handler);
    }

    private void assertAllResolversAreValid() {
        for (VisualItemValueResolver resolver : slotsToResolvers.values()) {
            assertValidResolver(resolver);
        }
    }

    private void assertInvariants() {
        assertNoNullSlotMappings();
        assertAllResolversAreValid();
    }

    private void assertNoNullSlotMappings() {
        for (Entry<Slot, VisualItemValueResolver> entry : slotsToResolvers
                .entrySet()) {
            assert entry.getValue() != null : "resolver for slot "
                    + entry.getKey() + " must not be null";
        }
    }

    private void assertValidResolver(VisualItemValueResolver resolver) {
        assert resolver != null : "resolver must not be null";
        assert resolver.getTargetSlots() != null : "resolver "
                + resolver.toString() + " getTargetSlots() must not be null";
    }

    private void assertValidSlot(Slot slot) {
        assert slot != null : "slot must not be null";
        assert containsSlot(slot) : "slot " + slot
                + " is not allowed (valid slots: " + slotsByID + ")";
    }

    @Override
    public boolean containsSlot(Slot slot) {
        return slotsByID.contains(slot);
    }

    /**
     * @return {@link Slot}s that have {@link VisualItemValueResolver}s that
     *         have {@code slot} as one of their target slots.
     * 
     * @see VisualItemValueResolver#getTargetSlots()
     */
    /*
     * XXX find deeper dependencies (several levels, use breadth/depth first
     * search)
     * 
     * XXX make sure slots are only found once (string set, test case)
     * 
     * XXX depth-first search must consider unconfigured slots
     */
    public LightweightList<Slot> getDependentSlots(Slot slot) {
        assertValidSlot(slot);

        LightweightList<Slot> dependentSlots = CollectionFactory
                .createLightweightList();
        for (Entry<Slot, VisualItemValueResolver> entry : slotsToResolvers
                .entrySet()) {

            VisualItemValueResolver otherResolver = entry.getValue();
            LightweightCollection<Slot> targetSlots = otherResolver
                    .getTargetSlots();

            for (Slot targetSlot : targetSlots) {
                if (targetSlot.equals(slot)) {
                    dependentSlots.add(entry.getKey());
                    break;
                }
            }
        }
        return dependentSlots;
    }

    @Override
    public VisualItemValueResolver getResolver(Slot slot)
            throws NoResolverForSlotException {

        assertValidSlot(slot);

        if (!isConfigured(slot)) {
            throw new NoResolverForSlotException(slot, slotsToResolvers);
        }

        assert slotsToResolvers.containsKey(slot);

        return slotsToResolvers.get(slot);
    }

    @Override
    public Slot getSlotById(String slotId) {
        assert slotId != null;
        return slotsByID.get(slotId);
    }

    @Override
    public Slot[] getSlots() {
        return slots;
    }

    @Override
    public LightweightCollection<Slot> getUnconfiguredSlots() {
        LightweightList<Slot> unconfiguredSlots = CollectionFactory
                .createLightweightList();
        for (Slot slot : slots) {
            if (!isConfigured(slot)) {
                unconfiguredSlots.add(slot);
            }
        }
        return unconfiguredSlots;
    }

    @Override
    public boolean isConfigured(Slot slot) {
        assertValidSlot(slot);

        return slotsToResolvers.containsKey(slot);
    }

    @Override
    public void setResolver(Slot slot, VisualItemValueResolver resolver) {
        assertInvariants();
        assertValidSlot(slot);
        assertValidResolver(resolver);

        VisualItemValueResolver oldResolver = slotsToResolvers.get(slot);
        slotsToResolvers.put(slot, resolver);

        handlerManager.fireEvent(new SlotMappingChangedEvent(slot, oldResolver,
                resolver));

        LightweightList<Slot> dependentSlots = getDependentSlots(slot);

        for (Slot dependentSlot : dependentSlots) {
            handlerManager.fireEvent(new SlotMappingChangedEvent(dependentSlot,
                    oldResolver, resolver));
        }

        assertInvariants();
    }
}