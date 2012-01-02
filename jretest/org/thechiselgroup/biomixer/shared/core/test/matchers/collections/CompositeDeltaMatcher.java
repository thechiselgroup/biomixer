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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;

public final class CompositeDeltaMatcher<T> extends TypeSafeMatcher<Delta<T>> {

    private final Matcher<?> addedElementsMatcher;

    private final Matcher<?> updatedElementsMatcher;

    private final Matcher<?> removedElementsMatcher;

    public CompositeDeltaMatcher(Matcher<?> addedElementsMatcher,
            Matcher<?> updatedElementsMatcher, Matcher<?> removedElementsMatcher) {

        this.addedElementsMatcher = addedElementsMatcher;
        this.updatedElementsMatcher = updatedElementsMatcher;
        this.removedElementsMatcher = removedElementsMatcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("added ")
                .appendDescriptionOf(addedElementsMatcher)
                .appendText("updated ")
                .appendDescriptionOf(updatedElementsMatcher)
                .appendText("removed ")
                .appendDescriptionOf(removedElementsMatcher);
    }

    @Override
    public boolean matchesSafely(Delta<T> actual) {
        return addedElementsMatcher.matches(actual.getAddedElements())
                && updatedElementsMatcher.matches(actual.getUpdatedElements())
                && removedElementsMatcher.matches(actual.getRemovedElements());
    }
}