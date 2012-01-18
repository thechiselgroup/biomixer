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

import org.thechiselgroup.biomixer.client.core.label.HasLabel;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * <p>
 * Classes implementing this interface manage sets of resources. They support
 * event notification on changes and are more heavy-weight than plain arrays or
 * sets. For intermediate calculations that do not require events etc, consider
 * using a {@link LightweightList} via {@link CollectionFactory}.
 * </p>
 * <p>
 * <b>IMPLEMENTATION NOTE</b>: This interface originally extended
 * java.util.Set<Resource>. However, the collection classes in java.util require
 * supporting subclasses of Resource, as well as supporting generic objects in
 * contains and remove. In GWT, this lead to a performance penalty, as
 * instanceof checks and class casting are fairly expensive (at least according
 * to profiling in Chrome 8). Therefore, this interface now provides methods
 * that resemble the Java collections API, but are specific to Resources.
 * </p>
 * 
 * @author Lars Grammel
 * 
 * @see Resource
 */
public interface ResourceSet extends HasLabel, LightweightCollection<Resource> {

    boolean add(Resource resource);

    boolean addAll(Iterable<Resource> resources);

    /**
     * Adds an event handler that gets called when resources are added to or
     * removed from this resource set.
     * 
     * @param handler
     *            event handler that gets called when resources are added or
     *            removed
     * 
     * @return handler registration for removing the event handler
     */
    // TODO rename
    HandlerRegistration addEventHandler(ResourceSetChangedEventHandler handler);

    /**
     * Adds and removes resources in a single operation (which triggers a single
     * event). The intersection of <code>resourcesToAdd</code> and
     * <code>resourcesToRemove</code> must be empty.
     * 
     * @return true if the resource set changed as a result of this operation.
     */
    boolean change(Iterable<Resource> resourcesToAdd,
            Iterable<Resource> resourcesToRemove);

    /**
     * Removes all resources from this resource set.
     */
    void clear();

    /**
     * <p>
     * <b>PERFORMANCE</b>: Assumed to be very fast (linear to size of
     * ResourceSet with very small multiplier). ResourceSet implementations
     * should use hashing of the resource URI for containment checks.
     * </p>
     * 
     * @param resource
     *            Resource. Must not be null.
     */
    @Override
    boolean contains(Resource resource);

    /**
     * Tests that all {@link Resource}s from <code>resources</code> are
     * contained in this {@link ResourceSet}.
     */
    boolean containsAll(Iterable<Resource> resources);

    boolean containsEqualResources(ResourceSet other);

    /**
     * Tests that no {@link Resource} from <code>resources</code> is contained
     * in this {@link ResourceSet}.
     */
    boolean containsNone(Iterable<Resource> resources);

    /**
     * <p>
     * <b>PERFORMANCE</b>: Assumed to be very fast (linear to size of
     * ResourceSet with very small multiplier). ResourceSet implementations
     * should use hashing of the resource URI for containment checks.
     * </p>
     * 
     * @param uri
     *            Resource URI. Must not be null.
     */
    boolean containsResourceWithUri(String uri);

    Resource getByUri(String uri);

    /**
     * Calculates the intersection of this resource set and the resources in the
     * parameter.
     * 
     * @return LightweightList with resources that are in both this resource set
     *         and in <code>resources</code>.
     */
    LightweightList<Resource> getIntersection(
            LightweightCollection<Resource> resources);

    /**
     * Inverts the containment of this resource. If the resource is already
     * contained, it is removed. If it is not already contained, it is added.
     */
    void invert(Resource resource);

    /**
     * Inverts the containment of the resources in the parameter. For each
     * resource, if the resource is already contained, it is removed. If the
     * resource is not already contained, it is added.
     */
    void invertAll(Iterable<Resource> resources);

    boolean isModifiable();

    /**
     * @return Iterator over the resources in this resource set. Resource sets
     *         are sorted by the natural order of the URIs of the contained
     *         resources.
     */
    @Override
    Iterator<Resource> iterator();

    boolean remove(Resource resource);

    boolean removeAll(Iterable<Resource> resources);

    /**
     * Removes all resources from this resource set that are not contained in
     * the parameter resource set.
     * 
     * @param resources
     *            resource set with resource that will remain in this resource
     *            set
     * 
     * @return true if this resource set changed as a result of this operation
     */
    boolean retainAll(ResourceSet resources);

    /**
     * Returns the number of resources in this resource set.
     * 
     * @return number of resources in this set.
     */
    @Override
    int size();

    /**
     * <b>PERFORMANCE</b>: This method is slow and meant for use in tests.
     * 
     * @return Unmodifiable List that contains elements from this resource set.
     */
    @Override
    List<Resource> toList();

}