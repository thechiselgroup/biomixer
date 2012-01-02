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

import java.io.Serializable;

public class Authentication implements Serializable {

    private static final long serialVersionUID = -7044990088160485627L;

    private String email;

    private boolean isSignedIn;

    private String loginURL;

    private String logoutURL;

    public Authentication() {
        // for GWT serialization
    }

    public Authentication(String email, boolean isSignedIn, String loginURL,
            String logoutURL) {

        this.email = email;
        this.isSignedIn = isSignedIn;
        this.logoutURL = logoutURL;
        this.loginURL = loginURL;
    }

    public String getEmail() {
        return email;
    }

    public String getLoginURL() {
        return loginURL;
    }

    public String getLogoutURL() {
        return logoutURL;
    }

    public boolean isSignedIn() {
        return isSignedIn;
    }

    @Override
    public String toString() {
        return "Authentication [email=" + email + ", isSignedIn=" + isSignedIn
                + ", loginURL=" + loginURL + ", logoutURL=" + logoutURL + "]";
    }

}
