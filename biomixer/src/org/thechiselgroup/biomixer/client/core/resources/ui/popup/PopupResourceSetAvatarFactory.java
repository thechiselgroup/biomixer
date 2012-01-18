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
package org.thechiselgroup.biomixer.client.core.resources.ui.popup;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ui.DelegatingResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarEnabledStatusEvent;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarEnabledStatusEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.popup.ResourceSetAvatarPopupWidgetFactory.HeaderUpdatedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ui.popup.ResourceSetAvatarPopupWidgetFactory.ResourceSetAvatarPopupWidgetFactoryAction;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManager;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManagerFactory;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewAccessor;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

public class PopupResourceSetAvatarFactory extends
        DelegatingResourceSetAvatarFactory {

    public static interface Action {

        void execute(ResourceSet resources, View view);

        String getLabel();

    }

    public static class ActionToDragAvatarPopupWidgetFactoryActionAdapter
            implements ResourceSetAvatarPopupWidgetFactoryAction {

        private ResourceSetAvatar avatar;

        private Action delegate;

        private PopupManager popupManager;

        private ViewAccessor viewAccessor;

        public ActionToDragAvatarPopupWidgetFactoryActionAdapter(
                ViewAccessor viewAccessor, ResourceSetAvatar avatar,
                Action delegate) {

            this.viewAccessor = viewAccessor;
            this.avatar = avatar;
            this.delegate = delegate;
        }

        @Override
        public void execute() {
            delegate.execute(avatar.getResourceSet(),
                    viewAccessor.findView(avatar));
            popupManager.hidePopup();
        }

        @Override
        public String getLabel() {
            return delegate.getLabel();
        }

        public void setPopupManager(PopupManager popupManager) {
            this.popupManager = popupManager;
        }
    }

    private List<Action> actions;

    private String infoText;

    private final PopupManagerFactory popupManagerFactory;

    private final boolean resourceLabelModifiable;

    private String subHeaderText;

    private final ViewAccessor viewAccessor;

    public PopupResourceSetAvatarFactory(ResourceSetAvatarFactory delegate,
            ViewAccessor viewAccessor, PopupManagerFactory popupManagerFactory,
            List<Action> actions, String subHeaderText, String infoText,
            boolean resourceLabelModifiable) {

        super(delegate);

        this.viewAccessor = viewAccessor;
        this.popupManagerFactory = popupManagerFactory;
        this.infoText = infoText;
        this.actions = actions;
        this.subHeaderText = subHeaderText;
        this.resourceLabelModifiable = resourceLabelModifiable;
    }

    @Override
    public ResourceSetAvatar createAvatar(final ResourceSet resources) {
        final ResourceSetAvatar avatar = delegate.createAvatar(resources);

        List<ResourceSetAvatarPopupWidgetFactoryAction> actionAdapters = new ArrayList<ResourceSetAvatarPopupWidgetFactoryAction>();
        for (Action action : actions) {
            actionAdapters
                    .add(new ActionToDragAvatarPopupWidgetFactoryActionAdapter(
                            viewAccessor, avatar, action));
        }

        final PopupManager popupManager = popupManagerFactory
                .createPopupManager(createWidget(resources, avatar,
                        actionAdapters));

        for (ResourceSetAvatarPopupWidgetFactoryAction action : actionAdapters) {
            ((ActionToDragAvatarPopupWidgetFactoryActionAdapter) action)
                    .setPopupManager(popupManager);
        }

        final HandlerRegistration link = popupManager.linkToWidget(avatar);

        popupManager.setEnabled(avatar.isEnabled());
        final HandlerRegistration handlerRegistration = avatar
                .addEnabledStatusHandler(new ResourceSetAvatarEnabledStatusEventHandler() {
                    @Override
                    public void onDragAvatarEnabledStatusChange(
                            ResourceSetAvatarEnabledStatusEvent event) {
                        popupManager.setEnabled(avatar.isEnabled());
                    }
                });

        avatar.addDisposable(new Disposable() {
            @Override
            public void dispose() {
                handlerRegistration.removeHandler();
                link.removeHandler();
            }
        });

        return avatar;
    }

    protected Widget createWidget(final ResourceSet resources,
            final ResourceSetAvatar avatar,
            List<ResourceSetAvatarPopupWidgetFactoryAction> actionAdapters) {

        // TODO refactor - is a widgetFactory really required here?
        ResourceSetAvatarPopupWidgetFactory widgetFactory = new ResourceSetAvatarPopupWidgetFactory(
                avatar.getText(), subHeaderText, actionAdapters, infoText,
                resourceLabelModifiable ? new HeaderUpdatedEventHandler() {
                    @Override
                    public void headerLabelChanged(String newLabel) {
                        resources.setLabel(newLabel);
                    }
                } : null);

        return widgetFactory.createWidget();
    }
}
