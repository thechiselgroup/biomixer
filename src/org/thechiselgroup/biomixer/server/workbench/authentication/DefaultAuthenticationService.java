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
package org.thechiselgroup.biomixer.server.workbench.authentication;

import org.thechiselgroup.biomixer.client.core.util.ServiceException;
import org.thechiselgroup.biomixer.client.workbench.authentication.Authentication;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationService;

import com.google.appengine.api.users.UserService;

public class DefaultAuthenticationService implements AuthenticationService {

    private UserService userService;

    public DefaultAuthenticationService(UserService userService) {
        assert userService != null;
        this.userService = userService;
    }

    @Override
    public Authentication getAuthentication(String redirectURL)
            throws ServiceException {

        assert redirectURL != null;

        boolean isSignedIn = userService.isUserLoggedIn();
        String email = null;
        String logoutURL = null;
        String loginURL = null;

        if (isSignedIn) {
            email = userService.getCurrentUser().getEmail();
            logoutURL = userService.createLogoutURL(redirectURL);
        } else {
            loginURL = userService.createLoginURL(redirectURL);
        }

        return new Authentication(email, isSignedIn, loginURL, logoutURL);
    }
}