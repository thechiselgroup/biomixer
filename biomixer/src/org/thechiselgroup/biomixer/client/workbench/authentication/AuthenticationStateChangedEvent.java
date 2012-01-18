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
package org.thechiselgroup.biomixer.client.workbench.authentication;

import com.google.gwt.event.shared.GwtEvent;

public class AuthenticationStateChangedEvent extends
        GwtEvent<AuthenticationStateChangedEventHandler> {

    public static final Type<AuthenticationStateChangedEventHandler> TYPE = new Type<AuthenticationStateChangedEventHandler>();

    private final AuthenticationState authenticationState;

    private final AuthenticationManager manager;

    public AuthenticationStateChangedEvent(AuthenticationManager manager,
            AuthenticationState authenticationState) {
        this.manager = manager;
        this.authenticationState = authenticationState;
    }

    @Override
    protected void dispatch(AuthenticationStateChangedEventHandler handler) {
        handler.onAuthenticationStateChanged(this);
    }

    @Override
    public Type<AuthenticationStateChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public AuthenticationState getAuthenticationState() {
        return authenticationState;
    }

    public AuthenticationManager getManager() {
        return manager;
    }

}