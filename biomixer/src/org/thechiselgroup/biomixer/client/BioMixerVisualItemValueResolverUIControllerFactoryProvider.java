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
package org.thechiselgroup.biomixer.client;

import static org.thechiselgroup.biomixer.client.BioMixerVisualItemValueResolverFactoryProvider.CONCEPT_BY_ONTOLOGY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.BioMixerVisualItemValueResolverFactoryProvider.GRAPH_LABEL_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.BioMixerVisualItemValueResolverFactoryProvider.NODE_BACKGROUND_COLOR_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.BioMixerVisualItemValueResolverFactoryProvider.NODE_BORDER_COLOR_RESOLVER_FACTORY;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemResolverUIFactoryProvider;

import com.google.inject.Inject;

public class BioMixerVisualItemValueResolverUIControllerFactoryProvider extends
        WorkbenchVisualItemResolverUIFactoryProvider {

    @Inject
    public BioMixerVisualItemValueResolverUIControllerFactoryProvider(
            ErrorHandler errorHandler) {
        super(errorHandler);
    }

    @Override
    public void registerFactories() {
        super.registerFactories();

        register(GRAPH_LABEL_RESOLVER_FACTORY);
        register(NODE_BACKGROUND_COLOR_RESOLVER_FACTORY);
        register(NODE_BORDER_COLOR_RESOLVER_FACTORY);
        register(CONCEPT_BY_ONTOLOGY_RESOLVER_FACTORY);
    }
}
