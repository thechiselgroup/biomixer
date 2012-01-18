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

import static org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory.createLightweightList;

import java.util.Collection;

import org.hamcrest.Matcher;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;

public final class CollectionMatchers {

    public static <T> Matcher<Collection<T>> contains(T expectedValue) {
        return new ContainsValueMatcher<T>(expectedValue);
    }

    public static <T> Matcher<Iterable<T>> containsExactly(
            Collection<T> expected) {

        return new IterableContentMatcher<T, Iterable<T>>(expected);
    }

    public static <T> Matcher<Iterable<T>> containsExactly(
            LightweightCollection<T> expected) {

        return CollectionMatchers.<T> containsExactly(expected.toList());
    }

    public static <T> Matcher<Iterable<T>> containsExactly(T... expected) {
        LightweightList<T> list = createLightweightList();
        list.addAll(expected);
        return CollectionMatchers.<T> containsExactly(list);
    }

    public static <T> Matcher<LightweightCollection<T>> containsExactlyLightweightCollection(
            LightweightCollection<T> expected) {

        return new LightweightCollectionContentMatcher<T>(expected);
    }

    public static <T> Matcher<LightweightCollection<T>> containsExactlyLightweightCollection(
            T... expected) {

        return containsExactlyLightweightCollection(LightweightCollections
                .toCollection(expected));
    }

    public static <T> Matcher<T[]> equalsArray(T... expected) {
        return new ArrayEqualsMatcher<T>(expected);
    }

    public static <T> Matcher<T> isContainedIn(Collection<T> expected) {
        return new IsContainedInCollectionMatcher<T>(expected);
    }

    public static <T> Matcher<T> isContainedIn(LightweightCollection<T> expected) {
        return CollectionMatchers.<T> isContainedIn(expected.toList());
    }

    public static <T extends Object> Matcher<LightweightCollection<T>> isEmpty() {
        return new IsEmptyLightweightCollectionMatcher<T>();
    }

    public static <T> Matcher<LightweightCollection<T>> isEmpty(Class<T> clazz) {
        return new IsEmptyLightweightCollectionMatcher<T>();
    }

    public static <T> Matcher<Delta<T>> matchesDelta(
            Matcher<?> addedElementsMatcher, Matcher<?> updatedElementsMatcher,
            Matcher<?> removedElementsMatcher) {

        return new CompositeDeltaMatcher<T>(addedElementsMatcher,
                updatedElementsMatcher, removedElementsMatcher);
    }

    private CollectionMatchers() {
    }

}