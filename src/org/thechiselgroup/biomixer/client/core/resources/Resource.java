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
package org.thechiselgroup.biomixer.client.core.resources;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

// TODO introduce resource ID's
// TODO equality / hash based on ID
public class Resource implements Serializable {

    private static final long serialVersionUID = 5652752520235015241L;

    public static String getTypeFromURI(String uri) {
        assert uri != null;

        int splitIndex = uri.indexOf(':');
        return uri.substring(0, splitIndex);
    }

    // TODO find ways to use better map implementation
    // (CollectionFactory.createStringMap)
    private HashMap<String, Serializable> properties = new HashMap<String, Serializable>();

    // unique resource identifier (URI)
    private String uri;

    // for GWT serialization usage only
    public Resource() {
    }

    public Resource(String uri) {
        assert uri != null;
        this.uri = uri;
    }

    public void applyPartialProperties(
            Map<String, Serializable> partialProperties) {
        for (Entry<String, Serializable> entry : partialProperties.entrySet()) {
            putValue(entry.getKey(), entry.getValue());
        }
    }

    public boolean containsProperty(String property) {
        return properties.containsKey(property);
    }

    /**
     * Equals is just based on the <code>uri</code>. In Choosel there should be
     * just one resource per uri.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Resource other = (Resource) obj;

        return uri.equals(other.uri);
    }

    public HashMap<String, Serializable> getProperties() {
        return properties;
    }

    public String getUri() {
        return uri;
    }

    // return a UriList representation of a resource property
    public UriList getUriListValue(String key) {
        UriList result = (UriList) getValue(key);

        if (result == null) {
            result = new UriList();
            putValue(key, result);
        }

        return result;
    }

    public Object getValue(String key) {
        return properties.get(key);
    }

    /**
     * @return hash code of uri
     */
    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    public boolean isUriList(String key) {
        return getValue(key) instanceof UriList;
    }

    public void putValue(String key, Serializable value) {
        properties.put(key, value);
    }

    public void putValueAsUriList(String key, String uri) {
        UriList uriList = new UriList();
        uriList.add(uri);
        putValue(key, uriList);
    }

    @Override
    public String toString() {
        return "Resource [uri=" + uri + ";properties=" + properties + "]";
    }

}
