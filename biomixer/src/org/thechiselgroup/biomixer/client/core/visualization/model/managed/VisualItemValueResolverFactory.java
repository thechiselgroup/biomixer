/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.visualization.model.managed;

import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;

/**
 * This interface is used to return new instances of a
 * {@link VisualItemValueResolver} . This is used so that the application can
 * create new instances on the fly as users create the need for them
 */
public interface VisualItemValueResolverFactory extends Identifiable {

    /**
     * A factory must always know if a resolver that it has created is
     * applicable to a set of view items in a given slot. This means that any
     * internal state change of a resolver must not change whether or not it can
     * resolve view items, only how it does the resolution.
     */
    boolean canCreateApplicableResolver(Slot slot,
            LightweightCollection<VisualItem> visualItems);

    /**
     * @return A new instance of the corresponding
     *         {@link ManagedVisualItemValueResolver}
     */
    ManagedVisualItemValueResolver create(
            LightweightCollection<VisualItem> visualItems);

    /**
     * @return the id of both the factory and the resolver
     */
    @Override
    String getId();

    /**
     * @return The label used in the UI that describes the factory
     */
    String getLabel();

}
