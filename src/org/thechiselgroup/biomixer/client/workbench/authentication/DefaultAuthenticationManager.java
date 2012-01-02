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

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DefaultAuthenticationManager implements AuthenticationManager {

    private Authentication authentication = null;

    private AuthenticationServiceAsync authenticationService;

    private Throwable failure;

    private HandlerManager handlerManager = new HandlerManager(this);

    private AuthenticationState state = AuthenticationState.UNKNOWN;

    @Inject
    public DefaultAuthenticationManager(
            AuthenticationServiceAsync authenticationService) {

        assert authenticationService != null;

        this.authenticationService = authenticationService;

        init();
    }

    @Override
    public HandlerRegistration addAuthenticationStateChangedEventHandler(
            AuthenticationStateChangedEventHandler handler) {

        assert handler != null;

        return handlerManager.addHandler(AuthenticationStateChangedEvent.TYPE,
                handler);
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public AuthenticationState getAuthenticationState() {
        return state;
    }

    @Override
    public Throwable getFailure() {
        return failure;
    }

    private void init() {
        // TODO extract static call to Window.Location.Href
        authenticationService.getAuthentication(Window.Location.getHref(),
                new AsyncCallback<Authentication>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        failure = caught;
                        setState(AuthenticationState.FAILED);
                    }

                    @Override
                    public void onSuccess(Authentication result) {
                        authentication = result;

                        if (result.isSignedIn()) {
                            setState(AuthenticationState.SIGNED_IN);
                        } else {
                            setState(AuthenticationState.SIGNED_OUT);
                        }
                    }
                });

        setState(AuthenticationState.WAITING);
    }

    @Override
    public boolean isAuthenticated() {
        return getAuthenticationState() == AuthenticationState.SIGNED_IN;
    }

    private void setState(AuthenticationState newState) {
        if (newState == state) {
            return;
        }

        state = newState;

        handlerManager.fireEvent(new AuthenticationStateChangedEvent(this,
                newState));
    }
}
