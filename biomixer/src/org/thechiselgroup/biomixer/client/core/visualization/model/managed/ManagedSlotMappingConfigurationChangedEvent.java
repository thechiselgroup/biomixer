package org.thechiselgroup.biomixer.client.core.visualization.model.managed;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

import com.google.gwt.event.shared.GwtEvent;

public class ManagedSlotMappingConfigurationChangedEvent extends
        GwtEvent<ManagedSlotMappingConfigurationChangedEventHandler> {

    private final Map<Slot, ManagedSlotMappingState> slotConfigurationStates;

    private final LightweightCollection<VisualItem> visualItems;

    public static final GwtEvent.Type<ManagedSlotMappingConfigurationChangedEventHandler> TYPE = new GwtEvent.Type<ManagedSlotMappingConfigurationChangedEventHandler>();

    public ManagedSlotMappingConfigurationChangedEvent(
            Map<Slot, ManagedSlotMappingState> slotConfigurationStates,
            LightweightCollection<VisualItem> visualItems) {
        this.slotConfigurationStates = slotConfigurationStates;
        this.visualItems = visualItems;
    }

    @Override
    protected void dispatch(
            ManagedSlotMappingConfigurationChangedEventHandler handler) {
        handler.onSlotMappingStateChanged(this);
    }

    @Override
    public GwtEvent.Type<ManagedSlotMappingConfigurationChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public Map<Slot, ManagedSlotMappingState> getSlotConfigurationStates() {
        return slotConfigurationStates;
    }

    public LightweightCollection<VisualItem> getVisualItems() {
        return visualItems;
    }

}
