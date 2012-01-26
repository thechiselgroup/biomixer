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
package org.thechiselgroup.biomixer.client.core.visualization;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.HasResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceByUriMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.DisposeUtil;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualizationModel;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Facade that facilitates usage of {@link ViewContentDisplay} as {@link Widget}
 * .
 * 
 * @author Lars Grammel
 */
/*
 * XXX calls to setHeight, setWidth and setSize will fail to update view.
 */
public class VisualizationWidget<T extends ViewContentDisplay> extends
        SimplePanel implements HasResourceCategorizer {

    private final VisualizationModel viewModel;

    private final T contentDisplay;

    public VisualizationWidget(T contentDisplay, ResourceSet selectedResource,
            ResourceSet highlightedResources,
            VisualItemBehavior visualItemBehavior, ErrorHandler errorHandler) {

        assert contentDisplay != null;

        this.contentDisplay = contentDisplay;
        // XXX creating new DisposeUtil
        this.viewModel = new DefaultVisualizationModel(contentDisplay,
                selectedResource, highlightedResources, visualItemBehavior,
                errorHandler, new DefaultResourceSetFactory(),
                new ResourceByUriMultiCategorizer(), new DisposeUtil(
                        errorHandler));

        setWidget(contentDisplay.asWidget());
    }

    @Override
    public ResourceMultiCategorizer getCategorizer() {
        return viewModel.getCategorizer();
    }

    public T getContentDisplay() {
        return contentDisplay;
    }

    public ResourceSet getContentResourceSet() {
        return viewModel.getContentResourceSet();
    }

    @Override
    public void setCategorizer(ResourceMultiCategorizer newCategorizer) {
        viewModel.setCategorizer(newCategorizer);
    }

    public void setContentResourceSet(ResourceSet contentResourceSet) {
        viewModel.setContentResourceSet(contentResourceSet);
    }

    @Override
    public void setPixelSize(int width, int height) {
        assert width >= 0;
        assert height >= 0;

        super.setPixelSize(width, height);
        contentDisplay.setSize(width, height);
    }

    public void setPropertyValue(String property, Object value) {
        viewModel.getViewContentDisplay().setPropertyValue(property, value);
    }

    public void setResolver(Slot slot, VisualItemValueResolver resolver) {
        viewModel.setResolver(slot, resolver);
    }
}