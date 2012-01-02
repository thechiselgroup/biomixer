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
package org.thechiselgroup.biomixer.client.core.visualization.model.extensions;

import org.thechiselgroup.biomixer.client.core.resources.CountingResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ProxyResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetDelegateChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.UnionResourceSet;

import com.google.gwt.event.shared.HandlerRegistration;

public class HighlightingModel {

    private ProxyResourceSet highlightedResourceSetContainer;

    private ResourceSet highlightedSingleResources;

    private UnionResourceSet combinedHighlightedResources;

    public HighlightingModel() {
        highlightedResourceSetContainer = new ProxyResourceSet();

        /*
         * We use a counting resource set, because elements might get removed
         * from the set after they have been added again, e.g. when moving the
         * mouse from over a resource item with popup to over a resource set and
         * the popup removes the resource a bit later.
         */
        highlightedSingleResources = new CountingResourceSet();

        combinedHighlightedResources = new UnionResourceSet(
                new DefaultResourceSet());
        combinedHighlightedResources
                .addResourceSet(highlightedResourceSetContainer);
        combinedHighlightedResources.addResourceSet(highlightedSingleResources);
    }

    public HandlerRegistration addEventHandler(
            ResourceSetChangedEventHandler handler) {
        return combinedHighlightedResources.addEventHandler(handler);
    }

    public HandlerRegistration addEventHandler(
            ResourceSetDelegateChangedEventHandler handler) {
        return highlightedResourceSetContainer.addEventHandler(handler);
    }

    public void addHighlightedResources(ResourceSet resource) {
        highlightedSingleResources.addAll(resource);
    }

    public ResourceSet getResources() {
        return combinedHighlightedResources;
    }

    public void removeHighlightedResources(ResourceSet resources) {
        highlightedSingleResources.removeAll(resources);
    }

    public void setHighlightedResourceSet(ResourceSet resourceSet) {
        highlightedResourceSetContainer.setDelegate(resourceSet);
    }

}