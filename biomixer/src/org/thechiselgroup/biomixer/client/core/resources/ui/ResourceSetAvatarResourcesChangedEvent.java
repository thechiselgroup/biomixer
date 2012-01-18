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

import com.google.gwt.event.shared.GwtEvent;

public class ResourceSetAvatarResourcesChangedEvent extends
        GwtEvent<ResourceSetAvatarResourcesChangedEventHandler> {

    public static final GwtEvent.Type<ResourceSetAvatarResourcesChangedEventHandler> TYPE = new GwtEvent.Type<ResourceSetAvatarResourcesChangedEventHandler>();

    private final ResourceSetAvatar avatar;

    private final ResourceSet newResources;

    private final ResourceSet oldResources;

    public ResourceSetAvatarResourcesChangedEvent(ResourceSetAvatar avatar,
            ResourceSet newResources, ResourceSet oldResources) {

        this.avatar = avatar;
        this.newResources = newResources;
        this.oldResources = oldResources;
    }

    @Override
    protected void dispatch(
            ResourceSetAvatarResourcesChangedEventHandler handler) {
        handler.onResourcesChanged(this);
    }

    @Override
    public GwtEvent.Type<ResourceSetAvatarResourcesChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public ResourceSetAvatar getAvatar() {
        return avatar;
    }

    public ResourceSet getNewResources() {
        return newResources;
    }

    public ResourceSet getOldResources() {
        return oldResources;
    }

}