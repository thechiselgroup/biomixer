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
package org.thechiselgroup.biomixer.client.workbench;

import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.DATE_PROPERTY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_COLOR_STEELBLUE_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_DATE_TODAY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_NUMBER_0_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.FIXED_TEXT_EMPTY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.LOCATION_PROPERTY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.SUM_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.workbench.WorkbenchVisualItemValueResolverFactoryProvider.TEXT_PROPERTY_RESOLVER_FACTORY;

import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;
import org.thechiselgroup.biomixer.client.workbench.workspace.ShareConfigurationFactory;
import org.thechiselgroup.biomixer.client.workbench.workspace.ShareConfigurationViewPart;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightList;
import org.thechiselgroup.choosel.core.client.visualization.ViewPart;
import org.thechiselgroup.choosel.core.client.visualization.model.managed.DefaultSlotMappingInitializer;
import org.thechiselgroup.choosel.core.client.visualization.model.managed.SlotMappingInitializer;
import org.thechiselgroup.choosel.core.client.visualization.model.managed.VisualItemValueResolverFactoryProvider;

import com.google.inject.Inject;

public class ChooselWorkbenchViewWindowContentProducer extends
        ViewWindowContentProducer {

    @Inject
    protected VisualItemValueResolverFactoryProvider provider;

    @Inject
    private ShareConfigurationFactory shareConfigurationFactory;

    @Inject
    public VisualItemValueResolverFactoryProvider factoryProvider;

    /**
     * XXX This class relies on the factoryProvider that is defined in the
     * Choosel core package. I'm not sure if this is good or not.
     */
    @Override
    protected SlotMappingInitializer createSlotMappingInitializer(
            String contentType) {

        DefaultSlotMappingInitializer initializer = new DefaultSlotMappingInitializer();

        initializer.configureFixedResolver(FIXED_NUMBER_0_RESOLVER_FACTORY);
        initializer.configureFixedResolver(FIXED_TEXT_EMPTY_RESOLVER_FACTORY);
        initializer
                .configureFixedResolver(FIXED_COLOR_STEELBLUE_RESOLVER_FACTORY);
        initializer.configureFixedResolver(FIXED_DATE_TODAY_RESOLVER_FACTORY);

        initializer.configurePropertyResolver(SUM_RESOLVER_FACTORY);
        initializer.configurePropertyResolver(TEXT_PROPERTY_RESOLVER_FACTORY);
        initializer
                .configurePropertyResolver(LOCATION_PROPERTY_RESOLVER_FACTORY);
        initializer.configurePropertyResolver(DATE_PROPERTY_RESOLVER_FACTORY);

        return initializer;
    }

    @Override
    protected LightweightList<ViewPart> createViewParts(String contentType) {
        LightweightList<ViewPart> parts = super.createViewParts(contentType);

        parts.add(new ShareConfigurationViewPart(shareConfigurationFactory
                .createShareConfiguration()));

        return parts;
    }

}