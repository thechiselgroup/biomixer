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

import org.thechiselgroup.biomixer.client.core.persistence.IdentifiableCreatingPersistence;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedVisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.SingletonVisualItemResolverFactory;

public class SingletonVisualItemResolverPersistence implements
        IdentifiableCreatingPersistence<ManagedVisualItemValueResolver> {

    private SingletonVisualItemResolverFactory resolverFactory;

    public SingletonVisualItemResolverPersistence(
            SingletonVisualItemResolverFactory resolverFactory) {
        this.resolverFactory = resolverFactory;
    }

    @Override
    public String getId() {
        return resolverFactory.getId();
    }

    @Override
    public ManagedVisualItemValueResolver restore(Memento memento) {
        return resolverFactory.create();
    }

    @Override
    public Memento save(ManagedVisualItemValueResolver o) {
        return new Memento();
    }

}