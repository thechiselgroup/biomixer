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
package org.thechiselgroup.biomixer.shared.core.test.matchers.collections;

import java.util.Collection;

import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

public final class IsContainedInCollectionMatcher<T> extends TypeSafeMatcher<T> {

    private final Collection<T> collection;

    public IsContainedInCollectionMatcher(Collection<T> collection) {
        this.collection = collection;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("contained in").appendValue(collection);
    }

    @Override
    public boolean matchesSafely(T actual) {
        return collection.contains(actual);
    }

}