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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.resources.ResourceMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.util.DisposeUtil;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Decorator for {@link VisualizationModel} that sets fixed
 * {@link VisualItemValueResolver} s for one or more {@link Slot}s. The fixed
 * slots are not exposed by this decorator, only the configurable ones.
 * 
 * @author Lars Grammel
 */
// TODO needs more tests & features, e.g. for the error model decoration
public class FixedSlotResolversVisualizationModelDecorator implements
        VisualizationModel, Disposable {

    private VisualizationModel delegate;

    private Map<Slot, VisualItemValueResolver> fixedSlotResolvers;

    private Slot[] slots;

    private HandlerManager slotMappingChangedHandlerManager = new HandlerManager(
            this);

    private HandlerRegistration handlerRegistration;

    public FixedSlotResolversVisualizationModelDecorator(
            VisualizationModel delegate,
            Map<Slot, VisualItemValueResolver> fixedSlotResolvers) {

        assert delegate != null;
        assert fixedSlotResolvers != null;

        this.delegate = delegate;
        this.fixedSlotResolvers = fixedSlotResolvers;

        initFixedSlots();
        initAvailableSlots(delegate, fixedSlotResolvers);
        initDelegateHandlers();
    }

    @Override
    public HandlerRegistration addHandler(SlotMappingChangedHandler handler) {
        return slotMappingChangedHandlerManager.addHandler(
                SlotMappingChangedEvent.TYPE, handler);
    }

    @Override
    public boolean containsSlot(Slot slot) {
        return delegate.containsSlot(slot);
    }

    @Override
    public void dispose() {
        handlerRegistration.removeHandler();
        handlerRegistration = null;

        DisposeUtil.dispose(delegate);
    }

    @Override
    public ResourceMultiCategorizer getCategorizer() {
        return delegate.getCategorizer();
    }

    @Override
    public ResourceSet getContentResourceSet() {
        return delegate.getContentResourceSet();
    }

    @Override
    public VisualItemContainer getErrorFreeVisualItemContainer() {
        return delegate.getErrorFreeVisualItemContainer();
    }

    private LightweightCollection<Slot> getFixedSlots() {
        LightweightList<Slot> result = CollectionFactory
                .createLightweightList();
        result.addAll(fixedSlotResolvers.keySet());
        return result;
    }

    @Override
    public VisualItemContainer getFullVisualItemContainer() {
        return delegate.getFullVisualItemContainer();
    }

    @Override
    public ResourceSet getHighlightedResources() {
        return delegate.getHighlightedResources();
    }

    @Override
    public VisualItemValueResolver getResolver(Slot slot) {
        return delegate.getResolver(slot);
    }

    @Override
    public ResourceSet getSelectedResources() {
        return delegate.getSelectedResources();
    }

    @Override
    public Slot getSlotById(String slotId) {
        return delegate.getSlotById(slotId);
    }

    @Override
    public Slot[] getSlots() {
        return slots;
    }

    @Override
    public LightweightCollection<Slot> getSlotsWithErrors() {
        return LightweightCollections.getRelativeComplement(
                delegate.getSlotsWithErrors(), getFixedSlots());
    }

    @Override
    public LightweightCollection<Slot> getSlotsWithErrors(VisualItem visualItem) {
        return delegate.getSlotsWithErrors(visualItem);
    }

    @Override
    public LightweightCollection<Slot> getUnconfiguredSlots() {
        return delegate.getUnconfiguredSlots();
    }

    @Override
    public ViewContentDisplay getViewContentDisplay() {
        return delegate.getViewContentDisplay();
    }

    @Override
    public LightweightCollection<VisualItem> getVisualItemsWithErrors() {
        return delegate.getVisualItemsWithErrors();
    }

    @Override
    public LightweightCollection<VisualItem> getVisualItemsWithErrors(Slot slot) {
        return delegate.getVisualItemsWithErrors(slot);
    }

    @Override
    public boolean hasErrors() {
        return delegate.hasErrors();
    }

    @Override
    public boolean hasErrors(Slot slot) {
        return delegate.hasErrors(slot);
    }

    @Override
    public boolean hasErrors(VisualItem visualItem) {
        return delegate.hasErrors(visualItem);
    }

    /**
     * This method calculates and initialized the slots field to the non-fixed
     * slots that are in the slotMappingConfiguration of the delegate. These
     * slots represent slots that the user is able to configure in the UI
     */
    private void initAvailableSlots(VisualizationModel delegate,
            Map<Slot, VisualItemValueResolver> fixedSlotResolvers) {
        ArrayList<Slot> slotList = new ArrayList<Slot>();
        slotList.addAll(Arrays.asList(delegate.getSlots()));
        slotList.removeAll(fixedSlotResolvers.keySet());
        this.slots = slotList.toArray(new Slot[slotList.size()]);
    }

    public void initDelegateHandlers() {
        handlerRegistration = this.delegate
                .addHandler(new SlotMappingChangedHandler() {
                    @Override
                    public void onSlotMappingChanged(SlotMappingChangedEvent e) {
                        if (!fixedSlotResolvers.containsKey(e.getSlot())) {
                            slotMappingChangedHandlerManager.fireEvent(e);
                        }
                    }
                });
    }

    private void initFixedSlots() {
        for (Entry<Slot, VisualItemValueResolver> entry : fixedSlotResolvers
                .entrySet()) {
            delegate.setResolver(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean isConfigured(Slot slot) {
        return delegate.isConfigured(slot);
    }

    @Override
    public void setCategorizer(ResourceMultiCategorizer newCategorizer) {
        delegate.setCategorizer(newCategorizer);
    }

    @Override
    public void setContentResourceSet(ResourceSet resources) {
        delegate.setContentResourceSet(resources);
    }

    @Override
    public void setResolver(Slot slot, VisualItemValueResolver resolver) {
        delegate.setResolver(slot, resolver);
    }

}
