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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;

/**
 * Allows adding the same resource multiple times and stores the count. The
 * resource has to be removed the same number of times before being removed from
 * the set. Size and the returned element are not affected by double adding,
 * every resource is counted once for size and returned once in the iterator.
 */
public class CountingResourceSet extends AbstractResourceSet {

    private static class ResourceElement {

        private int counter;

        private Resource resource;

        public ResourceElement(Resource resource) {
            this.resource = resource;
            counter = 1;
        }

    }

    private Map<String, ResourceElement> uriToResourceElementMap = CollectionFactory
            .createStringMap();

    @Override
    public boolean addAll(Iterable<Resource> resources) {
        assert resources != null;

        LightweightList<Resource> addedResources = CollectionFactory
                .createLightweightList();
        for (Resource resource : resources) {
            doAdd(resource, addedResources);
        }

        if (!addedResources.isEmpty()) {
            fireEvent(ResourceSetChangedEvent.createResourcesAddedEvent(this,
                    addedResources));
        }

        return !addedResources.isEmpty();
    }

    @Override
    public boolean containsResourceWithUri(String uri) {
        return uriToResourceElementMap.containsKey(uri);
    }

    @Override
    protected void doAdd(Resource resource,
            LightweightList<Resource> addedResources) {
        String uri = resource.getUri();

        if (uriToResourceElementMap.containsKey(uri)) {
            uriToResourceElementMap.get(uri).counter++;
        } else {
            uriToResourceElementMap.put(uri, new ResourceElement(resource));
            addedResources.add(resource);
        }
    }

    @Override
    protected void doRemove(Resource resource,
            LightweightList<Resource> removedResources) {
        String uri = resource.getUri();
        assert uriToResourceElementMap.containsKey(uri);

        uriToResourceElementMap.get(uri).counter--;

        if (uriToResourceElementMap.get(uri).counter == 0) {
            uriToResourceElementMap.remove(uri);
            removedResources.add(resource);
        }
    }

    @Override
    public Resource getByUri(String uri) {
        assert uri != null;
        assert uriToResourceElementMap.containsKey(uri);

        return uriToResourceElementMap.get(uri).resource;
    }

    @Override
    public boolean isEmpty() {
        return uriToResourceElementMap.isEmpty();
    }

    @Override
    public Iterator<Resource> iterator() {
        return toList().iterator();
    }

    @Override
    public int size() {
        return uriToResourceElementMap.size();
    }

    @Override
    public List<Resource> toList() {
        List<Resource> resources = new ArrayList<Resource>();

        for (ResourceElement element : uriToResourceElementMap.values()) {
            resources.add(element.resource);
        }

        return resources;
    }
}
