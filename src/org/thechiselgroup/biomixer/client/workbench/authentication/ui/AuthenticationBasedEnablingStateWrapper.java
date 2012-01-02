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
package org.thechiselgroup.biomixer.client.workbench.authentication.ui;

import org.thechiselgroup.biomixer.client.core.ui.HasEnabledState;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationManager;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationStateChangedEvent;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationStateChangedEventHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

/**
 * Enables / disables widgets based on the authentication state of the
 * application.
 */
public class AuthenticationBasedEnablingStateWrapper implements Disposable,
        HasEnabledState {

    private boolean authEnabled;

    private AuthenticationManager authenticationManager;

    private HandlerRegistration handlerRegistration;

    private HasEnabledState hasEnabledState;

    private boolean outsideEnabled = true;

    @Inject
    public AuthenticationBasedEnablingStateWrapper(
            AuthenticationManager authenticationManager,
            HasEnabledState hasEnabledState) {

        assert authenticationManager != null;
        assert hasEnabledState != null;

        this.authenticationManager = authenticationManager;
        this.hasEnabledState = hasEnabledState;
    }

    @Override
    public void dispose() {
        handlerRegistration.removeHandler();
    }

    public void init() {
        handlerRegistration = authenticationManager
                .addAuthenticationStateChangedEventHandler(new AuthenticationStateChangedEventHandler() {
                    @Override
                    public void onAuthenticationStateChanged(
                            AuthenticationStateChangedEvent event) {
                        updateEnabling();
                    }
                });

        updateEnabling();
    }

    @Override
    public boolean isEnabled() {
        return outsideEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        outsideEnabled = enabled;
        updateDelegateEnabling();
    }

    private void updateDelegateEnabling() {
        hasEnabledState.setEnabled(authEnabled && outsideEnabled);
    }

    private void updateEnabling() {
        authEnabled = authenticationManager.isAuthenticated();
        updateDelegateEnabling();
    }
}
