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

import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Information about a change in a {@link ResourceSet}. It contains the resource
 * that have been added and removed. There is no overlap in those resource sets.
 * 
 * <p>
 * Use the factory methods
 * {@link #createResourcesAddedAndRemovedEvent(ResourceSet, LightweightCollection, LightweightCollection)}
 * , {@link #createResourcesAddedEvent(ResourceSet, LightweightCollection)} and
 * {@link #createResourcesRemovedEvent(ResourceSet, LightweightCollection)} to
 * create events.
 * </p>
 * 
 * @author Lars Grammel
 * 
 * @see ResourceSet
 * @see ResourceSetChangedEventHandler
 */
public class ResourceSetChangedEvent extends
        GwtEvent<ResourceSetChangedEventHandler> {

    private final LightweightCollection<Resource> addedResources;

    private final LightweightCollection<Resource> removedResources;

    private final ResourceSet target;

    public final static GwtEvent.Type<ResourceSetChangedEventHandler> TYPE = new GwtEvent.Type<ResourceSetChangedEventHandler>();

    public static ResourceSetChangedEvent createResourcesAddedAndRemovedEvent(
            ResourceSet target, LightweightCollection<Resource> addedResources,
            LightweightCollection<Resource> removedResources) {

        return new ResourceSetChangedEvent(target, addedResources,
                removedResources);
    }

    public static ResourceSetChangedEvent createResourcesAddedEvent(
            ResourceSet target, LightweightCollection<Resource> addedResources) {

        return new ResourceSetChangedEvent(target, addedResources,
                LightweightCollections.<Resource> emptyCollection());
    }

    public static ResourceSetChangedEvent createResourcesRemovedEvent(
            ResourceSet target, LightweightCollection<Resource> removedResources) {

        return new ResourceSetChangedEvent(target,
                LightweightCollections.<Resource> emptyCollection(),
                removedResources);
    }

    /**
     * Use factory methods instead.
     * 
     * @see #createResourcesAddedEvent(ResourceSet, LightweightCollection)
     * @see #createResourcesRemovedEvent(ResourceSet, LightweightCollection)
     * @see #createResourcesAddedAndRemovedEvent(ResourceSet,
     *      LightweightCollection, LightweightCollection)
     */
    private ResourceSetChangedEvent(ResourceSet target,
            LightweightCollection<Resource> addedResources,
            LightweightCollection<Resource> removedResources) {

        assert target != null;
        assert addedResources != null;
        assert removedResources != null;
        assert addedResources.toList().removeAll(removedResources.toList()) == false : "added and removed resources must not overlap";

        this.target = target;
        this.addedResources = addedResources;
        this.removedResources = removedResources;
    }

    @Override
    protected void dispatch(ResourceSetChangedEventHandler handler) {
        handler.onResourceSetChanged(this);
    }

    /**
     * Returns resources that have been added to the target resource set.
     */
    public LightweightCollection<Resource> getAddedResources() {
        return addedResources;
    }

    @Override
    public GwtEvent.Type<ResourceSetChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * Returns resources that have been removed from the target resource set.
     */
    public LightweightCollection<Resource> getRemovedResources() {
        return removedResources;
    }

    /**
     * Returns the resource set that has changed.
     */
    public ResourceSet getTarget() {
        return target;
    }
}