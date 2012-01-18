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

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.util.Disposable;

import com.google.gwt.event.shared.HandlerRegistration;

public class DisableResourceSetAvatarIfEmptyManager implements
        ResourceSetChangedEventHandler,
        ResourceSetAvatarResourcesChangedEventHandler, Disposable {

    private HandlerRegistration handlerRegistration;

    private final ResourceSetAvatar avatar;

    private HandlerRegistration avatarChangeHandlerRegistration;

    public DisableResourceSetAvatarIfEmptyManager(ResourceSetAvatar avatar) {
        assert avatar != null;
        this.avatar = avatar;
    }

    private void deregisterResourceSetHandlers() {
        handlerRegistration.removeHandler();
        handlerRegistration = null;
    }

    @Override
    public void dispose() {
        deregisterResourceSetHandlers();
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
        updateAvatarState(getResources());
    }

    @Override
    public void onResourcesChanged(ResourceSetAvatarResourcesChangedEvent event) {
        deregisterResourceSetHandlers();
        registerResourceSetHandlers(event.getNewResources());
        updateAvatarState(event.getNewResources());
    }

    @Override
    public void onResourceSetChanged(ResourceSetChangedEvent event) {
        updateAvatarState(event.getTarget());
    }

    private void registerResourceSetHandlers(ResourceSet resourceSet) {
        handlerRegistration = resourceSet.addEventHandler(this);
    }

    private void updateAvatarState(ResourceSet resources) {
        avatar.setEnabled(!resources.isEmpty());
    }
}
