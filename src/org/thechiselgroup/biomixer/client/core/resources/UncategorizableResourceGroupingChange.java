package org.thechiselgroup.biomixer.client.core.resources;

import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;

public class UncategorizableResourceGroupingChange extends
        ResourceGroupingChange {

    /**
     * @param resources
     *            this should contain all resources currently in the set
     * @param addedResources
     * @param removedResources
     */
    public static UncategorizableResourceGroupingChange uncategorizedResourceChange(
            ResourceSet resources,
            LightweightCollection<Resource> addedResources,
            LightweightCollection<Resource> removedResources) {

        return new UncategorizableResourceGroupingChange(resources,
                addedResources, removedResources);
    }

    private UncategorizableResourceGroupingChange(ResourceSet resources,
            LightweightCollection<Resource> addedResources,
            LightweightCollection<Resource> removedResources) {

        assert addedResources != null;
        assert removedResources != null;
        assert resources != null;

        this.resourceSet = resources;
        this.addedResources = addedResources;
        this.removedResources = removedResources;
    }

    /**
     * returns whether or not this resourceGroupChange will have any affect on
     * the Grouping
     */
    public boolean hasActualChanges() {
        return !(addedResources.isEmpty() && removedResources.isEmpty());
    }
}
