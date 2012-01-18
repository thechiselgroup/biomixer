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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.thechiselgroup.biomixer.client.core.label.LabelChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.util.NullHandlerRegistration;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.collections.NullIterator;

import com.google.gwt.event.shared.HandlerRegistration;

public final class NullResourceSet implements ResourceSet {

    public final static ResourceSet NULL_RESOURCE_SET = new NullResourceSet();

    public static boolean isNullResourceSet(ResourceSet resourceSet) {
        return resourceSet instanceof NullResourceSet;
    }

    private NullResourceSet() {

    }

    @Override
    public boolean add(Resource resource) {
        return false;
    }

    @Override
    public boolean addAll(Iterable<Resource> resources) {
        return false;
    }

    @Override
    public HandlerRegistration addEventHandler(
            ResourceSetChangedEventHandler handler) {

        return NullHandlerRegistration.NULL_HANDLER_REGISTRATION;
    }

    @Override
    public HandlerRegistration addLabelChangedEventHandler(
            LabelChangedEventHandler eventHandler) {

        return NullHandlerRegistration.NULL_HANDLER_REGISTRATION;
    }

    @Override
    public boolean change(Iterable<Resource> addedResources,
            Iterable<Resource> removedResources) {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean contains(Resource r) {
        return false;
    }

    @Override
    public boolean containsAll(Iterable<Resource> resources) {
        return false;
    }

    @Override
    public boolean containsEqualResources(ResourceSet other) {
        return false;
    }

    @Override
    public boolean containsNone(Iterable<Resource> resources) {
        return true;
    }

    @Override
    public boolean containsResourceWithUri(String uri) {
        return false;
    }

    @Override
    public Resource getByUri(String uri) {
        return null;
    }

    @Override
    public Resource getFirstElement() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    @Override
    public LightweightList<Resource> getIntersection(
            LightweightCollection<Resource> resources) {

        return CollectionFactory.createLightweightList();
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public boolean hasLabel() {
        return false;
    }

    @Override
    public void invert(Resource resource) {
    }

    @Override
    public void invertAll(Iterable<Resource> resources) {
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public Iterator<Resource> iterator() {
        return NullIterator.nullIterator();
    }

    @Override
    public boolean remove(Resource o) {
        return false;
    }

    @Override
    public boolean removeAll(Iterable<Resource> resources) {
        return false;
    }

    @Override
    public boolean retainAll(ResourceSet resources) {
        return false;
    }

    @Override
    public void setLabel(String label) {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public List<Resource> toList() {
        return Collections.emptyList();
    }

}
