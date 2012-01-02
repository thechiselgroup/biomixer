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

import java.util.List;

public interface ResourceManager extends ResourceAccessor, Iterable<Resource> {

    /**
     * Adds a resource to this {@code ResourceManager}. If a resource with a
     * similar uri was already contained, this resource is returned.
     * 
     * @param resource
     *            resource that should be added to the {@code ResourceManager}
     * @return resource that is contained in the {@code ResourceManager}.
     */
    Resource add(Resource resource);

    List<Resource> addAll(List<Resource> resources);

    Resource allocate(String uri);

    void clear();

    /**
     * @return {@code true}, if a {@link UriList} property is not {@code null}
     *         and all contained URIs refer to {@link Resource}s in this
     *         {@link ResourceManager}.
     */
    boolean containsAllReferencedResources(Resource resource,
            String uriListProperty);

    void deallocate(String uri);

    List<Resource> getAllResources();

    List<Resource> resolveResources(Resource resource, String uriListProperty);

}