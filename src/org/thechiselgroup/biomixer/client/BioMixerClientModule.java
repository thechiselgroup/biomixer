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
package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.dnd.windows.Branding;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.graph.BioMixerArcTypeProvider;
import org.thechiselgroup.biomixer.client.graph.BioMixerGraphExpansionRegistry;
import org.thechiselgroup.biomixer.client.services.NcboRestUrlBuilderFactory;
import org.thechiselgroup.biomixer.client.services.mapping.MappingServiceAsync;
import org.thechiselgroup.biomixer.client.services.mapping.MappingServiceImplementation;
import org.thechiselgroup.biomixer.client.services.search.ConceptSearchServiceAsync;
import org.thechiselgroup.biomixer.client.services.search.ConceptSearchServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.term.LightTermResponseWithoutRelationshipsParser;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceImplementation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcTypeProvider;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphExpansionRegistry;
import org.thechiselgroup.biomixer.client.workbench.ChooselWorkbenchClientModule;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedInitializer;
import org.thechiselgroup.biomixer.client.workbench.init.WorkbenchInitializer;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;
import org.thechiselgroup.biomixer.client.workbench.util.url.FlashUrlFetchService;
import org.thechiselgroup.choosel.core.client.label.CategoryLabelProvider;
import org.thechiselgroup.choosel.core.client.persistence.PersistableRestorationServiceProvider;
import org.thechiselgroup.choosel.core.client.resources.ui.DetailsWidgetHelper;
import org.thechiselgroup.choosel.core.client.util.date.GwtDateTimeFormatFactory;
import org.thechiselgroup.choosel.core.client.util.url.ProfilingUrlFetchServiceDecorator;
import org.thechiselgroup.choosel.core.client.util.url.UrlBuilderFactory;
import org.thechiselgroup.choosel.core.client.util.url.UrlFetchService;
import org.thechiselgroup.choosel.core.client.visualization.model.initialization.ViewContentDisplaysConfiguration;
import org.thechiselgroup.choosel.core.client.visualization.model.managed.VisualItemValueResolverFactoryProvider;
import org.thechiselgroup.choosel.core.client.visualization.model.persistence.ManagedSlotMappingConfigurationPersistence;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.ui.VisualItemValueResolverUIControllerFactoryProvider;
import org.thechiselgroup.choosel.core.shared.util.date.DateTimeFormatFactory;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class BioMixerClientModule extends ChooselWorkbenchClientModule {

    // TODO organize
    @Override
    protected void bindCustomServices() {
        // Graph visualization bindings
        bind(ArcTypeProvider.class).to(BioMixerArcTypeProvider.class).in(
                Singleton.class);
        bind(GraphExpansionRegistry.class).to(
                BioMixerGraphExpansionRegistry.class).in(Singleton.class);

        // other bindings
        bind(ConceptSearchServiceAsync.class).to(
                ConceptSearchServiceAsyncClientImplementation.class).in(
                Singleton.class);
        bind(ConceptNeighbourhoodServiceAsync.class).to(
                ConceptNeighbourhoodServiceAsyncClientImplementation.class).in(
                Singleton.class);

        // bind(MappingServiceAsync.class).to(FakeMappingService.class).in(
        // Singleton.class);
        bind(MappingServiceAsync.class).to(MappingServiceImplementation.class)
                .in(Singleton.class);

        bind(TermServiceAsync.class).to(TermServiceImplementation.class).in(
                Singleton.class);
        bind(DateTimeFormatFactory.class).to(GwtDateTimeFormatFactory.class)
                .in(Singleton.class);
        bind(LightTermResponseWithoutRelationshipsParser.class).in(
                Singleton.class);

        bind(UrlBuilderFactory.class).to(NcboRestUrlBuilderFactory.class).in(
                Singleton.class);
    }

    @Override
    protected void bindUrlFetchService() {
        bind(UrlFetchService.class).annotatedWith(Names.named("delegate"))
                .to(FlashUrlFetchService.class).in(Singleton.class);
        bind(UrlFetchService.class).to(ProfilingUrlFetchServiceDecorator.class)
                .in(Singleton.class);
    }

    @Override
    protected void bindWindowContentProducer() {
        bind(ViewWindowContentProducer.class).to(
                BioMixerViewWindowContentProducerProvider.class).in(
                Singleton.class);
        bind(WindowContentProducer.class).toProvider(
                BioMixerWindowContentProducerProvider.class)
                .in(Singleton.class);
    }

    @Override
    protected Class<? extends Branding> getBrandingClass() {
        return BioMixerBranding.class;
    }

    @Override
    protected Class<? extends CategoryLabelProvider> getCategoryLabelProviderClass() {
        return BioMixerMappingCategoryLabelProvider.class;
    }

    @Override
    protected Class<? extends DetailsWidgetHelper> getDetailsWidgetHelperClass() {
        return BioMixerDetailsWidgetHelper.class;
    }

    @Override
    protected Class<? extends EmbedInitializer> getEmbedInitializer() {
        return BioMixerEmbedInitializer.class;
    }

    @Override
    protected Class<? extends Provider<ManagedSlotMappingConfigurationPersistence>> getManagedSlotMappingConfigurationPersistenceProvider() {
        return BioMixerManagedSlotMappingConfigurationPersistenceProvider.class;
    }

    @Override
    protected Class<? extends PersistableRestorationServiceProvider> getPersistableRestorationServiceProvider() {
        return BioMixerPersistableRestorationServiceProvider.class;
    }

    @Override
    protected Class<? extends VisualItemValueResolverFactoryProvider> getResolverFactoryProviderClass() {
        return BioMixerVisualItemValueResolverFactoryProvider.class;
    }

    @Override
    protected Class<? extends VisualItemValueResolverUIControllerFactoryProvider> getResolverFactoryUIProviderClass() {
        return BioMixerVisualItemValueResolverUIControllerFactoryProvider.class;
    }

    @Override
    protected Class<? extends Provider<ViewContentDisplaysConfiguration>> getViewContentDisplaysConfigurationProvider() {
        return BioMixerWorkbenchViewContentDisplaysConfigurationProvider.class;
    }

    @Override
    protected Class<? extends WorkbenchInitializer> getWorkbenchInitializer() {
        return BioMixerWorkbench.class;
    }

}