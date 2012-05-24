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
package org.thechiselgroup.biomixer.client.core.util.url;

public class JsonpUrlBuilder implements UrlBuilder {

    private DefaultUrlBuilder defaultUrlBuilder;

    public JsonpUrlBuilder(DefaultUrlBuilder defaultUrlBuilder) {
        this.defaultUrlBuilder = defaultUrlBuilder;
    }

    @Override
    public UrlBuilder hash(String hash) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UrlBuilder host(String host) {
        return defaultUrlBuilder.host(host);
    }

    @Override
    public UrlBuilder parameter(String key, String... values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UrlBuilder path(String path) {
        /*
         * Will be an encoded parameter in url ex:
         * http://stage.bioontology.org/ajax
         * /jsonp?path=%2Fvirtual%2Fontology%2F1078%2Fall&apikey=YourApiKey
         */
        /*
         * NOTE: remove /bioportal at front if it is present
         */
        String trimmedPath = path.replaceFirst("^/?bioportal", "");
        return defaultUrlBuilder.uriParameter("path", trimmedPath);
    }

    @Override
    public UrlBuilder port(int port) {
        return defaultUrlBuilder.port(port);
    }

    @Override
    public UrlBuilder protocol(String protocol) {
        return defaultUrlBuilder.protocol(protocol);
    }

    @Override
    public UrlBuilder uriParameter(String key, String uriValue) {
        // In the end will be doubly-encoded
        // TODO Auto-generated method stub
        return null;
    }

}
