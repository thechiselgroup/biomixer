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

import org.thechiselgroup.biomixer.client.core.visualization.resolvers.PropertyDependantVisualItemValueResolver;

/**
 * This class is a specific type of ManagedResolverDecorator for Resolvers that
 * are Property Dependent. This is needed because in some cases, we assert that
 * the resolvers are property dependent, but because they use a decorator, a
 * simple instanceof check won't work. Instead, we use this class and do an
 * instance check on it.
 */
public class PropertyDependantManagedVisualItemValueResolverDecorator extends
        ManagedVisualItemValueResolverDecorator implements
        PropertyDependantVisualItemValueResolver {

    public PropertyDependantManagedVisualItemValueResolverDecorator(
            String resolverId, PropertyDependantVisualItemValueResolver delegate) {

        super(resolverId, delegate);
    }

    @Override
    public String getProperty() {
        return ((PropertyDependantVisualItemValueResolver) delegate)
                .getProperty();
    }

}