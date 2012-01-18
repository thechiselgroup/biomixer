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
package org.thechiselgroup.biomixer.client.core.resources;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

// TODO add memento stuff (maybe external mix-in)
// TODO delegate to this from DefaultSelectionModel
public class ResourceSetContainer {

    private List<ResourceSet> resourceSets = new ArrayList<ResourceSet>();

    protected transient HandlerManager eventBus;

    @Inject
    public ResourceSetContainer() {
        eventBus = new HandlerManager(this);
    }

    public HandlerRegistration addEventHandler(
            ResourceSetAddedEventHandler handler) {
        return eventBus.addHandler(ResourceSetAddedEvent.TYPE, handler);
    }

    public HandlerRegistration addEventHandler(
            ResourceSetRemovedEventHandler handler) {
        return eventBus.addHandler(ResourceSetRemovedEvent.TYPE, handler);
    }

    public void addResourceSet(ResourceSet resourceSet) {
        assert resourceSet != null;
        assert !resourceSets.contains(resourceSet);

        resourceSets.add(resourceSet);
        eventBus.fireEvent(new ResourceSetAddedEvent(resourceSet));
    }

    public void clear() {
        ArrayList<ResourceSet> copy = new ArrayList<ResourceSet>(resourceSets);
        for (ResourceSet resourceSet : copy) {
            removeResourceSet(resourceSet);
        }
    }

    public boolean containsSelectionSet(ResourceSet resourceSet) {
        return resourceSets.contains(resourceSet);
    }

    public List<ResourceSet> getResourceSets() {
        return resourceSets;
    }

    public void removeResourceSet(ResourceSet resourceSet) {
        assert resourceSet != null;
        assert resourceSets.contains(resourceSet);

        resourceSets.remove(resourceSet);
        eventBus.fireEvent(new ResourceSetRemovedEvent(resourceSet));
    }

}
