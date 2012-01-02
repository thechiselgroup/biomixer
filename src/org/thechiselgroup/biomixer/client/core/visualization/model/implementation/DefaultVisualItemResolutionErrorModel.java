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

import static org.thechiselgroup.biomixer.client.core.util.AdditionalJavaAssertions.assertMapDoesNotContainEmptyLists;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.collections.ArrayListToLightweightListAdapter;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemResolutionErrorModel;

/**
 * <p>
 * Default implementation of {@link VisualItemResolutionErrorModel}.
 * </p>
 * <p>
 * IMPLEMENTATION NOTE: This class assumes that the {@link Slot}s that are
 * passed in have unique slot ids. If two different slots have the same slot id,
 * the functionality in this class will not work.
 * </p>
 * 
 * @author Lars Grammel
 */
public class DefaultVisualItemResolutionErrorModel implements
        VisualItemResolutionErrorModel, Cloneable {

    /**
     * Contains ids of all slots that have errors as keys, and the
     * {@link VisualItem}s that could not be resolved for that slot as values.
     * Slots without errors must not be contained.
     * 
     * @see #assertInvariantIntegrity()
     */
    private Map<String, ArrayListToLightweightListAdapter<VisualItem>> errorsBySlotId = CollectionFactory
            .createStringMap();

    /**
     * Contains ids of all {@link VisualItem}s that have errors as keys, and the
     * {@link Slots}s that could not be resolved for that {@link VisualItem} as
     * values. VisualItems without errors must not be contained.
     * 
     * @see #assertInvariantIntegrity()
     */
    private Map<String, ArrayListToLightweightListAdapter<Slot>> errorsByVisualItemId = CollectionFactory
            .createStringMap();

    private ArrayListToLightweightListAdapter<Slot> slotsWithErrors = new ArrayListToLightweightListAdapter<Slot>();

    private ArrayListToLightweightListAdapter<VisualItem> visualItemsWithErrors = new ArrayListToLightweightListAdapter<VisualItem>();

    private void addErrorSlotToVisualItem(VisualItem visualItem, Slot slot) {
        String visualItemId = visualItem.getId();

        if (!errorsByVisualItemId.containsKey(visualItemId)) {
            errorsByVisualItemId.put(visualItemId,
                    new ArrayListToLightweightListAdapter<Slot>());
            visualItemsWithErrors.add(visualItem);
        }

        ArrayListToLightweightListAdapter<Slot> visualItemErrors = errorsByVisualItemId
                .get(visualItemId);
        if (!visualItemErrors.contains(slot)) {
            visualItemErrors.add(slot);
        }
    }

    private void addErrorVisualItemToSlot(Slot slot, VisualItem visualItem) {
        String slotId = slot.getId();

        if (!errorsBySlotId.containsKey(slotId)) {
            errorsBySlotId.put(slotId,
                    new ArrayListToLightweightListAdapter<VisualItem>());
            slotsWithErrors.add(slot);
        }

        ArrayListToLightweightListAdapter<VisualItem> slotErrors = errorsBySlotId
                .get(slotId);
        if (!slotErrors.contains(visualItem)) {
            slotErrors.add(visualItem);
        }
    }

    private void assertErrorSlotsIntegrity() {
        assert errorsBySlotId.size() == slotsWithErrors.size();
        for (Slot slot : slotsWithErrors) {
            assert errorsBySlotId.containsKey(slot.getId()) : "Slot with id "
                    + slot.getId() + " not contained in slotsWithErrors";
        }
    }

    /**
     * There are the following class invariants:
     * 
     * <ul>
     * <li>errorsBySlotId must not contain empty lists</li>
     * <li>errorsByVisualItemId must not contain empty lists</li>
     * <li>errorsBySlotId keys must match Slots in slotsWithErrors</li>
     * <li>errorsByVisualItemId keys must match VisualItems in visualItemsWithErrors</li>
     * </ul>
     */
    private void assertInvariantIntegrity() {
        assertMapDoesNotContainEmptyLists(errorsBySlotId);
        assertMapDoesNotContainEmptyLists(errorsByVisualItemId);
        assertErrorSlotsIntegrity();
        assertVisualItemsIntegrity();
    }

    private void assertVisualItemsIntegrity() {
        assert errorsByVisualItemId.size() == visualItemsWithErrors.size();
        for (VisualItem visualItem : visualItemsWithErrors) {
            assert errorsByVisualItemId.containsKey(visualItem.getId()) : "VisualItem with id "
                    + visualItem.getId()
                    + " not contained in visualItemsWithErrors";
        }
    }

    public void clearErrors(LightweightCollection<VisualItem> visualItems) {
        assert visualItems != null;
        assertInvariantIntegrity();

        for (VisualItem visualItem : visualItems) {
            clearErrors(visualItem);
        }

        assertInvariantIntegrity();
    }

    public void clearErrors(Slot slot) {
        assert slot != null;
        assertInvariantIntegrity();

        if (!slotsWithErrors.contains(slot)) {
            return;
        }

        slotsWithErrors.remove(slot);
        ArrayListToLightweightListAdapter<VisualItem> removedVisualItems = errorsBySlotId
                .remove(slot.getId());
        for (VisualItem visualItem : removedVisualItems) {
            removeSlotFromVisualItemErrors(slot, visualItem);
        }

        assertInvariantIntegrity();
        assert !errorsBySlotId.containsKey(slot.getId());
        assert !hasErrors(slot);
    }

    public void clearErrors(VisualItem visualItem) {
        assert visualItem != null;
        assertInvariantIntegrity();

        if (!visualItemsWithErrors.contains(visualItem)) {
            return;
        }

        visualItemsWithErrors.remove(visualItem);
        ArrayListToLightweightListAdapter<Slot> removedSlots = errorsByVisualItemId
                .remove(visualItem.getId());
        for (Slot slot : removedSlots) {
            removeVisualItemFromSlotErrors(visualItem, slot);
        }

        assertInvariantIntegrity();
        assert !errorsByVisualItemId.containsKey(visualItem.getId());
        assert !hasErrors(visualItem);
    }

    @Override
    public LightweightCollection<Slot> getSlotsWithErrors() {
        return slotsWithErrors;
    }

    @Override
    public LightweightCollection<Slot> getSlotsWithErrors(VisualItem visualItem) {
        assert visualItem != null;

        if (!hasErrors(visualItem)) {
            return LightweightCollections.emptyCollection();
        }

        return errorsByVisualItemId.get(visualItem.getId());
    }

    @Override
    public LightweightCollection<VisualItem> getVisualItemsWithErrors() {
        return visualItemsWithErrors;
    }

    @Override
    public LightweightCollection<VisualItem> getVisualItemsWithErrors(Slot slot) {
        assert slot != null;

        if (!hasErrors(slot)) {
            return LightweightCollections.emptyCollection();
        }

        return errorsBySlotId.get(slot.getId());
    }

    @Override
    public boolean hasErrors() {
        return !slotsWithErrors.isEmpty();
    }

    @Override
    public boolean hasErrors(Slot slot) {
        assert slot != null;

        return errorsBySlotId.containsKey(slot.getId());
    }

    @Override
    public boolean hasErrors(VisualItem visualItem) {
        assert visualItem != null;

        return errorsByVisualItemId.containsKey(visualItem.getId());
    }

    public void removeError(Slot slot, VisualItem visualItem) {
        assert visualItem != null;
        assert slot != null;
        assertInvariantIntegrity();

        removeSlotFromVisualItemErrors(slot, visualItem);
        removeVisualItemFromSlotErrors(visualItem, slot);

        assertInvariantIntegrity();
    }

    public void removeSlotFromVisualItemErrors(Slot slot, VisualItem visualItem) {
        assert errorsByVisualItemId.containsKey(visualItem.getId());

        ArrayListToLightweightListAdapter<Slot> slotErrors = errorsByVisualItemId
                .get(visualItem.getId());
        slotErrors.remove(slot);

        // remove slot if no errors left
        if (slotErrors.isEmpty()) {
            errorsByVisualItemId.remove(visualItem.getId());
            visualItemsWithErrors.remove(visualItem);
        }
    }

    private void removeVisualItemFromSlotErrors(VisualItem visualItem, Slot slot) {
        assert errorsBySlotId.containsKey(slot.getId());

        ArrayListToLightweightListAdapter<VisualItem> slotErrors = errorsBySlotId
                .get(slot.getId());
        slotErrors.remove(visualItem);

        // remove slot if no errors left
        if (slotErrors.isEmpty()) {
            errorsBySlotId.remove(slot.getId());
            slotsWithErrors.remove(slot);
        }
    }

    public void reportError(Slot slot, VisualItem visualItem) {
        assert slot != null;
        assert visualItem != null;
        assertInvariantIntegrity();

        addErrorVisualItemToSlot(slot, visualItem);
        addErrorSlotToVisualItem(visualItem, slot);

        assertInvariantIntegrity();
    }

    public void reportErrors(Slot slot,
            LightweightCollection<VisualItem> visualItems) {

        assert slot != null;
        assert visualItems != null;
        assertInvariantIntegrity();

        for (VisualItem visualItem : visualItems) {
            reportError(slot, visualItem);
        }

        assertInvariantIntegrity();
    }

}
