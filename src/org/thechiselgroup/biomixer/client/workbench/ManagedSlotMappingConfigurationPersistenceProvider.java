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
package org.thechiselgroup.biomixer.client.workbench;

import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.AVERAGE_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.COUNT_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.DATE_PROPERTY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_COLOR_STEELBLUE_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_DATE_TODAY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_NUMBER_0_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_NUMBER_1_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_TEXT_EMPTY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.ID_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.LOCATION_PROPERTY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.MAX_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.MIN_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.SUM_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.TEXT_PROPERTY_RESOLVER_FACTORY;

import org.thechiselgroup.choosel.core.client.visualization.model.persistence.ManagedSlotMappingConfigurationPersistence;
import org.thechiselgroup.choosel.core.client.visualization.model.persistence.PropertyDependantVisualItemResolverPersistence;
import org.thechiselgroup.choosel.core.client.visualization.model.persistence.SingletonVisualItemResolverPersistence;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.managed.PropertyDependantVisualItemValueResolverFactory;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.managed.SingletonVisualItemResolverFactory;

import com.google.inject.Provider;

// TODO injection etc
public class ManagedSlotMappingConfigurationPersistenceProvider implements
        Provider<ManagedSlotMappingConfigurationPersistence> {

    @Override
    public ManagedSlotMappingConfigurationPersistence get() {
        ManagedSlotMappingConfigurationPersistence persistence = new ManagedSlotMappingConfigurationPersistence();

        register(persistence, ID_RESOLVER_FACTORY);
        register(persistence, COUNT_RESOLVER_FACTORY);

        register(persistence, SUM_RESOLVER_FACTORY);
        register(persistence, AVERAGE_RESOLVER_FACTORY);
        register(persistence, MAX_RESOLVER_FACTORY);
        register(persistence, MIN_RESOLVER_FACTORY);

        register(persistence, TEXT_PROPERTY_RESOLVER_FACTORY);
        register(persistence, LOCATION_PROPERTY_RESOLVER_FACTORY);
        register(persistence, DATE_PROPERTY_RESOLVER_FACTORY);

        register(persistence, FIXED_NUMBER_0_RESOLVER_FACTORY);
        register(persistence, FIXED_NUMBER_1_RESOLVER_FACTORY);
        register(persistence, FIXED_COLOR_STEELBLUE_RESOLVER_FACTORY);
        register(persistence, FIXED_DATE_TODAY_RESOLVER_FACTORY);
        register(persistence, FIXED_TEXT_EMPTY_RESOLVER_FACTORY);

        return persistence;
    }

    protected void register(
            ManagedSlotMappingConfigurationPersistence persistence,
            PropertyDependantVisualItemValueResolverFactory resolverFactory) {

        persistence
                .registerResolverPersistence(new PropertyDependantVisualItemResolverPersistence(
                        resolverFactory));
    }

    protected void register(
            ManagedSlotMappingConfigurationPersistence persistence,
            SingletonVisualItemResolverFactory resolverFactory) {
        persistence
                .registerResolverPersistence(new SingletonVisualItemResolverPersistence(
                        resolverFactory));
    }

}