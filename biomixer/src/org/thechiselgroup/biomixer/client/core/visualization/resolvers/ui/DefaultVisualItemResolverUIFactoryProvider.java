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
package org.thechiselgroup.biomixer.client.core.visualization.resolvers.ui;

import org.thechiselgroup.biomixer.client.core.util.collections.Registry;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.PropertyDependantVisualItemValueResolverFactory;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.SingletonVisualItemResolverFactory;

public class DefaultVisualItemResolverUIFactoryProvider extends
        Registry<VisualItemValueResolverUIControllerFactory> implements
        VisualItemValueResolverUIControllerFactoryProvider {

    public void register(
            PropertyDependantVisualItemValueResolverFactory resolverFactory) {
        register(new PropertyListBoxResolverUIControllerFactory(resolverFactory));
    }

    public void register(SingletonVisualItemResolverFactory resolverFactory) {
        register(new EmptyWidgetResolverUIControllerFactory(resolverFactory));
    }

}
