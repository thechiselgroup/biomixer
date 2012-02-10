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
package org.thechiselgroup.biomixer.client.services.hierarchy;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;

public class ResourceMatcher extends TypeSafeMatcher<Resource> {

    public static Matcher<Resource> equalsResource(Resource expected) {
        return new ResourceMatcher(expected);
    }

    private Resource expected;

    public ResourceMatcher(Resource expected) {
        this.expected = expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expected.toString());
    }

    @Override
    public boolean matchesSafely(Resource actual) {
        Map<String, Serializable> actualProperties = actual.getProperties();
        Map<String, Serializable> expectedProperties = expected.getProperties();

        if (actualProperties.size() != expectedProperties.size()) {
            return false;
        }

        for (Entry<String, Serializable> actualEntry : actualProperties
                .entrySet()) {
            String key = actualEntry.getKey();

            if (actual.isUriList(key) && expected.isUriList(key)) {
                // compare lists, independent of element ordering
                if (!uriListsHaveExactlySameValues(actual.getUriListValue(key),
                        expected.getUriListValue(key))) {
                    return false;
                }
            } else if (!actual.isUriList(key) && !actual.isUriList(key)) {
                Object actualValue = actual.getValue(key);
                Object expectedValue = expected.getValue(key);
                if (!actualValue.equals(expectedValue)) {
                    return false;
                }
            } else {
                // properties have mismatched types
                return false;
            }

        }

        return true;
    }

    private boolean uriListsHaveExactlySameValues(UriList actualUriList,
            UriList expectedUriList) {
        if (actualUriList.size() != expectedUriList.size()) {
            return false;
        }

        for (String expectedUri : expectedUriList) {
            if (!actualUriList.contains(expectedUri)) {
                return false;
            }
        }

        return true;
    }
}
