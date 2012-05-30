/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.thechiselgroup.biomixer.client.core.util.url;

import java.util.HashMap;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.UriUtils;

import com.google.gwt.http.client.URL;

/**
 * URL builder that does not encode the built URL (you have to take care of that
 * yourself). Copied from {@link com.google.gwt.http.client.UrlBuilder} and
 * removed call to {@link URL#encode(String)}.
 */
public class DefaultUrlBuilder implements UrlBuilder {

    /**
     * A mapping of query parameters to their values.
     */
    private final Map<String, String[]> listParamMap = new HashMap<String, String[]>();

    private String protocol = "http";

    private String host = null;

    private int port = PORT_UNSPECIFIED;

    private String path = null;

    private String hash = null;

    /**
     * Assert that the value is not null.
     * 
     * @param value
     *            the value
     * @param message
     *            the message to include with any exceptions
     * @throws IllegalArgumentException
     *             if value is null
     */
    private void assertNotNull(Object value, String message)
            throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the value is not null or empty.
     * 
     * @param value
     *            the value
     * @param message
     *            the message to include with any exceptions
     * @param isState
     *            if true, throw a state exception instead
     * @throws IllegalArgumentException
     *             if value is null
     * @throws IllegalStateException
     *             if value is null and isState is true
     */
    private void assertNotNullOrEmpty(String value, String message,
            boolean isState) throws IllegalArgumentException {
        if (value == null || value.length() == 0) {
            if (isState) {
                throw new IllegalStateException(message);
            } else {
                throw new IllegalArgumentException(message);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderInterface#
     * hash(java.lang.String)
     */
    @Override
    public UrlBuilder hash(String hash) {
        if (hash != null && hash.startsWith("#")) {
            hash = hash.substring(1);
        }
        this.hash = hash;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderInterface#
     * host(java.lang.String)
     */
    @Override
    public UrlBuilder host(String host) {
        // Extract the port from the host.
        if (host != null && host.contains(":")) {
            String[] parts = host.split(":");
            if (parts.length > 2) {
                throw new IllegalArgumentException(
                        "Host contains more than one colon: " + host);
            }
            try {
                port(Integer.parseInt(parts[1]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Could not parse port out of host: " + host);
            }
            host = parts[0];
        }
        this.host = host;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderInterface#
     * parameter(java.lang.String, java.lang.String)
     */
    @Override
    public DefaultUrlBuilder parameter(String key, String... values) {
        assertNotNullOrEmpty(key, "Key cannot be null or empty", false);
        assertNotNull(values,
                "Values cannot null. Try using removeParameter instead.");
        if (values.length == 0) {
            throw new IllegalArgumentException(
                    "Values cannot be empty.  Try using removeParameter instead.");
        }
        listParamMap.put(key, values);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderInterface#
     * path(java.lang.String)
     */
    @Override
    public UrlBuilder path(String path) {
        if (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderInterface#
     * port(int)
     */
    @Override
    public UrlBuilder port(int port) {
        this.port = port;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderInterface#
     * protocol(java.lang.String)
     */
    @Override
    public UrlBuilder protocol(String protocol) {
        assertNotNull(protocol, "Protocol cannot be null");
        if (protocol.endsWith("://")) {
            protocol = protocol.substring(0, protocol.length() - 3);
        } else if (protocol.endsWith(":/")) {
            protocol = protocol.substring(0, protocol.length() - 2);
        } else if (protocol.endsWith(":")) {
            protocol = protocol.substring(0, protocol.length() - 1);
        }
        if (protocol.contains(":")) {
            throw new IllegalArgumentException("Invalid protocol: " + protocol);
        }
        assertNotNullOrEmpty(protocol, "Protocol cannot be empty", false);
        this.protocol = protocol;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderInterface#
     * toString()
     */
    @Override
    public String toString() {
        StringBuilder url = new StringBuilder();

        // http://
        url.append(protocol).append("://");

        // http://www.google.com
        if (host != null) {
            url.append(host);
        }

        // http://www.google.com:80
        if (port != PORT_UNSPECIFIED) {
            url.append(":").append(port);
        }

        // http://www.google.com:80/path/to/file.html
        if (path != null && !"".equals(path)) {
            url.append("/").append(path);
        }

        // Generate the query string.
        // http://www.google.com:80/path/to/file.html?k0=v0&k1=v1
        char prefix = '?';
        for (Map.Entry<String, String[]> entry : listParamMap.entrySet()) {
            for (String val : entry.getValue()) {
                url.append(prefix).append(entry.getKey()).append('=');
                if (val != null) {
                    url.append(val);
                }
                prefix = '&';
            }
        }

        // http://www.google.com:80/path/to/file.html?k0=v0&k1=v1#token
        if (hash != null) {
            url.append("#").append(hash);
        }

        return url.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderInterface#
     * uriParameter(java.lang.String, java.lang.String)
     */
    @Override
    public UrlBuilder uriParameter(String key, String uriValue) {
        return parameter(key, UriUtils.encodeURIComponent(uriValue));
    }
}
