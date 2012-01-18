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
package org.thechiselgroup.biomixer.client.core.visualization.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.resources.DataTypeLists;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.HasResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceByPropertyMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceByUriMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetUtils;
import org.thechiselgroup.biomixer.client.core.ui.ConfigurationPanel;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.ExtendedListBox;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.ListBoxControl;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.transform.NullTransformer;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.DefaultManagedSlotMappingConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedSlotMappingConfigurationChangedEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedSlotMappingState;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedVisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.VisualItemValueResolverFactoryProvider;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.ui.VisualItemValueResolverUIController;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.ui.VisualItemValueResolverUIControllerFactory;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.ui.VisualItemValueResolverUIControllerFactoryProvider;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Widget;

public class DefaultVisualMappingsControl implements VisualMappingsControl {

    protected final HasResourceCategorizer resourceGrouping;

    protected final DefaultManagedSlotMappingConfiguration slotMappingConfigurationUIModel;

    protected final VisualItemValueResolverFactoryProvider resolverFactoryProvider;

    private ConfigurationPanel visualMappingPanel;

    protected ListBoxControl<String> groupingBox;

    private DataTypeLists<SlotControl> slotControlsByDataType;

    private Map<Slot, SlotControl> slotToSlotControls = new HashMap<Slot, SlotControl>();

    private final VisualItemValueResolverUIControllerFactoryProvider uiProvider;

    public static final String GROUP_BY_URI_LABEL = "No Grouping";

    public DefaultVisualMappingsControl(
            DefaultManagedSlotMappingConfiguration slotMappingConfigurationUIModel,
            HasResourceCategorizer resourceGrouping,
            VisualItemValueResolverUIControllerFactoryProvider uiProvider,
            VisualItemValueResolverFactoryProvider resolverFactoryProvider) {

        assert slotMappingConfigurationUIModel != null;
        assert resourceGrouping != null;
        assert uiProvider != null;

        this.slotMappingConfigurationUIModel = slotMappingConfigurationUIModel;

        this.resourceGrouping = resourceGrouping;
        this.uiProvider = uiProvider;
        this.resolverFactoryProvider = resolverFactoryProvider;
    }

    private void addSlotControl(Slot slot, SlotControl slotControl) {
        assert slotControl != null;
        assert slot != null;

        visualMappingPanel.setConfigurationSetting(slot.getName(),
                slotControl.asWidget());

        slotControlsByDataType.get(slot.getDataType()).add(slotControl);
        slotToSlotControls.put(slot, slotControl);
    }

    @Override
    public Widget asWidget() {
        if (visualMappingPanel == null) {
            init();
        }

        return visualMappingPanel;
    }

    protected List<String> calculateGroupingBoxOptions(
            LightweightCollection<VisualItem> visualItems) {

        List<String> options = new ArrayList<String>();

        for (String property : getProperties(visualItems, DataType.TEXT)) {
            options.add(property);
        }
        options.add(GROUP_BY_URI_LABEL);

        return options;
    }

    private SlotControl createSlotControl(Slot slot,
            ManagedVisualItemValueResolver resolver) {

        VisualItemValueResolverUIController newResolverUI = createUIControllerFromResolver(
                slot, resolver);

        DefaultSlotControl slotControl = new DefaultSlotControl(slot,
                slotMappingConfigurationUIModel, newResolverUI);

        this.slotToSlotControls.put(slot, slotControl);

        addSlotControl(slot, slotControl);

        return slotControl;
    }

    // TODO this should take allowableFactories as a parameter
    private VisualItemValueResolverUIController createUIControllerFromResolver(
            Slot slot, ManagedVisualItemValueResolver currentResolver) {

        VisualItemValueResolverUIControllerFactory uiFactory = uiProvider
                .get(currentResolver.getId());

        assert uiFactory != null;

        return uiFactory.create(
                slotMappingConfigurationUIModel.getManagedSlotMapping(slot),
                slotMappingConfigurationUIModel.getVisualItems());
    }

    private LightweightList<String> getProperties(
            LightweightCollection<VisualItem> visualItems, DataType dataType) {

        /*
         * XXX shouldn't this is use method in ResourceSetUtils that calculates
         * the properties that are valid accross all visual items or is this the
         * same?
         */
        ResourceSet resources = new DefaultResourceSet();
        for (VisualItem visualItem : visualItems) {
            resources.addAll(visualItem.getResources());
        }
        return ResourceSetUtils.getProperties(resources, dataType);
    }

    // TODO uh, shouldnt we just initialize the visualMappingSPanel in the
    // constructor, as well as the other two things.
    private void init() {
        visualMappingPanel = new ConfigurationPanel();

        initGroupingBox();
        initSlotControls();
    }

    protected void initGroupingBox() {
        // TODO include aggregation that does not aggregate...
        // TODO include bin aggregation for numerical slots

        groupingBox = new ListBoxControl<String>(new ExtendedListBox(false),
                new NullTransformer<String>());

        /**
         * This is an event handle which watches the resource grouping box for
         * grouping changes
         */
        groupingBox.setChangeHandler(new ChangeHandler() {
            // XXX This is a bad Hack.
            @Override
            public void onChange(ChangeEvent event) {
                String property = groupingBox.getSelectedValue();

                if (GROUP_BY_URI_LABEL.equals(property)) {
                    resourceGrouping
                            .setCategorizer(new ResourceByUriMultiCategorizer());
                } else {
                    resourceGrouping
                            .setCategorizer(new ResourceByPropertyMultiCategorizer(
                                    property));
                }
            }
        });

        visualMappingPanel.setConfigurationSetting("Grouping",
                groupingBox.asWidget());
    }

    private void initSlotControls() {
        slotControlsByDataType = new DataTypeLists<SlotControl>();
    }

    protected boolean shouldResetGrouping(
            LightweightCollection<VisualItem> visualItems) {
        return groupingBox.getSelectedValue() == null
                || !calculateGroupingBoxOptions(visualItems).contains(
                        groupingBox.getSelectedValue());
    }

    @Override
    public void updateConfigurationForSlotMappingChangedEvent(
            ManagedSlotMappingConfigurationChangedEvent e) {

        updateGroupingBox(e.getVisualItems());

        for (Entry<Slot, ManagedSlotMappingState> entry : e
                .getSlotConfigurationStates().entrySet()) {
            updateSlotUI(entry.getKey(), entry.getValue(), e.getVisualItems());
        }
    }

    protected void updateGroupingBox(
            LightweightCollection<VisualItem> visualItems) {
        groupingBox.setValues(calculateGroupingBoxOptions(visualItems));
        if (shouldResetGrouping(visualItems)) {

            if (resourceGrouping.getCategorizer() instanceof ResourceByPropertyMultiCategorizer) {

                String property = ((ResourceByPropertyMultiCategorizer) resourceGrouping
                        .getCategorizer()).getProperty();
                groupingBox.setSelectedValue(property);
            } else if (resourceGrouping.getCategorizer() instanceof ResourceByUriMultiCategorizer) {
                groupingBox.setSelectedValue(GROUP_BY_URI_LABEL);
            }
        }
    }

    private void updateSlotUI(Slot slot, ManagedSlotMappingState state,
            LightweightCollection<VisualItem> visualItems) {

        // TODO in the future, errors in these two things would likely be
        // handled, instead of exceptions being thrown
        assert state.isAllowable();
        assert state.isConfigured();

        ManagedVisualItemValueResolver resolver = state.getResolver();

        SlotControl slotControl = slotToSlotControls.get(slot);
        if (slotControl == null) {
            // The slot Control has not yet been initialized, initialize it and
            // set the resolverUI
            slotControl = createSlotControl(slot, resolver);
        } else if (!resolver.getId().equals(
                slotControl.getCurrentResolverUIId())) {
            // only the factory has changed, and we need to update the
            // resolverUI
            slotControl.setNewUIModel(createUIControllerFromResolver(slot,
                    resolver));
        }

        // update the slotMapping with the new options
        slotControl.updateOptions(visualItems);
    }

}