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
package org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSetUtils;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedVisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.PropertyDependantVisualItemValueResolver;

public abstract class PropertyDependantVisualItemValueResolverFactory extends
        AbstractVisualItemValueResolverFactory {

    protected PropertyDependantVisualItemValueResolverFactory(String id,
            DataType dataType, String label) {

        super(id, dataType, label);
    }

    @Override
    public boolean canCreateApplicableResolver(Slot slot,
            LightweightCollection<VisualItem> visualItems) {

        assert slot != null;
        assert visualItems != null;

        if (!super.canCreateApplicableResolver(slot, visualItems)) {
            return false;
        }

        return !ResourceSetUtils.getSharedPropertiesOfDataType(visualItems,
                getDataType()).isEmpty();
    }

    /**
     * This can fail if you do not first check to see if this factory is
     * applicable. Checking if the factory can create a resolver will set an
     * initial property for the resolver to use
     */
    // XXX this method does some inference - does it belong here??
    @Override
    public ManagedVisualItemValueResolver create(
            LightweightCollection<VisualItem> visualItems) {

        assert visualItems != null;
        List<String> properties = ResourceSetUtils
                .getSharedPropertiesOfDataType(visualItems, dataType);
        assert !properties.isEmpty();
        return create(properties.get(0));
    }

    public ManagedVisualItemValueResolver create(String property) {
        return wrap(createUnmanagedResolver(property));
    }

    protected abstract PropertyDependantVisualItemValueResolver createUnmanagedResolver(
            String property);

    protected PropertyDependantManagedVisualItemValueResolverDecorator wrap(
            PropertyDependantVisualItemValueResolver delegate) {

        return new PropertyDependantManagedVisualItemValueResolverDecorator(id,
                delegate);
    }

}