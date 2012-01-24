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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.persistence.IdentifiableCreatingPersistence;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedSlotMappingConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedVisualItemValueResolver;

public class ManagedSlotMappingConfigurationPersistenceTest {

    private static final String SLOT_ID = "i1";

    private static final String RESOLVER_ID = "id";

    private ManagedSlotMappingConfigurationPersistence underTest;

    @Mock
    private ManagedSlotMappingConfiguration configuration;

    @Mock
    private ManagedSlotMappingConfiguration restoredConfiguration;

    @Mock
    private ManagedVisualItemValueResolver resolver;

    @Mock
    private ManagedVisualItemValueResolver restoredResolver;

    private Slot[] slots;

    @Mock
    private IdentifiableCreatingPersistence<ManagedVisualItemValueResolver> persistenceManager;

    @Test
    public void saveRestore1() {
        Memento memento = new Memento();

        setUpSlots(configuration);
        setUpSlots(restoredConfiguration);
        when(configuration.getCurrentResolver(slots[0])).thenReturn(resolver);
        when(resolver.getId()).thenReturn(RESOLVER_ID);
        when(persistenceManager.save(resolver)).thenReturn(memento);
        when(persistenceManager.restore(memento)).thenReturn(restoredResolver);

        underTest.restore(restoredConfiguration, underTest.save(configuration));

        verify(restoredConfiguration, times(1)).setCurrentResolver(slots[0],
                restoredResolver);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        slots = new Slot[] { new Slot(SLOT_ID, "n1", DataType.TEXT) };

        when(persistenceManager.getId()).thenReturn(RESOLVER_ID);

        underTest = new ManagedSlotMappingConfigurationPersistence();
        underTest.registerResolverPersistence(persistenceManager);
    }

    private void setUpSlots(ManagedSlotMappingConfiguration configuration2) {
        when(configuration2.getSlots()).thenReturn(slots);
        when(configuration2.isConfigured(slots[0])).thenReturn(true);
        when(configuration2.getSlotById(SLOT_ID)).thenReturn(slots[0]);
    }
}