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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ResourceTest {

    private String propertyKey;

    private Resource resource;

    @Test
    public void getUriListValueReturnsUriListProperty() {
        UriList uriList = new UriList();
        uriList.add("uri1");
        uriList.add("uri2");
        uriList.add("uri3");

        resource.putValue(propertyKey, uriList);
        assertEquals(true,
                resource.getUriListValue(propertyKey) instanceof UriList);
    }

    @Test
    public void getUriListValueReturnsUriListStringProperty() {
        resource.putValueAsUriList(propertyKey, "x");
        assertEquals(true,
                resource.getUriListValue(propertyKey) instanceof UriList);
    }

    @Test
    public void isUriListReturnsFalseIfPropertyIsInt() {
        resource.putValue(propertyKey, 5);
        assertEquals(false, resource.isUriList(propertyKey));
    }

    @Test
    public void isUriListReturnsFalseIfPropertyIsString() {
        resource.putValue(propertyKey, "String");
        assertEquals(false, resource.isUriList(propertyKey));
    }

    @Test
    public void isUriListReturnsFalseIfPropertyNotSet() {
        assertEquals(false, resource.isUriList(propertyKey));
    }

    @Test
    public void isUriListReturnsFalseIfPropertyValueIsNull() {
        resource.putValue(propertyKey, null);
        assertEquals(false, resource.isUriList(propertyKey));
    }

    @Test
    public void isUriListReturnsTrueIfPropertyIsEmptyUriList() {
        UriList uriList = new UriList();
        resource.putValue(propertyKey, uriList);

        assertEquals(true, resource.isUriList(propertyKey));
    }

    @Test
    public void isUriListReturnsTrueIfPropertyIsPopulatedUriList() {
        UriList uriList = new UriList();
        uriList.add("uri1");
        uriList.add("uri2");
        uriList.add("uri3");

        resource.putValue(propertyKey, uriList);

        assertEquals(true, resource.isUriList(propertyKey));
    }

    @Before
    public void setUp() {
        propertyKey = "p";
        resource = new Resource("1");
    }
}
