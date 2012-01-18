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

import org.thechiselgroup.biomixer.client.core.label.LabelChangedEvent;
import org.thechiselgroup.biomixer.client.core.label.LabelChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.Disposable;

import com.google.gwt.event.shared.HandlerRegistration;

public class UpdateResourceSetAvatarWhenLabelChangesManager implements
        LabelChangedEventHandler,
        ResourceSetAvatarResourcesChangedEventHandler, Disposable {

    private final ResourceSetAvatar avatar;

    private HandlerRegistration avatarChangeHandlerRegistration;

    private HandlerRegistration labelChangedHandlerRegistration;

    public UpdateResourceSetAvatarWhenLabelChangesManager(
            ResourceSetAvatar avatar) {
        assert avatar != null;
        this.avatar = avatar;
    }

    private void deregisterLabelChangedHandler() {
        labelChangedHandlerRegistration.removeHandler();
        labelChangedHandlerRegistration = null;
    }

    @Override
    public void dispose() {
        deregisterLabelChangedHandler();
        avatarChangeHandlerRegistration.removeHandler();
        avatarChangeHandlerRegistration = null;
    }

    private ResourceSet getResources() {
        return avatar.getResourceSet();
    }

    public void init() {
        avatarChangeHandlerRegistration = avatar
                .addResourceChangedHandler(this);
        registerResourceSetHandlers(getResources());
        avatar.addDisposable(this);
        updateAvatarState(getResources().getLabel());
    }

    @Override
    public void onLabelChanged(LabelChangedEvent event) {
        updateAvatarState(event.getNewLabel());
    }

    @Override
    public void onResourcesChanged(ResourceSetAvatarResourcesChangedEvent event) {
        deregisterLabelChangedHandler();
        registerResourceSetHandlers(event.getNewResources());
        updateAvatarState(event.getNewResources().getLabel());
    }

    private void registerResourceSetHandlers(ResourceSet resourceSet) {
        labelChangedHandlerRegistration = resourceSet
                .addLabelChangedEventHandler(this);
    }

    private void updateAvatarState(String label) {
        avatar.setText(label);
    }
}
