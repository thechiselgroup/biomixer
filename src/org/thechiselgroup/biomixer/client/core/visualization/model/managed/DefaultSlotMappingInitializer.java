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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.resources.DataTypeLists;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetUtils;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.FixedVisualItemResolverFactory;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.PropertyDependantVisualItemValueResolverFactory;

public class DefaultSlotMappingInitializer implements SlotMappingInitializer {

    private Map<DataType, VisualItemValueResolver> fixedResolvers = new EnumMap<DataType, VisualItemValueResolver>(
            DataType.class);

    private Map<DataType, PropertyDependantVisualItemValueResolverFactory> propertyResolverFactories = new EnumMap<DataType, PropertyDependantVisualItemValueResolverFactory>(
            DataType.class);

    public void configureFixedResolver(
            FixedVisualItemResolverFactory resolverFactory) {

        assert resolverFactory != null;

        VisualItemValueResolver resolver = resolverFactory.create();
        fixedResolvers.put(resolverFactory.getDataType(), resolver);
    }

    public void configurePropertyResolver(
            PropertyDependantVisualItemValueResolverFactory resolverFactory) {

        assert resolverFactory != null;

        propertyResolverFactories.put(resolverFactory.getDataType(),
                resolverFactory);
    }

    private VisualItemValueResolver createPropertyResolver(DataType dataType,
            String firstProperty) {

        return propertyResolverFactories.get(dataType).create(firstProperty);
    }

    private VisualItemValueResolver getFixedResolver(DataType dataType) {
        assert fixedResolvers.containsKey(dataType) : "no fixed resolver for "
                + dataType;
        return fixedResolvers.get(dataType);
    }

    @Override
    public Map<Slot, VisualItemValueResolver> getResolvers(
            ResourceSet viewResources, Map<Slot, ManagedSlotMappingState> states) {

        // TODO make this more intelligent
        return getResolvers(viewResources, states.keySet().toArray(new Slot[0]));
    }

    @Override
    public Map<Slot, VisualItemValueResolver> getResolvers(
            ResourceSet viewResources, Slot[] slotsToUpdate) {

        DataTypeLists<String> propertiesByDataType = ResourceSetUtils
                .getPropertiesByDataType(viewResources);

        Map<Slot, VisualItemValueResolver> result = new HashMap<Slot, VisualItemValueResolver>();
        for (Slot slot : slotsToUpdate) {
            result.put(slot, getSlotResolver(propertiesByDataType, slot));
        }
        return result;
    }

    private VisualItemValueResolver getSlotResolver(
            DataTypeLists<String> propertiesByDataType, Slot slot) {

        DataType dataType = slot.getDataType();
        LightweightList<String> properties = propertiesByDataType.get(dataType);

        // fallback to default values if there are no corresponding slots
        if (properties.isEmpty()) {
            return getFixedResolver(dataType);
        }

        assert !properties.isEmpty();

        // dynamic resolution
        String firstProperty = properties.get(0);

        if (!propertyResolverFactories.containsKey(dataType)) {
            throw new UnableToInitializeSlotException(slot);
        }

        return createPropertyResolver(dataType, firstProperty);
    }
}