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

import org.thechiselgroup.biomixer.client.core.command.AsyncCommandExecutor;
import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.command.DefaultCommandManager;
import org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCommandExecutor;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingConstants;
import org.thechiselgroup.biomixer.client.core.error_handling.LoggerProvider;
import org.thechiselgroup.biomixer.client.core.error_handling.LoggingAsyncCommandExecutor;
import org.thechiselgroup.biomixer.client.core.error_handling.RootLoggerProvider;
import org.thechiselgroup.biomixer.client.core.importer.Importer;
import org.thechiselgroup.biomixer.client.core.label.CategoryLabelProvider;
import org.thechiselgroup.biomixer.client.core.label.LabelProvider;
import org.thechiselgroup.biomixer.client.core.label.MappingCategoryLabelProvider;
import org.thechiselgroup.biomixer.client.core.label.ResourceSetLabelFactory;
import org.thechiselgroup.biomixer.client.core.label.SelectionModelLabelFactory;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationServiceProvider;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ManagedResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.ResourceByUriTypeCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetContainer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.DefaultDetailsWidgetHelper;
import org.thechiselgroup.biomixer.client.core.resources.ui.DetailsWidgetHelper;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.core.ui.popup.DefaultPopupFactory;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupFactory;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManagerFactory;
import org.thechiselgroup.biomixer.client.core.ui.shade.ShadeManager;
import org.thechiselgroup.biomixer.client.core.util.DisposeUtil;
import org.thechiselgroup.biomixer.client.core.util.HandlerManagerProvider;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.core.visualization.DefaultViewAccessor;
import org.thechiselgroup.biomixer.client.core.visualization.ViewAccessor;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.initialization.ViewContentDisplaysConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.DefaultVisualItemResolverFactoryProvider;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.VisualItemValueResolverFactoryProvider;
import org.thechiselgroup.biomixer.client.core.visualization.model.persistence.ManagedSlotMappingConfigurationPersistence;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.ui.DefaultVisualItemResolverUIFactoryProvider;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.ui.VisualItemValueResolverUIControllerFactoryProvider;
import org.thechiselgroup.biomixer.client.dnd.popup.DragSupportingPopupManagerFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.AllSetDropTargetManager;
import org.thechiselgroup.biomixer.client.dnd.resources.DefaultDropTargetCapabilityChecker;
import org.thechiselgroup.biomixer.client.dnd.resources.DefaultResourceSetAvatarDragController;
import org.thechiselgroup.biomixer.client.dnd.resources.DragEnablerFactory;
import org.thechiselgroup.biomixer.client.dnd.resources.DropTargetCapabilityChecker;
import org.thechiselgroup.biomixer.client.dnd.resources.NullResourceSetAvatarDropTargetManager;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDragController;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDropTargetManager;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetDropTargetManager;
import org.thechiselgroup.biomixer.client.dnd.resources.SelectionDropTargetFactoryProvider;
import org.thechiselgroup.biomixer.client.dnd.resources.SelectionDropTargetManager;
import org.thechiselgroup.biomixer.client.dnd.resources.ViewDisplayDropTargetManager;
import org.thechiselgroup.biomixer.client.dnd.windows.Branding;
import org.thechiselgroup.biomixer.client.dnd.windows.DefaultDesktop;
import org.thechiselgroup.biomixer.client.dnd.windows.Desktop;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationManager;
import org.thechiselgroup.biomixer.client.workbench.authentication.DefaultAuthenticationManager;
import org.thechiselgroup.biomixer.client.workbench.client.command.ui.CommandPresenterFactory;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedInitializer;
import org.thechiselgroup.biomixer.client.workbench.error_handling.WorkbenchErrorHandlerProvider;
import org.thechiselgroup.biomixer.client.workbench.init.ApplicationInitializer;
import org.thechiselgroup.biomixer.client.workbench.init.ChooselApplicationInitializer;
import org.thechiselgroup.biomixer.client.workbench.init.DefaultWindowLocation;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;
import org.thechiselgroup.biomixer.client.workbench.init.WorkbenchInitializer;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.AllResourceSetAvatarFactoryProvider;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.DefaultResourceSetAvatarFactoryProvider;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ResourceSetsDragAvatarFactoryProvider;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.SelectionDragAvatarFactoryProvider;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;
import org.thechiselgroup.biomixer.client.workbench.ui.messages.DefaultMessageManager;
import org.thechiselgroup.biomixer.client.workbench.ui.messages.MessageBlockingCommandExecutor;
import org.thechiselgroup.biomixer.client.workbench.ui.messages.MessageManager;
import org.thechiselgroup.biomixer.client.workbench.ui.messages.ShadeMessageManager;
import org.thechiselgroup.biomixer.client.workbench.util.url.FlashUrlFetchService;
import org.thechiselgroup.biomixer.client.workbench.util.xml.SarissaDocumentProcessor;
import org.thechiselgroup.biomixer.client.workbench.workspace.DefaultShareConfigurationFactory;
import org.thechiselgroup.biomixer.client.workbench.workspace.DefaultViewLoadManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.DefaultViewSaveManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.DefaultViewSaver;
import org.thechiselgroup.biomixer.client.workbench.workspace.DefaultWorkspaceManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.DefaultWorkspacePersistenceManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.ShareConfigurationFactory;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewLoadManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewLoader;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewSaveManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewSaver;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspaceManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePersistenceManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePresenter;
import org.thechiselgroup.biomixer.client.workbench.workspace.command.ConfigureSharedViewsDialogCommand;
import org.thechiselgroup.biomixer.client.workbench.workspace.command.LoadWorkspaceDialogCommand;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

// TODO rename to setup
public abstract class ChooselWorkbenchClientModule extends AbstractGinModule
        implements ChooselInjectionConstants {

    private void bindBranding() {
        bind(Branding.class).to(getBrandingClass()).in(Singleton.class);
    }

    protected void bindCustomServices() {
    }

    private void bindDisplays() {
        bind(WorkspacePresenter.WorkspacePresenterDisplay.class).to(
                WorkspacePresenter.DefaultWorkspacePresenterDisplay.class);
        bind(LoadWorkspaceDialogCommand.DetailsDisplay.class).to(
                LoadWorkspaceDialogCommand.DefaultDetailsDisplay.class);
        bind(ConfigureSharedViewsDialogCommand.DetailsDisplay.class).to(
                ConfigureSharedViewsDialogCommand.DefaultDetailsDisplay.class);
    }

    private void bindDragAvatarDropTargetManagers() {
        bind(ResourceSetAvatarDropTargetManager.class).to(
                NullResourceSetAvatarDropTargetManager.class).in(
                Singleton.class);

        bind(ResourceSetAvatarDropTargetManager.class)
                .annotatedWith(Names.named(AVATAR_FACTORY_SELECTION))
                .to(SelectionDropTargetManager.class).in(Singleton.class);

        bind(ResourceSetAvatarDropTargetManager.class)
                .annotatedWith(Names.named(AVATAR_FACTORY_SET))
                .to(ResourceSetDropTargetManager.class).in(Singleton.class);

        bind(ResourceSetAvatarDropTargetManager.class)
                .annotatedWith(Names.named(AVATAR_FACTORY_ALL_RESOURCES))
                .to(AllSetDropTargetManager.class).in(Singleton.class);

        bind(ResourceSetAvatarDropTargetManager.class)
                .annotatedWith(Names.named(DROP_TARGET_MANAGER_VIEW_CONTENT))
                .to(ViewDisplayDropTargetManager.class).in(Singleton.class);
    }

    private void bindDragAvatarFactories() {
        bind(ResourceSetAvatarFactory.class).toProvider(
                DefaultResourceSetAvatarFactoryProvider.class).in(
                Singleton.class);
        bind(ResourceSetAvatarFactory.class)
                .annotatedWith(Names.named(AVATAR_FACTORY_SET))
                .toProvider(ResourceSetsDragAvatarFactoryProvider.class)
                .in(Singleton.class);
        bind(ResourceSetAvatarFactory.class)
                .annotatedWith(Names.named(AVATAR_FACTORY_ALL_RESOURCES))
                .toProvider(AllResourceSetAvatarFactoryProvider.class)
                .in(Singleton.class);
        bind(ResourceSetAvatarFactory.class)
                .annotatedWith(Names.named(AVATAR_FACTORY_SELECTION))
                .toProvider(SelectionDragAvatarFactoryProvider.class)
                .in(Singleton.class);
        bind(ResourceSetAvatarFactory.class)
                .annotatedWith(Names.named(AVATAR_FACTORY_SELECTION_DROP))
                .toProvider(SelectionDropTargetFactoryProvider.class)
                .in(Singleton.class);

        bind(ResourceSetContainer.class)
                .annotatedWith(Names.named(DATA_SOURCES))
                .to(ResourceSetContainer.class).in(Singleton.class);
    }

    protected void bindErrorHandler() {
        bind(ErrorHandler.class)
                .toProvider(WorkbenchErrorHandlerProvider.class).in(
                        Singleton.class);
    }

    private void bindHoverModel() {
        bind(HighlightingModel.class).in(Singleton.class);
    }

    private void bindLabelProviders() {
        bind(LabelProvider.class)
                .annotatedWith(Names.named(LABEL_PROVIDER_SELECTION_SET))
                .to(SelectionModelLabelFactory.class).in(Singleton.class);
        bind(LabelProvider.class)
                .annotatedWith(Names.named(LABEL_PROVIDER_RESOURCE_SET))
                .to(ResourceSetLabelFactory.class).in(Singleton.class);
    }

    /**
     * We use the @Named(LOG) command executor to prevent infinite loops when
     * the feedback dialog throws errors.
     */
    protected void bindLoggingCommandExecutor() {
        bind(AsyncCommandExecutor.class)
                .annotatedWith(Names.named(ErrorHandlingConstants.LOG))
                .to(LoggingAsyncCommandExecutor.class).in(Singleton.class);
    }

    protected void bindUrlFetchService() {
        bind(UrlFetchService.class).to(FlashUrlFetchService.class).in(
                Singleton.class);
    }

    protected void bindWindowContentProducer() {
        bind(ViewWindowContentProducer.class).to(
                ChooselWorkbenchViewWindowContentProducer.class).in(
                Singleton.class);
        bind(WindowContentProducer.class).toProvider(
                DefaultWindowContentProducerProvider.class).in(Singleton.class);
    }

    @Override
    protected void configure() {
        configureGwtApiWrappers();
        configureLogging();

        bind(CommandManager.class).to(DefaultCommandManager.class).in(
                Singleton.class);
        bind(Desktop.class).to(DefaultDesktop.class).in(Singleton.class);
        bind(ResourceSetAvatarDragController.class).to(
                DefaultResourceSetAvatarDragController.class).in(
                Singleton.class);
        bind(ViewAccessor.class).to(DefaultViewAccessor.class).in(
                Singleton.class);

        bind(AuthenticationManager.class).to(getAuthenticationManagerClass())
                .in(Singleton.class);

        bind(Importer.class).in(Singleton.class);

        bind(ShadeManager.class).in(Singleton.class);
        bind(DialogManager.class).in(Singleton.class);
        bindErrorHandler();
        bind(DisposeUtil.class).in(Singleton.class);
        bind(MessageManager.class).annotatedWith(Names.named(DEFAULT))
                .to(DefaultMessageManager.class).in(Singleton.class);
        bind(MessageManager.class).to(ShadeMessageManager.class).in(
                Singleton.class);
        bind(AsyncCommandExecutor.class).annotatedWith(Names.named(DEFAULT))
                .to(ErrorHandlingAsyncCommandExecutor.class)
                .in(Singleton.class);

        bindLoggingCommandExecutor();

        bind(AsyncCommandExecutor.class).to(
                MessageBlockingCommandExecutor.class).in(Singleton.class);
        bind(CommandPresenterFactory.class).in(Singleton.class);

        configurePopups();

        bind(AbsolutePanel.class).annotatedWith(Names.named(ROOT_PANEL))
                .toProvider(RootPanelProvider.class).in(Singleton.class);

        bind(ViewContentDisplaysConfiguration.class).toProvider(
                getViewContentDisplaysConfigurationProvider()).in(
                Singleton.class);

        bindWindowContentProducer();

        bind(ViewSaveManager.class).to(DefaultViewSaveManager.class).in(
                Singleton.class);
        bind(ViewSaver.class).to(DefaultViewSaver.class).in(Singleton.class);
        bind(ViewLoadManager.class).to(DefaultViewLoadManager.class).in(
                Singleton.class);
        bind(ViewLoader.class).to(DefaultViewLoadManager.class).in(
                Singleton.class);

        bind(ShareConfigurationFactory.class).to(
                DefaultShareConfigurationFactory.class).in(Singleton.class);

        bindDragAvatarDropTargetManagers();
        bindDragAvatarFactories();

        bind(DetailsWidgetHelper.class).to(getDetailsWidgetHelperClass()).in(
                Singleton.class);

        bind(HandlerManager.class).toProvider(HandlerManagerProvider.class).in(
                Singleton.class);

        bind(DragEnablerFactory.class).in(Singleton.class);

        bindLabelProviders();

        bind(SelectionModelLabelFactory.class).in(Singleton.class);

        bind(WorkspaceManager.class).to(DefaultWorkspaceManager.class).in(
                Singleton.class);
        bind(WorkspacePersistenceManager.class).to(
                DefaultWorkspacePersistenceManager.class).in(Singleton.class);

        bind(ResourceManager.class).to(DefaultResourceManager.class).in(
                Singleton.class);
        bind(ResourceSetFactory.class).to(ManagedResourceSetFactory.class).in(
                Singleton.class);

        bind(PersistableRestorationService.class).toProvider(
                getPersistableRestorationServiceProvider()).in(Singleton.class);

        bind(ManagedSlotMappingConfigurationPersistence.class).toProvider(
                getManagedSlotMappingConfigurationPersistenceProvider()).in(
                Singleton.class);

        bindHoverModel();

        bind(ResourceCategorizer.class).to(getResourceCategorizerClass()).in(
                Singleton.class);
        bind(CategoryLabelProvider.class).to(getCategoryLabelProviderClass())
                .in(Singleton.class);
        bind(DropTargetCapabilityChecker.class).to(
                getDropTargetCapabilityCheckerClass()).in(Singleton.class);

        bind(VisualItemValueResolverFactoryProvider.class).to(
                getResolverFactoryProviderClass()).in(Singleton.class);

        bind(VisualItemValueResolverUIControllerFactoryProvider.class).to(
                getResolverFactoryUIProviderClass()).in(Singleton.class);

        bindDisplays();

        bind(DocumentProcessor.class).to(SarissaDocumentProcessor.class).in(
                Singleton.class);
        bindUrlFetchService();

        bindBranding();
        bindCustomServices();
        configureApplicationInitializers();
    }

    protected void configureApplicationInitializers() {
        bind(ApplicationInitializer.class).to(
                ChooselApplicationInitializer.class).in(Singleton.class);

        bind(ApplicationInitializer.class)
                .annotatedWith(
                        Names.named(ChooselApplicationInitializer.WORKBENCH))
                .to(getWorkbenchInitializer()).in(Singleton.class);

        bind(ApplicationInitializer.class)
                .annotatedWith(Names.named(ChooselApplicationInitializer.EMBED))
                .to(getEmbedInitializer()).in(Singleton.class);
    }

    /**
     * GWT API wrappers are object oriented wrappers around static methods in
     * the GWT API. They faciliate testing with mock objects.
     */
    protected void configureGwtApiWrappers() {
        bind(WindowLocation.class).to(DefaultWindowLocation.class).in(
                Singleton.class);
    }

    protected void configureLogging() {
        bind(LoggerProvider.class).toProvider(RootLoggerProvider.class).in(
                Singleton.class);
    }

    protected void configurePopups() {
        bind(PopupFactory.class).to(DefaultPopupFactory.class).in(
                Singleton.class);
        bind(PopupManagerFactory.class).to(
                DragSupportingPopupManagerFactory.class).in(Singleton.class);
    }

    /**
     * Applications can override this method to provide a custom authentication
     * manager.
     */
    protected Class<? extends AuthenticationManager> getAuthenticationManagerClass() {
        return DefaultAuthenticationManager.class;
    }

    protected Class<? extends Branding> getBrandingClass() {
        return DefaultBranding.class;
    }

    protected Class<? extends CategoryLabelProvider> getCategoryLabelProviderClass() {
        return MappingCategoryLabelProvider.class;
    }

    protected Class<? extends DetailsWidgetHelper> getDetailsWidgetHelperClass() {
        return DefaultDetailsWidgetHelper.class;
    }

    protected Class<? extends DropTargetCapabilityChecker> getDropTargetCapabilityCheckerClass() {
        return DefaultDropTargetCapabilityChecker.class;
    }

    protected Class<? extends EmbedInitializer> getEmbedInitializer() {
        return EmbedInitializer.class;
    }

    protected Class<? extends Provider<ManagedSlotMappingConfigurationPersistence>> getManagedSlotMappingConfigurationPersistenceProvider() {
        return ManagedSlotMappingConfigurationPersistenceProvider.class;
    }

    protected Class<? extends PersistableRestorationServiceProvider> getPersistableRestorationServiceProvider() {
        return PersistableRestorationServiceProvider.class;
    }

    protected Class<? extends VisualItemValueResolverFactoryProvider> getResolverFactoryProviderClass() {
        return DefaultVisualItemResolverFactoryProvider.class;
    }

    protected Class<? extends VisualItemValueResolverUIControllerFactoryProvider> getResolverFactoryUIProviderClass() {
        return DefaultVisualItemResolverUIFactoryProvider.class;
    }

    protected Class<? extends ResourceCategorizer> getResourceCategorizerClass() {
        return ResourceByUriTypeCategorizer.class;
    }

    protected abstract Class<? extends Provider<ViewContentDisplaysConfiguration>> getViewContentDisplaysConfigurationProvider();

    protected Class<? extends WorkbenchInitializer> getWorkbenchInitializer() {
        return WorkbenchInitializer.class;
    }

}