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

import java.util.NoSuchElementException;

import org.thechiselgroup.biomixer.client.core.label.DefaultHasLabel;
import org.thechiselgroup.biomixer.client.core.label.HasLabel;
import org.thechiselgroup.biomixer.client.core.label.LabelChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.collections.NullIterable;
import org.thechiselgroup.biomixer.client.core.util.collections.SingleItemCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.SingleItemIterable;
import org.thechiselgroup.biomixer.client.core.util.event.PrioritizedHandlerManager;
import org.thechiselgroup.biomixer.shared.core.util.ForTest;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;

public abstract class AbstractResourceSet implements ResourceSet {

    private transient PrioritizedHandlerManager handlerManager;

    private HasLabel labelDelegate;

    public AbstractResourceSet() {
        handlerManager = new PrioritizedHandlerManager(this);
        labelDelegate = new DefaultHasLabel(this);
    }

    @Override
    public boolean add(Resource resource) {
        assert resource != null;

        return addAll(new SingleItemCollection<Resource>(resource));
    }

    @Override
    public boolean addAll(Iterable<Resource> resources) {
        // // XXX This is pretty slow...can it be sped up in any way?
        return change(resources, NullIterable.<Resource> nullIterable());
    }

    @Override
    public HandlerRegistration addEventHandler(
            ResourceSetChangedEventHandler handler) {

        return handlerManager.addHandler(ResourceSetChangedEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addLabelChangedEventHandler(
            LabelChangedEventHandler eventHandler) {

        return labelDelegate.addLabelChangedEventHandler(eventHandler);
    }

    /**
     * Intersection calculation based on contains.
     * 
     * @param iteratedResources
     *            Resources that are iterated over. Usually this set should be
     *            smaller than the other one, unless its contains check is slow.
     * @param containmentCheckedResources
     *            Resources on which contains is called. Make sure contains is
     *            fast for this set.
     * 
     * @see #getIntersection(Iterable)
     */
    private LightweightList<Resource> calculateIntersection(
            Iterable<Resource> iteratedResources,
            LightweightCollection<Resource> containmentCheckedResources) {

        LightweightList<Resource> result = CollectionFactory
                .createLightweightList();

        for (Resource resource : iteratedResources) {
            if (containmentCheckedResources.contains(resource)) {
                result.add(resource);
            }
        }
        return result;
    }

    @Override
    public boolean change(Iterable<Resource> resourcesToAdd,
            Iterable<Resource> resourcesToRemove) {

        assert resourcesToAdd != null;
        assert resourcesToRemove != null;

        LightweightList<Resource> addedResources = CollectionFactory
                .createLightweightList();
        // XXX It seems to take much longer due to adding resources individually
        for (Resource resource : resourcesToAdd) {
            if (!contains(resource)) {
                doAdd(resource, addedResources);
            }
        }

        LightweightList<Resource> removedResources = CollectionFactory
                .createLightweightList();
        for (Resource resource : resourcesToRemove) {
            if (contains(resource)) {
                doRemove(resource, removedResources);
            }
        }

        boolean hasChanged = !addedResources.isEmpty()
                || !removedResources.isEmpty();

        if (hasChanged) {
            fireEvent(ResourceSetChangedEvent
                    .createResourcesAddedAndRemovedEvent(this, addedResources,
                            removedResources));
        }

        return hasChanged;
    }

    @Override
    public void clear() {
        removeAll(toList());
    }

    @Override
    public boolean contains(Resource resource) {
        assert resource != null;
        return containsResourceWithUri(resource.getUri());
    }

    @Override
    public boolean containsAll(Iterable<Resource> resources) {
        for (Resource resource : resources) {
            if (!contains(resource)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public final boolean containsEqualResources(ResourceSet other) {
        if (size() != other.size()) {
            return false;
        }

        return containsAll(other);
    }

    @Override
    public boolean containsNone(Iterable<Resource> resources) {
        for (Resource resource : resources) {
            if (contains(resource)) {
                return false;
            }
        }

        return true;
    }

    protected abstract void doAdd(Resource resource,
            LightweightList<Resource> addedResources);

    protected abstract void doRemove(Resource resource,
            LightweightList<Resource> removedResources);

    protected void fireEvent(ResourceSetChangedEvent event) {
        handlerManager.fireEvent(event);
    }

    @Override
    public Resource getFirstElement() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        // TODO optimize implementation
        return iterator().next();
    }

    @ForTest
    public int getHandlerCount(Type<?> type) {
        return handlerManager.getHandlerCount(type);
    }

    @Override
    public LightweightList<Resource> getIntersection(
            LightweightCollection<Resource> resources) {

        assert resources != null;

        // special case: one collection is empty
        if (isEmpty() || resources.isEmpty()) {
            return CollectionFactory.createLightweightList();
        }

        /*
         * special case: other collection is resource set. Resource sets are
         * assumed to be hash-based and to use a fast contains method, so we can
         * just iterate over the smaller set and check for containment.
         */
        if (size() < resources.size() && resources instanceof ResourceSet) {
            return calculateIntersection(this, resources);
        }

        return calculateIntersection(resources, this);
    }

    @Override
    public String getLabel() {
        return labelDelegate.getLabel();
    }

    @Override
    public boolean hasLabel() {
        return labelDelegate.hasLabel();
    }

    @Override
    public final void invert(Resource resource) {
        assert resource != null;

        invertAll(new SingleItemIterable<Resource>(resource));
    }

    /*
     * XXX what if resource is contained in several selected resource items? The
     * general selection model seems to be flawed. Need to create an issue
     * tracking item for this.
     */
    @Override
    public void invertAll(Iterable<Resource> resources) {
        // TODO fix: this fires several events
        assert resources != null;

        LightweightList<Resource> addedResources = CollectionFactory
                .createLightweightList();
        LightweightList<Resource> removedResources = CollectionFactory
                .createLightweightList();

        for (Resource resource : resources) {
            if (contains(resource)) {
                doRemove(resource, removedResources);
                assert !contains(resource);
            } else {
                doAdd(resource, addedResources);
                assert contains(resource);
            }
        }

        fireEvent(ResourceSetChangedEvent.createResourcesAddedAndRemovedEvent(
                this, addedResources, removedResources));
    }

    @Override
    public boolean isModifiable() {
        return true;
    }

    @Override
    public boolean remove(Resource resource) {
        assert resource != null;

        return removeAll(new SingleItemCollection<Resource>(resource));
    }

    @Override
    public boolean removeAll(Iterable<Resource> resources) {
        return change(NullIterable.<Resource> nullIterable(), resources);
    }

    // TODO implement faster retains if both are default resource sets
    @Override
    public boolean retainAll(ResourceSet resources) {
        assert resources != null;

        LightweightList<Resource> removedResources = CollectionFactory
                .createLightweightList();
        for (Resource resource : toList()) {
            if (!resources.contains(resource)) {
                doRemove(resource, removedResources);
            }
        }

        if (!removedResources.isEmpty()) {
            fireEvent(ResourceSetChangedEvent.createResourcesRemovedEvent(this,
                    removedResources));
        }

        return !removedResources.isEmpty();
    }

    @Override
    public void setLabel(String label) {
        labelDelegate.setLabel(label);
    }

}
