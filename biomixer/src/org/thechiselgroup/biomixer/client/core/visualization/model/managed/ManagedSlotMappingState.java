package org.thechiselgroup.biomixer.client.core.visualization.model.managed;

import java.util.Collection;

import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;

/**
 * Data class that represents a snapshot of the state of a ManagedSlotMapping.
 * These are used to pass into the initializer so that it can make good
 * decisions on how to fix a broken resolution
 * 
 * All values except for the Slot can be changed at runtime, so that it acts sor
 * of like a Delta
 */
public class ManagedSlotMappingState {

    private final Slot slot;

    private ManagedVisualItemValueResolver resolver;

    private boolean isConfigured;

    private boolean isAllowable;

    private Collection<VisualItemValueResolverFactory> allowableFactories;

    public ManagedSlotMappingState(Slot slot,
            ManagedVisualItemValueResolver resolver, boolean isConfigured,
            boolean isAllowable,
            Collection<VisualItemValueResolverFactory> allowableFactories) {
        this.slot = slot;
        this.resolver = resolver;
        this.isConfigured = isConfigured;
        this.isAllowable = isAllowable;
        this.allowableFactories = allowableFactories;

    }

    public Collection<VisualItemValueResolverFactory> getAllowableFactories() {
        return allowableFactories;
    }

    public ManagedVisualItemValueResolver getResolver() {
        return resolver;
    }

    public Slot getSlot() {
        return slot;
    }

    public boolean isAllowable() {
        return isAllowable;
    }

    public boolean isConfigured() {
        return isConfigured;
    }
}
