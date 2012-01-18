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

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Provides access to the authentication state. This class can be used to check
 * if the user has been authenticated, to get the authentication object, to
 * check what the exact authentication state is, and to register handlers that
 * get notified when the authentication state changes (e.g. the user logs on or
 * off).
 * 
 * @author Lars Grammel
 */
public interface AuthenticationManager {

    HandlerRegistration addAuthenticationStateChangedEventHandler(
            AuthenticationStateChangedEventHandler handler);

    /**
     * Returns the current authentication information, which contains
     * information about the user as well as the login and logout urls.
     */
    Authentication getAuthentication();

    /**
     * Returns the authentication state. If you just need to know if a user is
     * authenticated or not, you should use {@link #isAuthenticated()}.
     * 
     * @return current authentication state
     */
    AuthenticationState getAuthenticationState();

    /**
     * @return exception that caused the authentication to fail, if the
     *         authentication state is <code>FAILED</code>, and
     *         <code>null</code> otherwise.
     */
    Throwable getFailure();

    /**
     * @return true if the user is signed in
     */
    boolean isAuthenticated();

}