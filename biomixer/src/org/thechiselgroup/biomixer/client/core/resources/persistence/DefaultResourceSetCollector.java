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
package org.thechiselgroup.biomixer.client.core.resources.persistence;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;

public class DefaultResourceSetCollector implements ResourceSetCollector,
        ResourceSetAccessor {

    private List<ResourceSet> resourceSets = new ArrayList<ResourceSet>();

    @Override
    public ResourceSet getResourceSet(int id) {
        assert 0 <= id;
        assert id < resourceSets.size();
        return resourceSets.get(id);
    }

    public List<ResourceSet> getResourceSets() {
        return resourceSets;
    }

    @Override
    public int storeResourceSet(ResourceSet resourceSet) {
        assert resourceSet != null;

        if (!resourceSets.contains(resourceSet)) {
            resourceSets.add(resourceSet);
        }

        assert resourceSets.contains(resourceSet);
        return resourceSets.indexOf(resourceSet);
    }

}