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
package org.thechiselgroup.biomixer.client.core.visualization.model;

/**
 * Provides access to {@link VisualItemValueResolver}s by {@link Slot}.
 * 
 * @author Lars Grammel
 */
// TODO rename to ?
public interface VisualItemValueResolverContext {

    // TODO documentation
    // TODO exception
    VisualItemValueResolver getResolver(Slot slot);

    /**
     * {@link Slot}s that are available in this
     * {@link VisualItemValueResolverContext}.
     * 
     * @return {@link Slot}s that need to be configured.
     */
    // TODO change return type to LightweightCollection<Slot>
    Slot[] getSlots();

    /**
     * Checks if a {@link VisualItemValueResolver} has been set for a
     * {@link Slot} . This does not mean that the value resolver actually
     * resolves to valid values. This would be reported in the
     * {@link VisualItemResolutionErrorModel}.
     * 
     * @return {code true}, if a {@link VisualItemValueResolver} is configured
     *         for {code slot}.
     */
    // TODO throws InvalidSlotException
    boolean isConfigured(Slot slot);

}