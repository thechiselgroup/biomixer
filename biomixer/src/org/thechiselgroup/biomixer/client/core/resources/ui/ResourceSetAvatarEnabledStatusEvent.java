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

import com.google.gwt.event.shared.GwtEvent;

public class ResourceSetAvatarEnabledStatusEvent extends
        GwtEvent<ResourceSetAvatarEnabledStatusEventHandler> {

    public static final GwtEvent.Type<ResourceSetAvatarEnabledStatusEventHandler> TYPE = new GwtEvent.Type<ResourceSetAvatarEnabledStatusEventHandler>();

    private final ResourceSetAvatar avatar;

    public ResourceSetAvatarEnabledStatusEvent(ResourceSetAvatar avatar) {
        this.avatar = avatar;
    }

    @Override
    protected void dispatch(ResourceSetAvatarEnabledStatusEventHandler handler) {
        handler.onDragAvatarEnabledStatusChange(this);
    }

    @Override
    public GwtEvent.Type<ResourceSetAvatarEnabledStatusEventHandler> getAssociatedType() {
        return TYPE;
    }

    public ResourceSetAvatar getAvatar() {
        return avatar;
    }

}