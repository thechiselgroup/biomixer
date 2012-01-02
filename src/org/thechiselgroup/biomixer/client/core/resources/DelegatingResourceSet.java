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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.thechiselgroup.biomixer.client.core.label.LabelChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;

import com.google.gwt.event.shared.HandlerRegistration;

// TODO support for null delegates?
public class DelegatingResourceSet implements ResourceSet {

    // TODO support for null delegates?

    protected ResourceSet delegate;

    public DelegatingResourceSet(ResourceSet delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean add(Resource resource) {
        return delegate.add(resource);
    }

    @Override
    public boolean addAll(Iterable<Resource> resources) {
        return delegate.addAll(resources);
    }

    @Override
    public HandlerRegistration addEventHandler(
            ResourceSetChangedEventHandler handler) {
        return delegate.addEventHandler(handler);
    }

    @Override
    public HandlerRegistration addLabelChangedEventHandler(
            LabelChangedEventHandler eventHandler) {
        return delegate.addLabelChangedEventHandler(eventHandler);
    }

    @Override
    public boolean change(Iterable<Resource> addedResources,
            Iterable<Resource> removedResources) {
        return delegate.change(addedResources, removedResources);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean contains(Resource resource) {
        return delegate.contains(resource);
    }

    @Override
    public boolean containsAll(Iterable<Resource> resources) {
        return delegate.containsAll(resources);
    }

    @Override
    public boolean containsEqualResources(ResourceSet other) {
        return delegate.containsEqualResources(other);
    }

    @Override
    public boolean containsNone(Iterable<Resource> resources) {
        return delegate.containsNone(resources);
    }

    @Override
    public boolean containsResourceWithUri(String uri) {
        return delegate.containsResourceWithUri(uri);
    }

    @Override
    public Resource getByUri(String uri) {
        return delegate.getByUri(uri);
    }

    public ResourceSet getDelegate() {
        return delegate;
    }

    @Override
    public Resource getFirstElement() throws NoSuchElementException {
        return delegate.getFirstElement();
    }

    @Override
    public LightweightList<Resource> getIntersection(
            LightweightCollection<Resource> resources) {
        return delegate.getIntersection(resources);
    }

    @Override
    public String getLabel() {
        return delegate.getLabel();
    }

    @Override
    public boolean hasLabel() {
        return delegate.hasLabel();
    }

    @Override
    public void invert(Resource resource) {
        delegate.invert(resource);
    }

    @Override
    public void invertAll(Iterable<Resource> resources) {
        delegate.invertAll(resources);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean isModifiable() {
        return delegate.isModifiable();
    }

    @Override
    public Iterator<Resource> iterator() {
        return delegate.iterator();
    }

    @Override
    public boolean remove(Resource resource) {
        return delegate.remove(resource);
    }

    @Override
    public boolean removeAll(Iterable<Resource> resources) {
        return delegate.removeAll(resources);
    }

    @Override
    public boolean retainAll(ResourceSet resources) {
        return delegate.retainAll(resources);
    }

    @Override
    public void setLabel(String label) {
        delegate.setLabel(label);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public List<Resource> toList() {
        return delegate.toList();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}