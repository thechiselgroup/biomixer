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

import org.thechiselgroup.choosel.core.client.visualization.resolvers.ui.DefaultVisualItemResolverUIFactoryProvider;

import com.google.inject.Inject;

public class WorkbenchVisualItemResolverUIFactoryProvider extends
        DefaultVisualItemResolverUIFactoryProvider {

    // TODO it might be possible to auto-map those based on type information
    @Inject
    public void registerFactories() {
        // id
        register(ID_RESOLVER_FACTORY);

        // calculations
        register(COUNT_RESOLVER_FACTORY);
        register(SUM_RESOLVER_FACTORY);
        register(AVERAGE_RESOLVER_FACTORY);
        register(MAX_RESOLVER_FACTORY);
        register(MIN_RESOLVER_FACTORY);

        // first property
        register(TEXT_PROPERTY_RESOLVER_FACTORY);
        register(LOCATION_PROPERTY_RESOLVER_FACTORY);
        register(DATE_PROPERTY_RESOLVER_FACTORY);

        // fixed
        register(FIXED_NUMBER_0_RESOLVER_FACTORY);
        register(FIXED_NUMBER_1_RESOLVER_FACTORY);
        register(FIXED_COLOR_STEELBLUE_RESOLVER_FACTORY);
        register(FIXED_DATE_TODAY_RESOLVER_FACTORY);
        register(FIXED_TEXT_EMPTY_RESOLVER_FACTORY);
    }

}
