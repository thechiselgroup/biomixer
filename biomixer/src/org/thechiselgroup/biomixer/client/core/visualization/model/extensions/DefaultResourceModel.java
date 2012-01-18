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

import java.util.List;

import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.Persistable;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.FilteredResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.UnionResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.predicates.Predicate;

public class DefaultResourceModel implements ResourceModel, Disposable,
        Persistable {

    private static final String MEMENTO_FILTER_PREDICATE = "filterPredicate";

    private UnionResourceSet allResources;

    private ResourceSet automaticResources;

    private UnionResourceSet combinedUserResourceSets;

    private FilteredResourceSet filteredResources;

    static final String MEMENTO_RESOURCE_SET_COUNT = "resourceSetCount";

    static final String MEMENTO_RESOURCE_SET_PREFIX = "resourceSet-";

    static final String MEMENTO_AUTOMATIC_RESOURCES = "automaticResources";

    private ResourceSetFactory resourceSetFactory;

    public DefaultResourceModel(ResourceSetFactory resourceSetFactory) {
        assert resourceSetFactory != null;

        this.resourceSetFactory = resourceSetFactory;

        initResourceCombinator();
        initAutomaticResources();
        initAllResources();
        initFilteredResources();
    }

    @Override
    public void addResourceSet(ResourceSet resourceSet) {
        if (!resourceSet.hasLabel()) {
            automaticResources.addAll(resourceSet);
        } else {
            combinedUserResourceSets.addResourceSet(resourceSet);
        }
    }

    @Override
    public void addUnnamedResources(Iterable<Resource> resources) {
        assert resources != null;
        automaticResources.addAll(resources);
    }

    @Override
    public void clear() {
        automaticResources.clear();
        combinedUserResourceSets.clear();
    }

    @Override
    public boolean containsResources(Iterable<Resource> resources) {
        assert resources != null;
        return allResources.containsAll(resources);
    }

    @Override
    public boolean containsResourceSet(ResourceSet resourceSet) {
        assert resourceSet != null;
        assert resourceSet.hasLabel() : resourceSet.toString()
                + " has no label";

        return combinedUserResourceSets.containsResourceSet(resourceSet);
    }

    @Override
    public void dispose() {
        combinedUserResourceSets = null;
    }

    @Override
    public ResourceSet getAutomaticResourceSet() {
        return automaticResources;
    }

    @Override
    public UnionResourceSet getCombinedUserResourceSets() {
        return combinedUserResourceSets;
    }

    @Override
    public LightweightList<Resource> getIntersection(
            LightweightCollection<Resource> resources) {

        assert resources != null;
        return allResources.getIntersection(resources);
    }

    @Override
    public ResourceSet getResources() {
        return filteredResources;
    }

    private void initAllResources() {
        allResources = new UnionResourceSet(
                resourceSetFactory.createResourceSet());
        allResources.addResourceSet(automaticResources);
        allResources.addResourceSet(combinedUserResourceSets);
    }

    private void initAutomaticResources() {
        automaticResources = resourceSetFactory.createResourceSet();
    }

    private void initFilteredResources() {
        filteredResources = new FilteredResourceSet(allResources,
                resourceSetFactory.createResourceSet());
        filteredResources.setLabel("All"); // TODO add & update view name
    }

    private void initResourceCombinator() {
        combinedUserResourceSets = new UnionResourceSet(
                resourceSetFactory.createResourceSet());
    }

    @Override
    public void removeResourceSet(ResourceSet resourceSet) {
        assert resourceSet != null;
        assert resourceSet.hasLabel();

        combinedUserResourceSets.removeResourceSet(resourceSet);
    }

    @Override
    public void removeUnnamedResources(Iterable<Resource> resources) {
        assert resources != null;
        automaticResources.removeAll(resources);
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        restoreFilter(state, restorationService, accessor);

        // TODO remove user sets, automatic resources
        addUnnamedResources(restoreAutomaticResources(state, accessor));
        restoreUserResourceSets(state, accessor);
    }

    private ResourceSet restoreAutomaticResources(Memento state,
            ResourceSetAccessor accessor) {
        return restoreResourceSet(state, accessor, MEMENTO_AUTOMATIC_RESOURCES);
    }

    private void restoreFilter(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {
        Memento child = state.getChild(MEMENTO_FILTER_PREDICATE);
        if (child != null) {
            Predicate<Resource> filterPredicate = (Predicate<Resource>) restorationService
                    .restoreFromMemento(child, accessor);
            filteredResources.setFilterPredicate(filterPredicate);
        }
    }

    private ResourceSet restoreResourceSet(Memento state,
            ResourceSetAccessor accessor, String key) {
        int id = (Integer) state.getValue(key);
        ResourceSet resourceSet = accessor.getResourceSet(id);
        return resourceSet;
    }

    private void restoreUserResourceSets(Memento state,
            ResourceSetAccessor accessor) {

        int resourceSetCount = (Integer) state
                .getValue(MEMENTO_RESOURCE_SET_COUNT);
        for (int i = 0; i < resourceSetCount; i++) {
            addResourceSet(restoreResourceSet(state, accessor,
                    MEMENTO_RESOURCE_SET_PREFIX + i));
        }
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        Memento memento = new Memento();

        storeFilterPredicate(resourceSetCollector, memento);
        storeAutomaticResources(resourceSetCollector, memento);
        storeUserResourceSets(resourceSetCollector, memento);

        return memento;
    }

    @Override
    public void setFilterPredicate(Predicate<Resource> filterPredicate) {
        filteredResources.setFilterPredicate(filterPredicate);
    }

    private void storeAutomaticResources(
            ResourceSetCollector persistanceManager, Memento memento) {

        storeResourceSet(persistanceManager, memento,
                MEMENTO_AUTOMATIC_RESOURCES, automaticResources);
    }

    private void storeFilterPredicate(
            ResourceSetCollector resourceSetCollector, Memento memento) {

        Predicate<Resource> filterPredicate = filteredResources
                .getFilterPredicate();
        if (filterPredicate instanceof Persistable) {
            Memento save = ((Persistable) filterPredicate)
                    .save(resourceSetCollector);
            memento.addChild(MEMENTO_FILTER_PREDICATE, save);
        }
    }

    private void storeResourceSet(ResourceSetCollector persistanceManager,
            Memento memento, String key, ResourceSet resources) {
        memento.setValue(key, persistanceManager.storeResourceSet(resources));
    }

    private void storeUserResourceSets(ResourceSetCollector persistanceManager,
            Memento memento) {

        List<ResourceSet> resourceSets = combinedUserResourceSets
                .getResourceSets();
        memento.setValue(MEMENTO_RESOURCE_SET_COUNT, resourceSets.size());
        for (int i = 0; i < resourceSets.size(); i++) {
            storeResourceSet(persistanceManager, memento,
                    MEMENTO_RESOURCE_SET_PREFIX + i, resourceSets.get(i));
        }
    }

}
