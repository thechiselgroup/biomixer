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

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.predicates.Predicate;
import org.thechiselgroup.biomixer.client.core.util.predicates.TruePredicate;

/**
 * ResourceSet that uses a {@link Predicate}s to to filter its contents.
 */
// TODO this should be a read-only resource set...
public class FilteredResourceSet extends DelegatingResourceSet {

    private Predicate<Resource> filterPredicate = new TruePredicate<Resource>();

    private final ResourceSet sourceSet;

    /**
     * Instantiates a new filtered resource set.
     * 
     * @param sourceSet
     *            The resource set that should be filtered by this ResourceSet.
     * 
     * @param delegateSet
     *            Delegate in which the filtered resources are stored.
     */
    public FilteredResourceSet(ResourceSet sourceSet, ResourceSet delegateSet) {
        super(delegateSet);

        assert sourceSet != null;

        this.sourceSet = sourceSet;

        this.sourceSet.addEventHandler(new ResourceSetChangedEventHandler() {
            @Override
            public void onResourceSetChanged(ResourceSetChangedEvent event) {
                processEvent(event);
            }
        });
    }

    @Override
    public boolean add(Resource resource) {
        throw new UnsupportedOperationException(
                "FilteredResourceSet.add is not supported");
    }

    @Override
    public boolean addAll(Iterable<Resource> resources) {
        throw new UnsupportedOperationException(
                "FilteredResourceSet.addAll is not supported");
    }

    @Override
    public boolean change(Iterable<Resource> addedResources,
            Iterable<Resource> removedResources) {

        throw new UnsupportedOperationException(
                "FilteredResourceSet.change is not supported");
    }

    private LightweightList<Resource> filter(
            LightweightCollection<Resource> resources) {

        LightweightList<Resource> filteredResources = CollectionFactory
                .createLightweightList();
        for (Resource resource : resources) {
            if (filterPredicate.evaluate(resource)) {
                filteredResources.add(resource);
            }
        }
        return filteredResources;
    }

    public Predicate<Resource> getFilterPredicate() {
        return filterPredicate;
    }

    @Override
    public void invert(Resource resource) {
        throw new UnsupportedOperationException(
                "FilteredResourceSet.invert is not supported");
    }

    @Override
    public void invertAll(Iterable<Resource> resources) {
        throw new UnsupportedOperationException(
                "FilteredResourceSet.invertAll is not supported");
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    private void processEvent(ResourceSetChangedEvent event) {
        getDelegate().change(filter(event.getAddedResources()),
                filter(event.getRemovedResources()));
    }

    @Override
    public boolean remove(Resource resource) {
        throw new UnsupportedOperationException(
                "FilteredResourceSet.remove is not supported");
    }

    @Override
    public boolean removeAll(Iterable<Resource> resources) {
        throw new UnsupportedOperationException(
                "FilteredResourceSet.removeAll is not supported");
    }

    @Override
    public boolean retainAll(ResourceSet resources) {
        throw new UnsupportedOperationException(
                "FilteredResourceSet.retainAll is not supported");
    }

    public void setFilterPredicate(Predicate<Resource> newFilterPredicate) {
        assert newFilterPredicate != null;

        Predicate<Resource> currentFilterPredicate = filterPredicate;

        if (newFilterPredicate.equals(currentFilterPredicate)) {
            return;
        }

        LightweightList<Resource> resourcesToAdd = CollectionFactory
                .createLightweightList();
        LightweightList<Resource> resourcesToRemove = CollectionFactory
                .createLightweightList();

        for (Resource resource : sourceSet) {
            if (newFilterPredicate.evaluate(resource)
                    && !currentFilterPredicate.evaluate(resource)) {
                resourcesToAdd.add(resource);
            } else if (!newFilterPredicate.evaluate(resource)
                    && currentFilterPredicate.evaluate(resource)) {
                resourcesToRemove.add(resource);
            }
        }

        filterPredicate = newFilterPredicate;

        getDelegate().change(resourcesToAdd, resourcesToRemove);
    }

}