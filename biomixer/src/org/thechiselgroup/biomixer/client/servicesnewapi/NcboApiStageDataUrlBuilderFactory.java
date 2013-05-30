/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.servicesnewapi;

import org.thechiselgroup.biomixer.client.core.util.url.DefaultUrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;

/**
 * The new API for REST services differs from the old REST service. It has a
 * different endpoint, different query construction, and different parsing
 * requirements. This class provides access to the endpoint.
 * 
 * @author everbeek
 * 
 */
public class NcboApiStageDataUrlBuilderFactory implements UrlBuilderFactory {

    public static final String PROTOCOL = "http";

    public static final String SERVER = "stagedata.bioontology.org";

    public final static String API_KEY_PARAMETER = "apikey";

    public final static String USER_API_KEY_PARAMETER = "userapikey";

    public static final String FORMAT_PARAMETER = "format";

    public static final String FORMAT_VALUE = "jsonp";

    public static final String CALLBACK = "callback";

    // public static final String PATH = "ajax/jsonp";

    private String userApiKey;

    private String server = SERVER;

    @Override
    public UrlBuilder createUrlBuilder() {
        return
        new DefaultUrlBuilder().host(this.server)
                .protocol(PROTOCOL)
                .parameter(FORMAT_PARAMETER, FORMAT_VALUE)
                .parameter(API_KEY_PARAMETER, BIO_MIXER_API_KEY)
                .parameter(USER_API_KEY_PARAMETER, userApiKey)
        ;
    }

    @Override
    public void setUserApiKey(String apiKey) {
        assert apiKey != null;
        this.userApiKey = apiKey;
    }

    @Override
    public void setServerRoot(String serverRoot) {
        if (serverRoot != null) {
            this.server = serverRoot;
        } else {
            this.server = SERVER;
        }
    }
}