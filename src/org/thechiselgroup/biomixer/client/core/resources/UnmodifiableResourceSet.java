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

public class UnmodifiableResourceSet extends DelegatingResourceSet {

    public UnmodifiableResourceSet(ResourceSet delegate) {
        super(delegate);
    }

    @Override
    public boolean add(Resource i) {
        throw new UnsupportedOperationException(
                "UnmodifiableResourceSet.add not supported");
    }

    @Override
    public boolean addAll(Iterable<Resource> resources) {
        throw new UnsupportedOperationException(
                "UnmodifiableResourceSet.addAll not supported");
    }

    @Override
    public boolean change(Iterable<Resource> addedResources,
            Iterable<Resource> removedResources) {
        throw new UnsupportedOperationException(
                "FilteredResourceSet.change is not supported");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(
                "UnmodifiableResourceSet.clear not supported");
    }

    @Override
    public void invert(Resource resource) {
        throw new UnsupportedOperationException(
                "UnmodifiableResourceSet.invert not supported");
    }

    @Override
    public void invertAll(Iterable<Resource> resources) {
        throw new UnsupportedOperationException(
                "UnmodifiableResourceSet.invertAll not supported");
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public boolean remove(Resource r) {
        throw new UnsupportedOperationException(
                "UnmodifiableResourceSet.remove not supported");
    }

    @Override
    public boolean removeAll(Iterable<Resource> resources) {
        throw new UnsupportedOperationException(
                "UnmodifiableResourceSet.removeAll not supported");
    }

    @Override
    public boolean retainAll(ResourceSet resources) {
        throw new UnsupportedOperationException(
                "UnmodifiableResourceSet.retainAll not supported");
    }

    @Override
    public void setLabel(String label) {
        throw new UnsupportedOperationException(
                "UnmodifiableResourceSet.setLabel not supported");
    }

}