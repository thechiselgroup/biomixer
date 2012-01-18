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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

/**
 * Groups the {@link Resource}s contained in a {@link ResourceSet} which is set
 * via {@link ContainsSingleResourceSet}. A {@link ResourceMultiCategorizer} is
 * used for grouping and can be changed via {@link HasResourceCategorizer}.
 * 
 * @author Lars Grammel
 */
public class ResourceGrouping implements HasResourceCategorizer,
        ContainsSingleResourceSet, Disposable {

    private static class ResourcesCategorization {
        public Map<String, LightweightList<Resource>> resourceMapping;

        public LightweightList<Resource> uncategorizableResources;

        public ResourcesCategorization(
                Map<String, LightweightList<Resource>> categorizedResourceMapping,
                LightweightList<Resource> uncategorizedResources) {
            this.resourceMapping = categorizedResourceMapping;
            this.uncategorizableResources = uncategorizedResources;
        }
    }

    private Map<String, ResourceSet> groupedResources = CollectionFactory
            .createStringMap();

    private transient HandlerManager eventBus;

    private ResourceMultiCategorizer multiCategorizer;

    private final ResourceSetFactory resourceSetFactory;

    private ProxyResourceSet allResources;

    private ResourceSet uncategorizableResources;

    private Map<String, Set<String>> resourceIdToGroups = CollectionFactory
            .createStringMap();

    @Inject
    public ResourceGrouping(ResourceMultiCategorizer multiCategorizer,
            ResourceSetFactory resourceSetFactory) {

        this.multiCategorizer = multiCategorizer;
        this.resourceSetFactory = resourceSetFactory;
        this.uncategorizableResources = resourceSetFactory.createResourceSet();
        this.allResources = new ProxyResourceSet(
                resourceSetFactory.createResourceSet());

        eventBus = new HandlerManager(this);

        allResources.addEventHandler(new ResourceSetChangedEventHandler() {
            @Override
            public void onResourceSetChanged(ResourceSetChangedEvent event) {
                change(event.getAddedResources(), event.getRemovedResources());
            }
        });
    }

    /**
     * Adds the group to its resources for reverse lookup.
     * 
     * @see #resourceIdToGroups
     */
    private void addGroupToResources(String group,
            LightweightList<Resource> addedGroupResources) {

        for (Resource resource : addedGroupResources) {
            String resourceId = resource.getUri();
            Set<String> resourceGroups;
            if (!resourceIdToGroups.containsKey(resourceId)) {
                resourceGroups = CollectionFactory.createStringSet();
                resourceIdToGroups.put(resourceId, resourceGroups);
            } else {
                resourceGroups = resourceIdToGroups.get(resourceId);
            }

            resourceGroups.add(group);
        }
    }

    public HandlerRegistration addHandler(ResourceGroupingChangedHandler handler) {
        assert handler != null;
        return eventBus.addHandler(ResourceGroupingChangedEvent.TYPE, handler);
    }

    private void addResourcesToGroup(String group,
            LightweightList<Resource> addedResources,
            LightweightList<CategorizableResourceGroupingChange> changes) {

        assert group != null;
        assert addedResources != null;
        assert changes != null;

        if (containsGroup(group)) {
            ResourceSet groupResources = groupedResources.get(group);
            groupResources.addAll(addedResources);

            changes.add(CategorizableResourceGroupingChange
                    .newGroupChangedDelta(group, groupResources,
                            addedResources, null));
        } else {
            ResourceSet groupResources = resourceSetFactory.createResourceSet();
            groupResources.addAll(addedResources);

            groupedResources.put(group, groupResources);

            changes.add(CategorizableResourceGroupingChange
                    .newGroupCreatedDelta(group, groupResources, addedResources));
        }
    }

    private void addResourcesToGrouping(Iterable<Resource> resources,
            LightweightList<CategorizableResourceGroupingChange> changes,
            LightweightList<Resource> uncategorizableResources) {

        ResourcesCategorization categorization = categorize(resources);
        Map<String, LightweightList<Resource>> resourcesPerCategory = categorization.resourceMapping;
        uncategorizableResources
                .addAll(categorization.uncategorizableResources);

        this.uncategorizableResources.addAll(uncategorizableResources);

        for (Map.Entry<String, LightweightList<Resource>> entry : resourcesPerCategory
                .entrySet()) {

            String group = entry.getKey();
            LightweightList<Resource> addedGroupResources = entry.getValue();

            addResourcesToGroup(group, addedGroupResources, changes);
            addGroupToResources(group, addedGroupResources);
        }
    }

    private ResourcesCategorization categorize(Iterable<Resource> resources) {

        Map<String, LightweightList<Resource>> resourcesPerCategory = CollectionFactory
                .createStringMap();

        LightweightList<Resource> uncategorizableResources = CollectionFactory
                .createLightweightList();

        for (Resource resource : resources) {
            if (!multiCategorizer.canCategorize(resource)) {
                uncategorizableResources.add(resource);
                continue;
            }

            Set<String> categories = multiCategorizer.getCategories(resource);
            for (String category : categories) {
                assert category != null;

                if (!resourcesPerCategory.containsKey(category)) {
                    resourcesPerCategory.put(category, CollectionFactory
                            .<Resource> createLightweightList());
                }
                resourcesPerCategory.get(category).add(resource);
            }
        }
        return new ResourcesCategorization(resourcesPerCategory,
                uncategorizableResources);
    }

    protected void change(LightweightCollection<Resource> addedResources,
            LightweightCollection<Resource> removedResources) {

        assert addedResources != null;
        assert allResources.containsAll(addedResources);
        assert removedResources != null;
        assert CollectionUtils.containsNone(allResources.toList(),
                removedResources.toList());

        LightweightList<CategorizableResourceGroupingChange> changes = CollectionFactory
                .createLightweightList();

        LightweightList<Resource> addedUncategorizableResources = CollectionFactory
                .createLightweightList();
        LightweightList<Resource> removedUncategorizableResources = CollectionFactory
                .createLightweightList();

        removeResourcesFromGrouping(removedResources, changes,
                removedUncategorizableResources);
        addResourcesToGrouping(addedResources, changes,
                addedUncategorizableResources);

        UncategorizableResourceGroupingChange uncategorizableResourceChange = UncategorizableResourceGroupingChange
                .uncategorizedResourceChange(uncategorizableResources,
                        addedUncategorizableResources,
                        removedUncategorizableResources);

        fireChanges(changes, uncategorizableResourceChange);
    }

    /**
     * Clears the internal grouping structure. The grouping should be
     * recalculated after clearing to maintain a consistent state.
     */
    private void clearGrouping(
            LightweightList<CategorizableResourceGroupingChange> changes,
            LightweightList<Resource> removedResources) {
        for (Entry<String, ResourceSet> entry : groupedResources.entrySet()) {
            changes.add(CategorizableResourceGroupingChange
                    .newGroupRemovedDelta(entry.getKey(), entry.getValue(),
                            entry.getValue()));
        }
        // add the resources being cleared from the set to the
        removedResources.addAll(uncategorizableResources);

        uncategorizableResources.clear();
        groupedResources.clear();
        resourceIdToGroups.clear();
    }

    public boolean containsGroup(String groupId) {
        return groupedResources.containsKey(groupId);
    }

    @Override
    public void dispose() {
        allResources.dispose();
        allResources = null;
    }

    private void fireChanges(
            LightweightList<CategorizableResourceGroupingChange> changes,
            UncategorizableResourceGroupingChange uncategorizableChanges) {

        if (!changes.isEmpty() || uncategorizableChanges.hasActualChanges()) {

            eventBus.fireEvent(new ResourceGroupingChangedEvent(changes,
                    uncategorizableChanges));
        }
    }

    public Map<String, ResourceSet> getCategorizedResourceSets() {
        return new HashMap<String, ResourceSet>(groupedResources);
    }

    @Override
    public ResourceMultiCategorizer getCategorizer() {
        return multiCategorizer;
    }

    /**
     * Returns the resource group ids for the resource groups that contain at
     * least one of the resources.
     * 
     * @return resource group ids
     */
    public Set<String> getGroupIds(Iterable<Resource> resources) {
        assert resources != null;

        Set<String> result = CollectionFactory.createStringSet();
        for (Resource resource : resources) {
            Set<String> groups = resourceIdToGroups.get(resource.getUri());

            // groups are null if resource is not contained
            if (groups != null) {
                result.addAll(groups);
            }
        }
        return result;
    }

    public ResourceSet getGroupSet(String groupId) {
        assert containsGroup(groupId);
        return groupedResources.get(groupId);
    }

    @Override
    public ResourceSet getResourceSet() {
        return allResources.getResourceSet();
    }

    public ResourceSet getUncategorizableResources() {
        return uncategorizableResources;
    }

    /**
     * Removes the group from the resources for reverse lookup.
     * 
     * @see #resourceIdToGroups
     */
    private void removeGroupFromResources(String group,
            LightweightList<Resource> removedGroupResources) {

        for (Resource resource : removedGroupResources) {
            String resourceId = resource.getUri();
            Set<String> resourceGroups = resourceIdToGroups.get(resourceId);
            resourceGroups.remove(group);
            if (resourceGroups.isEmpty()) {
                resourceIdToGroups.remove(resourceId);
            }
        }
    }

    private void removeResourcesFromGroup(String group,
            LightweightList<Resource> removedResources,
            LightweightList<CategorizableResourceGroupingChange> changes) {

        ResourceSet groupResources = groupedResources.get(group);

        if (groupResources.size() == removedResources.size()
                && groupResources.containsAll(removedResources)) {

            groupedResources.remove(group);
            changes.add(CategorizableResourceGroupingChange
                    .newGroupRemovedDelta(group, groupResources,
                            removedResources));
        } else {
            groupResources.removeAll(removedResources);
            changes.add(CategorizableResourceGroupingChange
                    .newGroupChangedDelta(group, groupResources, null,
                            removedResources));
        }
    }

    private void removeResourcesFromGrouping(Iterable<Resource> resources,
            LightweightList<CategorizableResourceGroupingChange> changes,
            LightweightList<Resource> uncategorizableResources) {

        ResourcesCategorization categorization = categorize(resources);
        Map<String, LightweightList<Resource>> resourcesPerCategory = categorization.resourceMapping;
        uncategorizableResources
                .addAll(categorization.uncategorizableResources);

        this.uncategorizableResources.removeAll(uncategorizableResources);

        for (Map.Entry<String, LightweightList<Resource>> entry : resourcesPerCategory
                .entrySet()) {

            String group = entry.getKey();
            LightweightList<Resource> removedGroupResources = entry.getValue();

            removeResourcesFromGroup(group, removedGroupResources, changes);
            removeGroupFromResources(group, removedGroupResources);
        }
    }

    /**
     * Sets a new resource categorizer. Changing the categorizer causes the
     * whole grouping to be recalculated and triggers an event containining the
     * resulting changes.
     */
    // TODO is there a bug here in that resources could get removed and then
    // regrouped to where they were before
    // thus having them in added and removed sets of a change at the same time
    @Override
    public void setCategorizer(ResourceMultiCategorizer newCategorizer) {
        assert newCategorizer != null;

        if (newCategorizer.equals(multiCategorizer)) {
            return;
        }

        multiCategorizer = newCategorizer;

        LightweightList<CategorizableResourceGroupingChange> changes = CollectionFactory
                .createLightweightList();
        LightweightList<Resource> addedUncategorizableResources = CollectionFactory
                .createLightweightList();
        LightweightList<Resource> removedUncategorizableResources = CollectionFactory
                .createLightweightList();
        clearGrouping(changes, removedUncategorizableResources);
        addResourcesToGrouping(allResources, changes,
                addedUncategorizableResources);

        // TODO do some sort of calculation so that I can remove all of the
        // wrongly removed and added resources

        UncategorizableResourceGroupingChange uncategorizableChanges = UncategorizableResourceGroupingChange
                .uncategorizedResourceChange(uncategorizableResources,
                        addedUncategorizableResources,
                        removedUncategorizableResources);

        fireChanges(changes, uncategorizableChanges);
    }

    @Override
    public void setResourceSet(ResourceSet newResourceSet) {
        allResources.setResourceSet(newResourceSet);
    }
}