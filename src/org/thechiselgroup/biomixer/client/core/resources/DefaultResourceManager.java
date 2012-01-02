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

public class DefaultResourceManager implements ResourceManager {

    private static class ResourceElement {

        private int allocationCounter = 0;

        private Resource resource;

        public ResourceElement(Resource resource) {
            this.resource = resource;
        }

        public void allocate() {
            allocationCounter++;
        }

        public void deallocate() {
            assert allocationCounter >= 1;
            allocationCounter--;
        }

        public boolean isUsed() {
            return allocationCounter > 0;
        }

    }

    private Map<String, ResourceElement> keysToResourceElements = CollectionFactory
            .createStringMap();

    @Override
    public Resource add(Resource resource) {
        String key = resource.getUri();

        if (!contains(key)) {
            keysToResourceElements.put(key, new ResourceElement(resource));
        }

        return getResourceElement(key).resource;
    }

    @Override
    public List<Resource> addAll(List<Resource> resources) {
        List<Resource> addedResources = new ArrayList<Resource>();
        for (Resource resource : resources) {
            addedResources.add(add(resource));
        }
        return addedResources;
    }

    @Override
    public Resource allocate(String uri) {
        ResourceElement resourceElement = getResourceElement(uri);
        resourceElement.allocate();
        return resourceElement.resource;
    }

    @Override
    public void clear() {
        keysToResourceElements.clear();
    }

    @Override
    public boolean contains(String uri) {
        return keysToResourceElements.containsKey(uri);
    }

    @Override
    public boolean containsAllReferencedResources(Resource resource,
            String uriListProperty) {

        if (!resource.containsProperty(uriListProperty)) {
            return false;
        }

        UriList uriList = (UriList) resource.getValue(uriListProperty);
        for (String uri : uriList) {
            if (!contains(uri)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void deallocate(String uri) {
        assert uri != null;

        ResourceElement resourceElement = getResourceElement(uri);

        if (resourceElement == null) {
            return;
        }

        resourceElement.deallocate();
        if (!resourceElement.isUsed()) {
            removeResourceElement(uri);
        }
    }

    @Override
    public List<Resource> getAllResources() {
        List<Resource> resources = new ArrayList<Resource>();
        for (ResourceElement element : keysToResourceElements.values()) {
            resources.add(element.resource);
        }
        return resources;
    }

    @Override
    public Resource getByUri(String uri) {
        assert keysToResourceElements.containsKey(uri) : "no resource for uri '"
                + uri + "'";
        return getResourceElement(uri).resource;
    }

    private ResourceElement getResourceElement(String uri) {
        return keysToResourceElements.get(uri);
    }

    @Override
    public Iterator<Resource> iterator() {
        return getAllResources().iterator();
    }

    private void removeResourceElement(String uri) {
        keysToResourceElements.remove(uri);
    }

    @Override
    public List<Resource> resolveResources(Resource resource,
            String uriListProperty) {

        List<Resource> result = new ArrayList<Resource>();
        UriList uris = (UriList) resource.getValue(uriListProperty);
        for (String uri : uris) {
            result.add(getByUri(uri));
        }
        return result;
    }

    @Override
    public String toString() {
        return getAllResources().toString();
    }

}