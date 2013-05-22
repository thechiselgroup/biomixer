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

import org.thechiselgroup.biomixer.client.core.label.CategoryLabelProvider;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationServiceProvider;
import org.thechiselgroup.biomixer.client.core.util.date.GwtDateTimeFormatFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.core.visualization.model.initialization.ViewContentDisplaysConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.VisualItemValueResolverFactoryProvider;
import org.thechiselgroup.biomixer.client.core.visualization.model.persistence.ManagedSlotMappingConfigurationPersistence;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.ui.VisualItemValueResolverUIControllerFactoryProvider;
import org.thechiselgroup.biomixer.client.dnd.windows.Branding;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.embeds.BioMixerEmbedInitializer;
import org.thechiselgroup.biomixer.client.graph.BioMixerArcTypeProvider;
import org.thechiselgroup.biomixer.client.services.hierarchy.HierarchyPathServiceAsync;
import org.thechiselgroup.biomixer.client.services.hierarchy.HierarchyPathServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.mapping.ConceptMappingServiceAsync;
import org.thechiselgroup.biomixer.client.services.mapping.ConceptMappingServiceImplementation;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyNameServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyNameServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyStatusServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyStatusServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyVersionServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyVersionServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCountServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCountServiceAsyncImplementation;
import org.thechiselgroup.biomixer.client.services.search.concept.ConceptSearchServiceAsync;
import org.thechiselgroup.biomixer.client.services.search.concept.ConceptSearchServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologyMetricServiceAsync;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologyMetricServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologySearchServiceAsync;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologySearchServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsyncClientImplementation;
import org.thechiselgroup.biomixer.client.services.term.LightTermResponseWithoutRelationshipsParser;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceImplementation;
import org.thechiselgroup.biomixer.client.servicesnewapi.NcboApiStageDataUrlBuilderFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcTypeProvider;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphExpansionRegistryFactory;
import org.thechiselgroup.biomixer.client.workbench.ChooselWorkbenchClientModule;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedInitializer;
import org.thechiselgroup.biomixer.client.workbench.init.WorkbenchInitializer;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;
import org.thechiselgroup.biomixer.client.workbench.util.url.JsonpUrlFetchService;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormatFactory;

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
        bind(GraphExpansionRegistryFactory.class).in(Singleton.class);

        // other bindings
        bind(ConceptSearchServiceAsync.class).to(
                ConceptSearchServiceAsyncClientImplementation.class).in(
                Singleton.class);
        bind(OntologySearchServiceAsync.class).to(
                OntologySearchServiceAsyncClientImplementation.class).in(
                Singleton.class);
        bind(ConceptNeighbourhoodServiceAsync.class).to(
                ConceptNeighbourhoodServiceAsyncClientImplementation.class).in(
                Singleton.class);

        bind(OntologyMetricServiceAsync.class).to(
                OntologyMetricServiceAsyncClientImplementation.class).in(
                Singleton.class);

        bind(ConceptMappingServiceAsync.class).to(
                ConceptMappingServiceImplementation.class).in(Singleton.class);

        bind(TermServiceAsync.class).to(TermServiceImplementation.class).in(
                Singleton.class);
        bind(DateTimeFormatFactory.class).to(GwtDateTimeFormatFactory.class)
                .in(Singleton.class);
        bind(LightTermResponseWithoutRelationshipsParser.class).in(
                Singleton.class);

        bind(HierarchyPathServiceAsync.class).to(
                HierarchyPathServiceAsyncClientImplementation.class).in(
                Singleton.class);

        bind(OntologyNameServiceAsync.class).to(
                OntologyNameServiceAsyncClientImplementation.class).in(
                Singleton.class);

        bind(OntologyVersionServiceAsync.class).to(
                OntologyVersionServiceAsyncClientImplementation.class).in(
                Singleton.class);

        bind(OntologyStatusServiceAsync.class).to(
                OntologyStatusServiceAsyncClientImplementation.class).in(
                Singleton.class);

        bind(OntologyMappingCountServiceAsync.class).to(
                OntologyMappingCountServiceAsyncImplementation.class).in(
                Singleton.class);

        bind(UrlBuilderFactory.class).to(
                NcboApiStageDataUrlBuilderFactory.class).in(Singleton.class);
    }

    @Override
    protected void bindUrlFetchService() {
        bind(UrlFetchService.class).annotatedWith(Names.named("delegate"))
                .to(JsonpUrlFetchService.class).in(Singleton.class);
        bind(UrlFetchService.class).to(JsonpUrlFetchService.class).in(
                Singleton.class);
    }

    @Override
    protected void bindWindowContentProducer() {
        bind(ViewWindowContentProducer.class).to(
                BioMixerViewWindowContentProducer.class).in(Singleton.class);
        bind(WindowContentProducer.class).toProvider(
                BioMixerWindowContentProducerProvider.class)
                .in(Singleton.class);

        bind(ViewWindowContentProducer.class)
                .annotatedWith(Names.named("embed"))
                .to(BioMixerEmbedViewWindowContentProducer.class)
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

    // @Override
    // protected Class<? extends DetailsWidgetHelper<VisualItem>>
    // getDetailsWidgetHelperClass() {
    // return BioMixerDetailsWidgetHelper.class;
    // }

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