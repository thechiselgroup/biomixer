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

// TODO move label support into separate class
public abstract class AbstractUriMapBasedResourceSet extends
        AbstractResourceSet {

    protected Map<String, Resource> uriToResource = CollectionFactory
            .createStringMap();

    protected Resource addResourceToMap(Resource resource) {
        return uriToResource.put(resource.getUri(), resource);
    }

    @Override
    public boolean containsResourceWithUri(String uri) {
        assert uri != null;
        return uriToResource.containsKey(uri);
    }

    @Override
    public Resource getByUri(String uri) {
        return uriToResource.get(uri);
    }

    @Override
    public boolean isEmpty() {
        return uriToResource.isEmpty();
    }

    @Override
    public Iterator<Resource> iterator() {
        // FIXME should be unmodifiable
        return uriToResource.values().iterator();
    }

    protected void removeResourceFromMap(String key) {
        uriToResource.remove(key);
    }

    @Override
    public int size() {
        return uriToResource.size();
    }

    @Override
    public List<Resource> toList() {
        return new ArrayList<Resource>(uriToResource.values());
    }

    @Override
    public String toString() {
        return uriToResource.values().toString();
    }

}