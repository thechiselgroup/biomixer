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

import java.util.EnumMap;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.event.EventHandlerPriority;
import org.thechiselgroup.biomixer.client.core.util.event.PrioritizedEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.NoResolverForSlotException;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingResolutionException;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteractionHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;

/**
 * Default implementation of {@link VisualItem}.
 * <p>
 * <b>PERFORMANCE NOTE</b>: Provides caching for calculated slot values and for
 * highlighting and selection status.
 * </p>
 * 
 * @author Lars Grammel
 */
public class DefaultVisualItem implements Disposable, VisualItem {

    private final class CacheUpdateOnResourceSetChange implements
            ResourceSetChangedEventHandler, PrioritizedEventHandler {

        @Override
        public EventHandlerPriority getPriority() {
            return EventHandlerPriority.FIRST;
        }

        @Override
        public void onResourceSetChanged(ResourceSetChangedEvent event) {
            valueCache.clear();
        }
    }

    private final class SubsetContainer {

        private ResourceSet resources = new DefaultResourceSet();

        private Status cachedStatus;

        private Status calculateSubsetStatus() {
            if (resources.isEmpty()) {
                return Status.NONE;
            }

            if (resources.containsEqualResources(content)) {
                return Status.FULL;
            }

            return Status.PARTIAL;
        }

        public void clearStatusCache() {
            cachedStatus = null;
        }

        public Status getStatus() {
            if (cachedStatus == null) {
                cachedStatus = calculateSubsetStatus();
            }

            return cachedStatus;
        }

        public void update(
                LightweightCollection<Resource> addedSubsetResources,
                LightweightCollection<Resource> removedSubsetResources) {

            clearStatusCache();
            resources.addAll(content.getIntersection(addedSubsetResources));
            resources
                    .removeAll(content.getIntersection(removedSubsetResources));
        }

    }

    private String id;

    // TODO update & paint on changes in resources!!!
    private final ResourceSet content;

    private final VisualItemValueResolverContext valueResolverContext;

    /**
     * The representation of this resource item in the specific display. This is
     * set by the display to enable fast reference to this display element, and
     * should be casted into the specific type.
     */
    // TODO dispose
    private Object displayObject;

    private Map<Subset, SubsetContainer> subsets = new EnumMap<Subset, SubsetContainer>(
            Subset.class);

    /**
     * PERFORMANCE: Cache for the resolved slot values of ALL subset. Maps the
     * slot id to the value.
     * 
     * @see #clearValueCache(Slot)
     */
    private Map<String, Object> valueCache = CollectionFactory
            .createStringMap();

    private final VisualItemInteractionHandler interactionHandler;

    public DefaultVisualItem(String id, ResourceSet content,
            VisualItemValueResolverContext valueResolverContext,
            VisualItemInteractionHandler interactionHandler) {

        assert id != null;
        assert content != null;
        assert valueResolverContext != null;
        assert interactionHandler != null;

        this.id = id;
        this.content = content;
        this.valueResolverContext = valueResolverContext;
        this.interactionHandler = interactionHandler;

        this.subsets.put(Subset.SELECTED, new SubsetContainer());
        this.subsets.put(Subset.HIGHLIGHTED, new SubsetContainer());

        this.content.addEventHandler(new CacheUpdateOnResourceSetChange());
        this.content.addEventHandler(new ResourceSetChangedEventHandler() {
            @Override
            public void onResourceSetChanged(ResourceSetChangedEvent event) {
                LightweightCollection<Resource> removedResources = event
                        .getRemovedResources();

                getResources(Subset.HIGHLIGHTED).removeAll(removedResources);
                getResources(Subset.SELECTED).removeAll(removedResources);
            }
        });
    }

    /**
     * <p>
     * Clears the value cached for {@code slot}.
     * </p>
     * <p>
     * We have chosen to provide an explicit clear method that is called by
     * {@link DefaultVisualizationModel} instead of listening for slot mapping
     * changes, because there are several constraints on the execution order
     * (e.g. the cache needs to be cleared before the view is re-rendered) and
     * those constraints are easier to develop, test and understand when they
     * are specified in a straight-forward algorithm.
     * </p>
     */
    public void clearValueCache(Slot slot) {
        valueCache.remove(slot.getId());
    }

    @Override
    public void dispose() {
        // XXX deregister event listeners
        // XXX dispose intersection sets

    }

    @Override
    public Object getDisplayObject() {
        return displayObject;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ResourceSet getResources() {
        return content;
    }

    @Override
    public ResourceSet getResources(Subset subset) {
        assert subset != null;

        switch (subset) {
        case ALL:
            return getResources();
        case HIGHLIGHTED:
        case SELECTED:
            return getSubsetContainer(subset).resources;
        }

        throw new RuntimeException("invalid subset: " + subset);
    }

    // TODO move, refactor
    @Override
    public Slot[] getSlots() {
        return valueResolverContext.getSlots();
    }

    @Override
    public Status getStatus(Subset subset) {
        assert subset != null;

        switch (subset) {
        case ALL:
            // always containing all contained resources
            return Status.FULL;
        case HIGHLIGHTED:
        case SELECTED:
            return getSubsetContainer(subset).getStatus();
        }

        throw new RuntimeException("invalid subset: " + subset);
    }

    private SubsetContainer getSubsetContainer(Subset subset) {
        return subsets.get(subset);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(Slot slot) {
        assert slot != null : "slot must not be null";

        String slotId = slot.getId();
        if (valueCache.containsKey(slotId)) {
            return (T) valueCache.get(slotId);
        }

        try {
            Object value = valueResolverContext.getResolver(slot).resolve(this,
                    valueResolverContext);

            valueCache.put(slotId, value);

            return (T) value;
        } catch (NoResolverForSlotException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SlotMappingResolutionException(slot, this, ex);
        }
    }

    @Override
    public double getValueAsDouble(Slot slot) {
        return this.<Number> getValue(slot).doubleValue();
    }

    @Override
    public boolean isStatus(Subset subset, Status... status) {
        Status realStatus = getStatus(subset);
        for (Status expectedStatus : status) {
            if (realStatus.equals(expectedStatus)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void reportInteraction(VisualItemInteraction interaction) {
        assert interaction != null;
        interactionHandler.onInteraction(this, interaction);
    }

    @Override
    public void setDisplayObject(Object displayObject) {
        this.displayObject = displayObject;
    }

    @Override
    public String toString() {
        return "VisualItem[" + content.toString() + "]";
    }

    public void updateSubset(Subset subset,
            LightweightCollection<Resource> addedSubsetResources,
            LightweightCollection<Resource> removedSubsetResources) {

        assert addedSubsetResources != null;
        assert removedSubsetResources != null;

        valueCache.clear();
        getSubsetContainer(subset).update(addedSubsetResources,
                removedSubsetResources);
    }

}