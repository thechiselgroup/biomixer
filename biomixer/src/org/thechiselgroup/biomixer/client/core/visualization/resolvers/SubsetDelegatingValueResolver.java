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
package org.thechiselgroup.biomixer.client.core.visualization.resolvers;

import static org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections.toCollection;

import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.ManagedVisualItemValueResolverDecorator;

public class SubsetDelegatingValueResolver implements VisualItemValueResolver {

    private Slot slot;

    private LightweightCollection<Slot> targetSlots;

    private Subset subset;

    public SubsetDelegatingValueResolver(Slot slot, Subset subset) {
        assert slot != null;
        assert subset != null;

        this.slot = slot;
        this.subset = subset;
        this.targetSlots = toCollection(slot);
    }

    @Override
    public boolean canResolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {

        if (!context.isConfigured(slot)) {
            return false;
        }

        assert context.getResolver(slot) != null;

        return context.getResolver(slot).canResolve(visualItem, context);
    }

    @Override
    public LightweightCollection<Slot> getTargetSlots() {
        return targetSlots;
    }

    @Override
    public Object resolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {

        VisualItemValueResolver delegate = context.getResolver(slot);

        assert delegate != null;

        /*
         * XXX Explicitly checking for ManagedVisualItemValueResolverDecorator
         * and unwrapping the delegate is not an elegant solution, but it works
         * for now. Once the requirements for the value resolvers are clearer
         * and the managed layer is well tested, this should be changed.
         */
        if (delegate instanceof ManagedVisualItemValueResolverDecorator) {
            delegate = ((ManagedVisualItemValueResolverDecorator) delegate)
                    .getDelegate();
        }

        if (delegate instanceof SubsetVisualItemValueResolver) {
            return ((SubsetVisualItemValueResolver) delegate).resolve(
                    visualItem, context, subset);
        }

        return delegate.resolve(visualItem, context);
    }
}