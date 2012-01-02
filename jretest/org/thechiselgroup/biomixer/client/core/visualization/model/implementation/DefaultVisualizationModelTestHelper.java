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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceByUriTypeCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceCategorizerToMultiCategorizerAdapter;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.collections.NullIterator;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualizationModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.VisualItemValueResolverFactory;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.VisualItemValueResolverFactoryProvider;

import com.google.gwt.event.shared.HandlerRegistration;

// TODO refactor, change into object-based class
public final class DefaultVisualizationModelTestHelper {

    public static void stubHandlerRegistration(ResourceSet mockedResources,
            HandlerRegistration handlerRegistrationToReturn) {

        when(mockedResources.iterator()).thenReturn(
                NullIterator.<Resource> nullIterator());

        when(
                mockedResources
                        .addEventHandler(any(ResourceSetChangedEventHandler.class)))
                .thenReturn(handlerRegistrationToReturn);
    }

    private Slot[] slots = new Slot[0];

    private ResourceSet containedResources = new DefaultResourceSet();

    private ResourceSet highlightedResources = new DefaultResourceSet();

    private ResourceSet selectedResources = new DefaultResourceSet();

    private ViewContentDisplay viewContentDisplay = mock(ViewContentDisplay.class);

    private VisualItemValueResolverFactoryProvider resolverProvider = mock(VisualItemValueResolverFactoryProvider.class);

    private LightweightList<VisualItemValueResolverFactory> resolverFactories = CollectionFactory
            .createLightweightList();

    public boolean addToContainedResources(Resource resource) {
        return getContainedResources().add(resource);
    }

    public boolean addToContainedResources(ResourceSet resources) {
        return getContainedResources().addAll(resources);
    }

    public Slot[] createSlots(DataType... dataTypes) {
        assert dataTypes != null;

        Slot[] slots = VisualItemValueResolverTestUtils.createSlots(dataTypes);

        setSlots(slots);

        return slots;
    }

    public DefaultVisualizationModel createTestVisualizationModel() {
        when(resolverProvider.getAll()).thenReturn(
                resolverFactories);

        when(viewContentDisplay.getSlots()).thenReturn(slots);
        when(viewContentDisplay.isReady()).thenReturn(true);

        DefaultVisualizationModel visualizationModel = spy(new DefaultVisualizationModel(
                viewContentDisplay, selectedResources, highlightedResources,
                mock(VisualItemBehavior.class), new TestErrorHandler(),
                new DefaultResourceSetFactory(),
                new ResourceCategorizerToMultiCategorizerAdapter(
                        new ResourceByUriTypeCategorizer())));

        visualizationModel.setContentResourceSet(containedResources);
        return visualizationModel;
    }

    public ResourceSet getContainedResources() {
        return containedResources;
    }

    public ResourceSet getHighlightedResources() {
        return highlightedResources;
    }

    public LightweightList<VisualItemValueResolverFactory> getResolverFactories() {
        return resolverFactories;
    }

    public VisualItemValueResolverFactoryProvider getResolverProvider() {
        return resolverProvider;
    }

    public ResourceSet getSelectedResources() {
        return selectedResources;
    }

    public Slot[] getSlots() {
        return slots;
    }

    public ViewContentDisplay getViewContentDisplay() {
        return viewContentDisplay;
    }

    public void mockContainedResources() {
        this.containedResources = mock(ResourceSet.class);
    }

    public void mockHighlightedResources() {
        this.highlightedResources = mock(ResourceSet.class);
    }

    public void mockSelectedResources() {
        this.selectedResources = mock(ResourceSet.class);
    }

    public void setContainedResources(ResourceSet containedResources) {
        this.containedResources = containedResources;
    }

    public void setHighlightedResources(ResourceSet highlightedResources) {
        this.highlightedResources = highlightedResources;
    }

    public void setSelectedResources(ResourceSet selectedResources) {
        this.selectedResources = selectedResources;
    }

    public void setSlots(Slot... slots) {
        this.slots = slots;
    }

}