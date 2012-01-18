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
package org.thechiselgroup.biomixer.client.core.visualization.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.ExtendedListBox;
import org.thechiselgroup.biomixer.client.core.ui.widget.listbox.ListBoxControl;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.DefaultManagedSlotMappingConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.VisualItemValueResolverFactory;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.ui.VisualItemValueResolverUIController;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

//Slot name is [________] + [uiControllerWidget]
public class DefaultSlotControl extends SlotControl {

    private VisualItemValueResolverUIController uiController;

    private ListBoxControl<VisualItemValueResolverFactory> resolverFactorySelector;

    private ChangeHandler factorySelectedChangeHandler;

    private VerticalPanel panel;

    private Widget currentUIControllerWidget;

    // TODO I don't like how I need this here just to get the visualItems. I feel
    // like this class should not know about view items. It could always keep
    // track of updates, but then it is possible to introduce inconsistencies
    private DefaultManagedSlotMappingConfiguration configuration;

    public DefaultSlotControl(Slot slot,
            DefaultManagedSlotMappingConfiguration configurationUIModel,
            VisualItemValueResolverUIController uiController) {

        super(slot);
        this.uiController = uiController;
        this.configuration = configurationUIModel;

        panel = new VerticalPanel();

        /**
         * Only responsible for changing the selected resolver, the change in UI
         * will handled elsewhere
         */
        factorySelectedChangeHandler = new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                VisualItemValueResolverFactory resolverFactory = resolverFactorySelector
                        .getSelectedValue();

                LightweightCollection<VisualItem> visualItems = configuration
                        .getVisualItems();

                configuration.getManagedSlotMapping(getSlot()).setResolver(
                        resolverFactory.create(visualItems));

                updateOptions(visualItems);
            }
        };

        resolverFactorySelector = new ListBoxControl<VisualItemValueResolverFactory>(
                new ExtendedListBox(false),
                new Transformer<VisualItemValueResolverFactory, String>() {
                    @Override
                    public String transform(
                            VisualItemValueResolverFactory factory) {
                        return factory.getLabel();
                    }
                });
    }

    // TODO add special UI stuff for resolvers that are in error
    @Override
    public Widget asWidget() {
        // updateFactorySelector();

        Label slotNameLabel = new Label(getSlot().getName() + " is ");

        Widget factorySelectorWidget = resolverFactorySelector.asWidget();

        currentUIControllerWidget = uiController.asWidget();

        assert factorySelectorWidget != null;
        assert currentUIControllerWidget != null;

        panel.add(slotNameLabel);
        panel.add(factorySelectorWidget);
        panel.add(currentUIControllerWidget);

        return panel;
    }

    @Override
    public String getCurrentResolverUIId() {
        return uiController.getId();
    }

    private List<VisualItemValueResolverFactory> getFactoryList() {
        List<VisualItemValueResolverFactory> result = new ArrayList<VisualItemValueResolverFactory>();
        Collection<VisualItemValueResolverFactory> allowableResolverFactories = configuration
                .getManagedSlotMapping(getSlot())
                .getAllowableResolverFactories();

        for (VisualItemValueResolverFactory factrory : allowableResolverFactories) {
            result.add(factrory);
        }

        return result;
    }

    @Override
    public void setNewUIModel(VisualItemValueResolverUIController resolverUI) {
        this.uiController = resolverUI;
        panel.remove(currentUIControllerWidget);
        currentUIControllerWidget = uiController.asWidget();
        panel.add(currentUIControllerWidget);
    }

    private void updateFactorySelector(
            LightweightCollection<VisualItem> visualItems) {
        // TODO there is a mem bug here because we do not remove the old handler
        resolverFactorySelector.setChangeHandler(factorySelectedChangeHandler);

        resolverFactorySelector.setValues(getFactoryList());

        // XXX what if the current factory becomes unavailable?
        VisualItemValueResolverFactory currentFactory = configuration
                .getManagedSlotMapping(getSlot()).getCurrentFactory();

        assert currentFactory.canCreateApplicableResolver(getSlot(),
                visualItems);

        resolverFactorySelector.setSelectedValue(currentFactory);
    }

    @Override
    public void updateOptions(LightweightCollection<VisualItem> visualItems) {
        updateFactorySelector(visualItems);
        uiController.update(visualItems);
    }
}
