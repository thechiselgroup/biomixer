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
package org.thechiselgroup.biomixer.client.core.visualization.model.persistence;

import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.persistence.IdentifiableCreatingPersistence;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.RestoringPersistenceManager;
import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesSet;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.DefaultManagedSlotMappingConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedSlotMappingConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedVisualItemValueResolver;

/**
 * Adapter for {@link DefaultManagedSlotMappingConfiguration} that provides
 * persistence capabilities.
 * 
 * @author Lars Grammel
 */
public class ManagedSlotMappingConfigurationPersistence implements
        RestoringPersistenceManager<ManagedSlotMappingConfiguration> {

    private IdentifiablesSet<IdentifiableCreatingPersistence<ManagedVisualItemValueResolver>> visualItemResolverFactoryPersistenceManagers = new IdentifiablesSet<IdentifiableCreatingPersistence<ManagedVisualItemValueResolver>>();

    public void registerResolverPersistence(
            IdentifiableCreatingPersistence<ManagedVisualItemValueResolver> persistenceManager) {

        visualItemResolverFactoryPersistenceManagers.put(persistenceManager);
    }

    @Override
    public void restore(ManagedSlotMappingConfiguration configuration,
            Memento memento) {

        assert memento != null;
        assert configuration != null;

        for (Entry<String, Memento> entry : memento.getChildren().entrySet()) {
            String slotId = entry.getKey();
            Memento child = entry.getValue();

            assert child.getFactoryId() != null : "factory id must not be null ("
                    + child + ")";

            Slot slot = configuration.getSlotById(slotId);
            ManagedVisualItemValueResolver resolver = visualItemResolverFactoryPersistenceManagers
                    .get(child.getFactoryId()).restore(child);

            configuration.setCurrentResolver(slot, resolver);
        }
    }

    @Override
    public Memento save(ManagedSlotMappingConfiguration configuration) {
        assert configuration != null;

        Memento memento = new Memento();

        Slot[] slots = configuration.getSlots();
        for (Slot slot : slots) {
            if (configuration.isConfigured(slot)) {
                ManagedVisualItemValueResolver resolver = configuration
                        .getCurrentResolver(slot);

                Memento child = visualItemResolverFactoryPersistenceManagers
                        .get(resolver.getId()).save(resolver);
                child.setFactoryId(resolver.getId());
                memento.addChild(slot.getId(), child);
            }
        }

        return memento;
    }

}
