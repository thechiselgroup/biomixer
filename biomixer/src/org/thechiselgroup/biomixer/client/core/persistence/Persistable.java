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

import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;

/**
 * Interface for objects that can be saved into {@link Memento}s and restored
 * from saved {@link Memento}s. Each {@link Persistable} has a corresponding
 * {@link PersistableObjectFactory} that can be used to create a new object from
 * a {@link Memento}.
 * 
 * @author Lars Grammel
 * 
 * @see Memento
 * @see PersistableObjectFactory
 */
public interface Persistable {

    /**
     * @return ID of the {@link PersistableObjectFactory} that can restore this
     *         object from a {@link Memento}.
     */
    // String getFactoryId();
    // TODO reactivate?

    /**
     * Restores the state for an existing object.
     */
    // TODO can we somehow remove accessor?
    void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor);

    /**
     * Saves the state of this {@link Persistable} into a {@link Memento}.
     */
    // TODO can we somehow remove resourceSetCollector?
    Memento save(ResourceSetCollector resourceSetCollector);

}