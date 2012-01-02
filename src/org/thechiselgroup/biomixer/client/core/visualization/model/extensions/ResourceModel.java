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
package org.thechiselgroup.biomixer.client.core.visualization.model.extensions;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.UnionResourceSet;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.predicates.Predicate;

public interface ResourceModel {

    /**
     * Explicit adding of the resource set as a new, displayed resource set.
     */
    void addResourceSet(ResourceSet resourceSet);

    /**
     * Add the resources to the contents of this view without displaying the
     * resource set explicitly.
     */
    void addUnnamedResources(Iterable<Resource> resources);

    void clear();

    /**
     * Checks if the resources are displayed in this view.
     */
    boolean containsResources(Iterable<Resource> resources);

    /**
     * Checks if this labeled resource set is explicitly displayed in this view.
     */
    boolean containsResourceSet(ResourceSet resourceSet);

    // TODO document
    ResourceSet getAutomaticResourceSet();

    // TODO document
    UnionResourceSet getCombinedUserResourceSets();

    /**
     * Calculates the intersection, i.e. those resources that are both in the
     * parameter and in this resource model.
     * 
     * @return new <code>LightweightList</code> that contains only the subset of
     *         <code>resources</code> that is contained in this resource model
     */
    LightweightList<Resource> getIntersection(
            LightweightCollection<Resource> resources);

    /**
     * Returns an unmodifiable resource set containing all resources displayed
     * in this view.
     */
    ResourceSet getResources();

    /**
     * Removes a resource set that was explicitly added via
     * {@link #addResourceSet(ResourceSet)}. We assert that the resource set has
     * a label.
     */
    void removeResourceSet(ResourceSet resourceSet);

    /**
     * Removes resources that are <b>not</b> contained in any explicitly added
     * resource set.
     */
    void removeUnnamedResources(Iterable<Resource> resources);

    void setFilterPredicate(Predicate<Resource> filterPredicate);

}