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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.visualization_component.matrix.ViewWithResourceManager;
import org.thechiselgroup.biomixer.client.visualization_component.matrix.ViewWithResourceManager.SpecializedResourceManager;

// TODO better separation of concern - introduce factories for the expanders
// TODO use resource items instead of single resources
// TODO split up interface (did that some, by adding the SpecializedResourceManager, but still wanted exposure of that class's methods...)
// TODO rename
abstract public class NodeExpansionCallback<T extends ViewWithResourceManager> {

    abstract public T getDisplay();

    SpecializedResourceManager getSpecificResourceManager() {
        return this.getDisplay().getSpecificResourceManager();
    }

    abstract public LightweightCollection<VisualItem> getVisualItems(
            Iterable<Resource> resources);

    abstract public boolean isInitialized();

    abstract public boolean isRestoring();

    /**
     * Updates the arcs of all view items that contain any of the resources.
     */
    abstract public void updateArcsForResources(Iterable<Resource> resources);

    /**
     * Updates the arc items and arcs for the given view items. The view items
     * must already be contained in the view content display (i.e. they have
     * been added already and their nodes must be visible).
     */
    abstract public void updateArcsForVisuaItems(
            LightweightCollection<VisualItem> visualItems);

    // Convenience methods below:

    public void addAutomaticResource(Resource resource) {
        this.getSpecificResourceManager().addAutomaticResource(resource);
    }

    public boolean containsResourceWithUri(String resourceUri) {
        return this.getSpecificResourceManager().containsResourceWithUri(
                resourceUri);
    }

    public String getCategory(Resource resource) {
        return this.getSpecificResourceManager().getCategory(resource);
    }

    public Resource getResourceByUri(String value) {
        return this.getSpecificResourceManager().getResourceByUri(value);
    }

    /**
     * @deprecated {@link ResourceManager} should be injected instead.
     */
    @Deprecated
    public ResourceManager getResourceManager() {
        return this.getSpecificResourceManager().getResourceManager();
    }

}