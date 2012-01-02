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

import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceManager;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightCollection;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;

// TODO better separation of concern - introduce factories for the expanders
// TODO use resource items instead of single resources
// TODO split up interface
// TODO rename
public interface GraphNodeExpansionCallback {

    void addAutomaticResource(Resource resource);

    boolean containsResourceWithUri(String resourceUri);

    String getCategory(Resource resource);

    GraphDisplay getDisplay();

    Resource getResourceByUri(String value);

    /**
     * @deprecated {@link ResourceManager} should be injected instead.
     */
    @Deprecated
    ResourceManager getResourceManager();

    LightweightCollection<VisualItem> getVisualItems(
            Iterable<Resource> resources);

    boolean isInitialized();

    boolean isRestoring();

    /**
     * Updates the arcs of all view items that contain any of the resources.
     */
    void updateArcsForResources(Iterable<Resource> resources);

    /**
     * Updates the arc items and arcs for the given view items. The view items
     * must already be contained in the view content display (i.e. they have
     * been added already and their nodes must be visible).
     */
    void updateArcsForVisuaItems(LightweightCollection<VisualItem> visualItems);

}