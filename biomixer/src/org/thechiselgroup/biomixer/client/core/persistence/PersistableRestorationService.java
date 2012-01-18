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
package org.thechiselgroup.biomixer.client.core.persistence;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;

/**
 * Manages {@link PersistableObjectFactory}s to restore {@link Persistable}s
 * from {@link Memento}s using the factory id.
 * 
 * @author Lars Grammel
 */
public class PersistableRestorationService {

    private final Map<String, PersistableObjectFactory> persistableObjectFactoriesById = CollectionFactory
            .createStringMap();

    public void addFactory(PersistableObjectFactory factory) {
        assert factory != null;

        persistableObjectFactoriesById.put(factory.getFactoryId(), factory);
    }

    /**
     * Restores {@link Persistable} from {@link Memento}s using the id of the
     * corresponding {@link PersistableObjectFactory}.
     */
    public Persistable restoreFromMemento(Memento memento,
            ResourceSetAccessor accessor) {

        assert memento != null;
        assert memento.getFactoryId() != null;
        assert persistableObjectFactoriesById.containsKey(memento
                .getFactoryId());
        assert accessor != null;

        return persistableObjectFactoriesById.get(memento.getFactoryId())
                .restore(memento, accessor);
    }

}
