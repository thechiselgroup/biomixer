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

import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * <p>
 * Resource set that delegates most operations to a delegate resource set. The
 * event handlers, however, are managed by the switching resource set. The
 * delegate of the resource set can be changed. If this happens, appropriate
 * ResourcesAddedEvents and ResourcesRemovedEvents events are generated that
 * reflect the changes in the contained resources because of the delegate set
 * exchange.
 * </p>
 * <p>
 * Labeling is ignored here and in fact just delegated.
 * </p>
 * 
 * @author Lars Grammel
 */
// TODO separate labeling concept from resource set
public class ProxyResourceSet extends DelegatingResourceSet implements
        Disposable, ContainsSingleResourceSet {

    protected transient HandlerManager eventBus;

    private ResourceSetChangedEventHandler delegateResourcesChangedHandler = new ResourceSetChangedEventHandler() {
        @Override
        public void onResourceSetChanged(ResourceSetChangedEvent event) {
            forwardEvent(event);
        }
    };

    private HandlerRegistration delegateResourcesChangedHandlerRegistration;

    public ProxyResourceSet() {
        this(NullResourceSet.NULL_RESOURCE_SET);
    }

    public ProxyResourceSet(ResourceSet delegate) {
        super(delegate);

        eventBus = new HandlerManager(this);

        addEventHandlersToDelegate();
    }

    @Override
    public HandlerRegistration addEventHandler(
            ResourceSetChangedEventHandler handler) {

        assert handler != null;
        return eventBus.addHandler(ResourceSetChangedEvent.TYPE, handler);
    }

    public HandlerRegistration addEventHandler(
            ResourceSetDelegateChangedEventHandler handler) {

        assert handler != null;
        return eventBus.addHandler(ResourceSetDelegateChangedEvent.TYPE,
                handler);
    }

    private void addEventHandlersToDelegate() {
        delegateResourcesChangedHandlerRegistration = delegate
                .addEventHandler(delegateResourcesChangedHandler);
    }

    @Override
    public void dispose() {
        removeEventHandlersFromDelegate();
    }

    private void fireDelegateChanged(ResourceSet newDelegate) {
        eventBus.fireEvent(new ResourceSetDelegateChangedEvent(newDelegate));
    }

    private void fireResourceChanges(ResourceSet newDelegate,
            ResourceSet oldDelegate) {

        /*
         * the add event is fire before the remove event such that the
         * intermediate state does not include an empty set in some cases.
         */

        assert oldDelegate != null;
        assert newDelegate != null;

        LightweightList<Resource> addedResources = CollectionFactory
                .createLightweightList();
        for (Resource resource : newDelegate) {
            if (!oldDelegate.contains(resource)) {
                addedResources.add(resource);
            }
        }

        LightweightList<Resource> removedResources = CollectionFactory
                .createLightweightList();
        for (Resource resource : oldDelegate) {
            if (!newDelegate.contains(resource)) {
                removedResources.add(resource);
            }
        }

        if (!removedResources.isEmpty() || !addedResources.isEmpty()) {
            fireResourcesChanged(addedResources, removedResources);
        }
    }

    private void fireResourcesChanged(
            LightweightCollection<Resource> addedResources,
            LightweightCollection<Resource> removedResources) {

        eventBus.fireEvent(ResourceSetChangedEvent
                .createResourcesAddedAndRemovedEvent(this, addedResources,
                        removedResources));
    }

    protected void forwardEvent(ResourceSetChangedEvent e) {
        fireResourcesChanged(e.getAddedResources(), e.getRemovedResources());
    }

    @Override
    public ResourceSet getResourceSet() {
        return getDelegate();
    }

    public boolean hasDelegate() {
        return !NullResourceSet.isNullResourceSet(delegate);
    }

    private void removeEventHandlersFromDelegate() {
        if (hasDelegate()) {
            delegateResourcesChangedHandlerRegistration.removeHandler();
        }

        delegateResourcesChangedHandlerRegistration = null;
    }

    public void setDelegate(ResourceSet newDelegate) {
        if (newDelegate == null) {
            newDelegate = NullResourceSet.NULL_RESOURCE_SET;
        }

        if (newDelegate == delegate) {
            return;
        }

        removeEventHandlersFromDelegate();

        ResourceSet oldDelegate = delegate;
        delegate = newDelegate;

        fireDelegateChanged(newDelegate);
        fireResourceChanges(newDelegate, oldDelegate);

        addEventHandlersToDelegate();
    }

    @Override
    public void setResourceSet(ResourceSet newResourceSet) {
        setDelegate(newResourceSet);
    }
}