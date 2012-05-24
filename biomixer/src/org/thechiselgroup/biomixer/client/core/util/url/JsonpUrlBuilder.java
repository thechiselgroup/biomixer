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

import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;

/**
 * This class causes the UrlBuilder methods to be applied to the "path"
 * attribute of the url in the NCBO JSONP REST service calls. If you need to
 * manipulate the overall url, call {@link #getOverallUrlBuilder()} and add
 * parameters, etc. to it.
 * 
 * @author drusk
 * 
 */
public class JsonpUrlBuilder implements UrlBuilder {

    /**
     * Builds up the doubly-encoded "path" parameter portion of the overall url.
     * 
     * @author drusk
     * 
     */
    private class PathBuilder {

        private StringBuilder path = new StringBuilder();

        private String hash = null;

        private Map<String, String[]> params = CollectionFactory
                .createStringMap();

        public void addHash(String hash) {
            this.hash = hash;
        }

        public void addParameter(String key, String... value) {
            params.put(key, value);
        }

        public void setPath(String path) {
            this.path.append(path);
        }

        public String toEncodedString() {
            char prefix = '?';
            for (Entry<String, String[]> entry : params.entrySet()) {
                for (String val : entry.getValue()) {
                    path.append(prefix).append(entry.getKey()).append("=")
                            .append(val);
                    prefix = '&';
                }
            }
            if (hash != null) {
                path.append("#").append(hash);
            }
            return UriUtils.encodeURIComponent(path.toString());
        }
    }

    private DefaultUrlBuilder overallUrlBuilder;

    private PathBuilder pathBuilder = new PathBuilder();

    public JsonpUrlBuilder(DefaultUrlBuilder defaultUrlBuilder) {
        this.overallUrlBuilder = defaultUrlBuilder;
    }

    public UrlBuilder getOverallUrlBuilder() {
        return overallUrlBuilder;
    }

    @Override
    public UrlBuilder hash(String hash) {
        pathBuilder.addHash(hash);
        return this;
    }

    @Override
    public UrlBuilder host(String host) {
        overallUrlBuilder.host(host);
        return this;
    }

    /**
     * Adds a parameter to the "path" attribute which should end up
     * singly-encoded.
     */
    @Override
    public UrlBuilder parameter(String key, String... values) {
        pathBuilder.addParameter(key, values);
        return this;
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
        pathBuilder.setPath(path.replaceFirst("^/?bioportal", ""));
        return this;
    }

    @Override
    public UrlBuilder port(int port) {
        overallUrlBuilder.port(port);
        return this;
    }

    @Override
    public UrlBuilder protocol(String protocol) {
        overallUrlBuilder.protocol(protocol);
        return this;
    }

    @Override
    public String toString() {
        overallUrlBuilder.parameter("path", pathBuilder.toEncodedString());
        return overallUrlBuilder.toString();
    }

    /**
     * Adds a parameter to the "path" attribute which should end up
     * doubly-encoded.
     */
    @Override
    public UrlBuilder uriParameter(String key, String uriValue) {
        parameter(key, UriUtils.encodeURIComponent(uriValue));
        return this;
    }

}
