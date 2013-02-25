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

import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;

import com.google.inject.Inject;

// TODO need dispose
public class ManagedResourceSet extends AbstractUriMapBasedResourceSet {

    private final ResourceManager resourceManager;

    @Inject
    public ManagedResourceSet(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    protected void doAdd(Resource resource,
            LightweightList<Resource> addedResources) {
        // XXX Allowing one by one addition here makes things slow when we have
        // many resources to add.
        // Should the interface try to support only bulk adds?
        resourceManager.add(resource);
        Resource realResource = resourceManager.allocate(resource.getUri());
        addResourceToMap(realResource);
        addedResources.add(resource);
    }

    @Override
    public void doRemove(Resource resource,
            LightweightList<Resource> removedResources) {

        String key = resource.getUri();
        removeResourceFromMap(key);
        resourceManager.deallocate(key);
        removedResources.add(resource);
    }

}
