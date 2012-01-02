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
package org.thechiselgroup.biomixer.client.core.visualization.model.managed;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;

/**
 * Calculates default {@link VisualItemValueResolver}s. Used to set
 * {@link VisualItemValueResolver}s on a {@link VisualizationModel} if the
 * mappings for some {@link Slot}s are invalid.
 */
// TODO rename to DefaultVisualItemValueResolverProvider
public interface SlotMappingInitializer {

    Map<Slot, VisualItemValueResolver> getResolvers(ResourceSet viewResources,
            Map<Slot, ManagedSlotMappingState> states);

    /**
     * Calculates default {@link VisualItemValueResolver}s.
     * 
     * @param viewResources
     *            All {@link Resource}s that are contained in the
     *            {@link VisualizationModel}.
     * @param slotsToUpdate
     *            {@link Slot}s for which default
     *            {@link VisualItemValueResolver}s should be calculated.
     * 
     * @return A map that contains {@link VisualItemValueResolver}s for some or
     *         all of the {@link Slot}s specified in {@code slotsToUpdate}.
     *         There must be no {@link Slot}s in the result that are not
     *         contained in {@code slotsToUpdate}.
     */
    Map<Slot, VisualItemValueResolver> getResolvers(ResourceSet viewResources,
            Slot[] slotsToUpdate);

}