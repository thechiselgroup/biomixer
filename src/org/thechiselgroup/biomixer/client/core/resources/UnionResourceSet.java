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

public class UnionResourceSet extends AbstractCombinedResourceSet {

    public UnionResourceSet(ResourceSet delegate) {
        super(delegate);
    }

    @Override
    protected LightweightCollection<Resource> calculateResourcesToAdd(
            LightweightCollection<Resource> resources) {
        return resources;
    }

    @Override
    protected LightweightList<Resource> calculateResourcesToRemove(
            LightweightCollection<Resource> resources) {

        LightweightList<Resource> toRemove = CollectionFactory
                .createLightweightList();
        for (Resource resource : resources) {
            boolean contained = false;
            for (ResourceSetElement rse : containedResourceSets) {
                if (rse.resourceSet.contains(resource)) {
                    contained = true;
                    break;
                }
            }

            if (!contained) {
                toRemove.add(resource);
            }
        }
        return toRemove;
    }

    @Override
    protected void doAdd(ResourceSet resourceSet) {
        addAll(resourceSet);
    }

    @Override
    protected void doRemove(ResourceSet resourceSet) {
        removeAll(calculateResourcesToRemove(resourceSet));
    }

}
