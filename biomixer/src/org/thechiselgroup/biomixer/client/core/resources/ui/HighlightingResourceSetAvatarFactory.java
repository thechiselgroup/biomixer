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
package org.thechiselgroup.biomixer.client.core.resources.ui;

import org.thechiselgroup.biomixer.client.core.resources.DelegatingResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetDelegateChangedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetDelegateChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.UnmodifiableResourceSet;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class HighlightingResourceSetAvatarFactory extends
        DelegatingResourceSetAvatarFactory {

    private HighlightingModel hoverModel;

    public HighlightingResourceSetAvatarFactory(
            ResourceSetAvatarFactory delegate, HighlightingModel hoverModel) {

        super(delegate);
        assert hoverModel != null;
        this.hoverModel = hoverModel;
    }

    protected void addToHover(ResourceSetAvatar avatar) {
        hoverModel.setHighlightedResourceSet(avatar.getResourceSet());
    }

    @Override
    public ResourceSetAvatar createAvatar(ResourceSet resources) {
        final ResourceSetAvatar avatar = delegate.createAvatar(resources);

        final HandlerRegistration mouseOverHandlerRegistration = avatar
                .addMouseOverHandler(new MouseOverHandler() {
                    @Override
                    public void onMouseOver(MouseOverEvent event) {
                        addToHover(avatar);
                    }
                });
        final HandlerRegistration mouseOutHandlerRegistration = avatar
                .addMouseOutHandler(new MouseOutHandler() {
                    @Override
                    public void onMouseOut(MouseOutEvent event) {
                        removeFromHover();
                    }
                });
        final HandlerRegistration containerChangedHandler = hoverModel
                .addEventHandler(new ResourceSetDelegateChangedEventHandler() {
                    @Override
                    public void onResourceSetContainerChanged(
                            ResourceSetDelegateChangedEvent event) {

                        avatar.setHover(shouldHighlight(avatar,
                                event.getResourceSet()));
                    }

                });

        avatar.addDisposable(new Disposable() {
            @Override
            public void dispose() {
                mouseOverHandlerRegistration.removeHandler();
                mouseOutHandlerRegistration.removeHandler();
                containerChangedHandler.removeHandler();
            }
        });

        return avatar;
    }

    protected void removeFromHover() {
        hoverModel.setHighlightedResourceSet(null);
    }

    protected boolean shouldHighlight(ResourceSetAvatar avatar,
            ResourceSet resources) {
        ResourceSet dragAvatarResources = avatar.getResourceSet();

        while (dragAvatarResources instanceof UnmodifiableResourceSet) {
            dragAvatarResources = ((DelegatingResourceSet) dragAvatarResources)
                    .getDelegate();
        }

        while (resources instanceof UnmodifiableResourceSet) {
            resources = ((DelegatingResourceSet) resources).getDelegate();
        }

        return resources == dragAvatarResources;
    }
}
