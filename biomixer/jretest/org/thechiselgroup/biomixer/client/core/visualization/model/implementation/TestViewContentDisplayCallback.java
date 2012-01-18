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
package org.thechiselgroup.biomixer.client.core.visualization.model.implementation;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplayCallback;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;

import com.google.gwt.event.shared.HandlerRegistration;

// TODO split...
// TODO extract resource item manager?
public class TestViewContentDisplayCallback implements
        ViewContentDisplayCallback, VisualItemContainer {

    private Map<String, VisualItem> visualItemsByGroupId = CollectionFactory
            .createStringMap();

    @Override
    public HandlerRegistration addHandler(
            VisualItemContainerChangeEventHandler handler) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addVisualItem(VisualItem visualItem) {
        visualItemsByGroupId.put(visualItem.getId(), visualItem);
    }

    public void addVisualItems(Iterable<VisualItem> visualItems) {
        for (VisualItem visualItem : visualItems) {
            addVisualItem(visualItem);
        }
    }

    @Override
    public boolean containsVisualItem(String visualItemId) {
        return visualItemsByGroupId.containsKey(visualItemId);
    }

    @Override
    public VisualItemValueResolver getResolver(Slot slot) {
        return null;
    }

    @Override
    public String getSlotResolverDescription(Slot slot) {
        return null;
    }

    @Override
    public VisualItem getVisualItem(String groupId) {
        return visualItemsByGroupId.get(groupId);
    }

    @Override
    public LightweightCollection<VisualItem> getVisualItems() {
        LightweightList<VisualItem> result = CollectionFactory
                .createLightweightList();
        result.addAll(visualItemsByGroupId.values());
        return result;
    }

    @Override
    public LightweightCollection<VisualItem> getVisualItems(
            Iterable<Resource> resources) {

        return null;
    }

    public void removeResourceItem(VisualItem visualItem) {
        visualItemsByGroupId.remove(visualItem.getId());
    }

}