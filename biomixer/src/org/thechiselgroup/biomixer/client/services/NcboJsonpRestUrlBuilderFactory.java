/*******************************************************************************
 * Copyright 2012 David Rusk
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
package org.thechiselgroup.biomixer.client.services;

import org.thechiselgroup.biomixer.client.core.util.url.DefaultUrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;

public class NcboJsonpRestUrlBuilderFactory implements UrlBuilderFactory {

    public static final String PROTOCOL = "http";

    public static final String SERVER = "stage.bioontology.org";

    public final static String API_KEY_PARAMETER = "apikey";

    public static final String CALLBACK = "callback";

    public static final String PATH = "ajax/jsonp";

    @Override
    public UrlBuilder createUrlBuilder() {
        return new DefaultUrlBuilder()
                .host(SERVER)
                .protocol(PROTOCOL)
                .path(PATH)
                .parameter(API_KEY_PARAMETER,
                        "6700f7bc-5209-43b6-95da-44336cbc0a3a");
    }
}
