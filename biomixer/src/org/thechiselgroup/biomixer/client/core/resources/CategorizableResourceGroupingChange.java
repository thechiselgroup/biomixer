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

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;

/**
 * Delta change to a group in {@link ResourceGrouping}.
 * 
 * @author Lars Grammel
 */
public class CategorizableResourceGroupingChange extends ResourceGroupingChange {

    public static enum ChangeType {

        GROUP_CREATED, GROUP_REMOVED, GROUP_CHANGED

    }

    /**
     * @param groupID
     *            identifier of the changed group. The identifier is local to
     *            the grouping.
     * @param resourceSet
     *            What is included here depends on the delta: Delta.ADD: new
     *            content (all resources in set); Delta.UPDATE: new content (all
     *            resources in set); Delta.REMOVE: old content
     * @param addedResources
     *            Resources that were added to the group. Can be
     *            <code>null</code> if no resources were added.
     * @param removedResources
     *            Resources that were removed from the group. Can be
     *            <code>null</code> if no resources were removed.
     */
    public static CategorizableResourceGroupingChange newGroupChangedDelta(
            String groupID, ResourceSet resourceSet,
            LightweightCollection<Resource> addedResources,
            LightweightCollection<Resource> removedResources) {

        assert groupID != null;
        assert resourceSet != null;
        assert addedResources == null
                || resourceSet.containsAll(addedResources);
        assert removedResources == null
                || CollectionUtils.containsNone(resourceSet.toList(),
                        CollectionUtils.toList(removedResources));
        assert removedResources == null
                || addedResources == null
                || !CollectionUtils.intersects(removedResources.toList(),
                        addedResources.toList());

        return new CategorizableResourceGroupingChange(
                ChangeType.GROUP_CHANGED, groupID, resourceSet,
                addedResources == null ? LightweightCollections
                        .<Resource> emptyCollection() : addedResources,
                removedResources == null ? LightweightCollections
                        .<Resource> emptyCollection() : removedResources);
    }

    /**
     * @param groupID
     *            identifier of the created group. The identifier is local to
     *            the grouping.
     * @param resourceSet
     *            added {@link ResourceSet}
     */
    public static CategorizableResourceGroupingChange newGroupCreatedDelta(
            String groupID, ResourceSet resourceSet) {
        return newGroupCreatedDelta(groupID, resourceSet, resourceSet);
    }

    /**
     * @param groupID
     *            identifier of the created group. The identifier is local to
     *            the grouping.
     * @param resourceSet
     *            added {@link ResourceSet}
     * @param addedResources
     *            {@link Iterable} over added resources
     */
    public static CategorizableResourceGroupingChange newGroupCreatedDelta(
            String groupID, ResourceSet resourceSet,
            LightweightCollection<Resource> addedResources) {

        assert addedResources != null;
        assert resourceSet != null;
        assert groupID != null;
        assert CollectionUtils.contentEquals(resourceSet,
                CollectionUtils.toList(addedResources));

        return new CategorizableResourceGroupingChange(
                ChangeType.GROUP_CREATED, groupID, resourceSet, addedResources,
                LightweightCollections.<Resource> emptyCollection());
    }

    /**
     * @param groupID
     *            identifier of the created group. The identifier is local to
     *            the grouping.
     * @param resourceSet
     *            added {@link ResourceSet}
     */
    public static CategorizableResourceGroupingChange newGroupRemovedDelta(
            String groupID, ResourceSet resourceSet) {
        return newGroupRemovedDelta(groupID, resourceSet, resourceSet);
    }

    /**
     * @param groupID
     *            identifier of the created group. The identifier is local to
     *            the grouping.
     * @param resourceSet
     *            added {@link ResourceSet}
     * @param removedResources
     *            {@link Iterable} over removed resources
     */
    public static CategorizableResourceGroupingChange newGroupRemovedDelta(
            String groupID, ResourceSet resourceSet,
            LightweightCollection<Resource> removedResources) {

        assert removedResources != null;
        assert resourceSet != null;
        assert groupID != null;
        assert CollectionUtils.contentEquals(resourceSet,
                CollectionUtils.toList(removedResources));

        return new CategorizableResourceGroupingChange(
                ChangeType.GROUP_REMOVED, groupID, resourceSet,
                LightweightCollections.<Resource> emptyCollection(),
                removedResources);
    }

    private ChangeType delta;

    private String groupID;

    private CategorizableResourceGroupingChange(ChangeType delta,
            String groupID, ResourceSet resourceSet,
            LightweightCollection<Resource> addedResources,
            LightweightCollection<Resource> removedResources) {

        assert delta != null;
        assert groupID != null;
        assert resourceSet != null;
        assert addedResources != null;
        assert removedResources != null;

        this.delta = delta;
        this.groupID = groupID;
        this.resourceSet = resourceSet;
        this.addedResources = addedResources;
        this.removedResources = removedResources;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        CategorizableResourceGroupingChange other = (CategorizableResourceGroupingChange) obj;

        if (!groupID.equals(other.groupID)) {
            return false;
        }

        if (delta != other.delta) {
            return false;
        }

        if (!resourceSet.containsEqualResources(other.resourceSet)) {
            return false;
        }

        if (!CollectionUtils
                .contentEquals(addedResources, other.addedResources)) {
            return false;
        }

        if (!CollectionUtils.contentEquals(removedResources,
                other.removedResources)) {
            return false;
        }

        return true;
    }

    /**
     * @return {@link ChangeType} - type of change
     */
    public ChangeType getDelta() {
        return delta;
    }

    /**
     * @return identifier of the changed group
     */
    public String getGroupID() {
        return groupID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupID == null) ? 0 : groupID.hashCode());
        result = prime * result + ((delta == null) ? 0 : delta.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ResourceGroupingChange [delta=" + delta + ", groupID="
                + groupID + ", resourceSet=" + resourceSet + "]";
    }

}