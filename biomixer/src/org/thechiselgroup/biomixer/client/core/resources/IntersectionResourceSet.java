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

public class IntersectionResourceSet extends AbstractCombinedResourceSet {

    private UnionResourceSet allResources = new UnionResourceSet(
            new DefaultResourceSet());

    public IntersectionResourceSet(ResourceSet delegate) {
        super(delegate);
    }

    /**
     * @param resources
     *            resources to test
     * @param include
     *            true, if potentially included resources should be calculate,
     *            false for excluded resources
     */
    private LightweightCollection<Resource> calculateContainment(
            LightweightCollection<Resource> resources, boolean include) {

        LightweightList<Resource> result = CollectionFactory
                .createLightweightList();
        for (Resource resource : resources) {
            boolean isContainedInAllSets = false;
            for (ResourceSetElement resourceSetElement : containedResourceSets) {
                if (resourceSetElement.resourceSet.contains(resource)) {
                    isContainedInAllSets = true; // has to appear at least once
                } else {
                    isContainedInAllSets = false;
                    break;
                }
            }
            if (isContainedInAllSets == include) {
                result.add(resource);
            }
        }
        return result;
    }

    /**
     * Calculate resources that are contained in all resource sets.
     */
    @Override
    protected LightweightCollection<Resource> calculateResourcesToAdd(
            LightweightCollection<Resource> resources) {
        return calculateContainment(resources, true);
    }

    @Override
    protected LightweightCollection<Resource> calculateResourcesToRemove(
            LightweightCollection<Resource> resources) {
        return calculateContainment(resources, false);
    }

    @Override
    public void dispose() {
        allResources.dispose();
        super.dispose();
    }

    @Override
    protected void doAdd(ResourceSet resourceSet) {
        allResources.addResourceSet(resourceSet);
        if (containedResourceSets.size() == 1) {
            addAll(resourceSet);
        } else {
            removeAll(calculateResourcesToRemove(delegate));
        }
    }

    @Override
    protected void doRemove(ResourceSet resourceSet) {
        allResources.removeResourceSet(resourceSet);
        if (containedResourceSets.isEmpty()) {
            removeAll(calculateResourcesToRemove(resourceSet));
        } else {
            addAll(calculateResourcesToAdd(allResources));
        }
    }
}
