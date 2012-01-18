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

import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationManager;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationState;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationStateChangedEvent;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationStateChangedEventHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

public class AuthenticationBar extends HTML {

    private static final String CSS_AUTHENTICATION = "authentication";

    private AuthenticationManager authenticationManager;

    private HandlerRegistration registration;

    @Inject
    public AuthenticationBar(AuthenticationManager authenticationManager) {
        assert authenticationManager != null;

        this.authenticationManager = authenticationManager;

        addStyleName(CSS_AUTHENTICATION);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        registration = authenticationManager
                .addAuthenticationStateChangedEventHandler(new AuthenticationStateChangedEventHandler() {
                    @Override
                    public void onAuthenticationStateChanged(
                            AuthenticationStateChangedEvent event) {
                        AuthenticationBar.this.update(event
                                .getAuthenticationState());
                    }
                });

        update(authenticationManager.getAuthenticationState());
    }

    @Override
    protected void onDetach() {
        registration.removeHandler();
        super.onDetach();
    }

    private void update(AuthenticationState state) {
        switch (state) {
        case UNKNOWN: {
            setHTML("Initializing authentication management...");
        }
            break;
        case WAITING: {
            setHTML("Waiting for authentication state...");
        }
            break;
        case SIGNED_OUT: {
            setHTML("<a href='"
                    + authenticationManager.getAuthentication().getLoginURL()
                    + "'>Sign in</a>");
        }
            break;
        case SIGNED_IN: {
            String html = "<span class='userid'>"
                    + authenticationManager.getAuthentication().getEmail()
                    + "</span>";
            html += "<a href='"
                    + authenticationManager.getAuthentication().getLogoutURL()
                    + "'>Sign out</a>";
            setHTML(html);
        }
            break;
        case FAILED: {
            // TODO inform user to report error and reload page
            setHTML("<span class='warning'>Not signed in (Error occured: "
                    + authenticationManager.getFailure().getMessage()
                    + ")</span>");
        }
            break;
        }
    }
}