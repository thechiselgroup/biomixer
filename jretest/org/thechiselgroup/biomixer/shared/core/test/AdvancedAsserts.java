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
package org.thechiselgroup.biomixer.shared.core.test;

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers;

public final class AdvancedAsserts {

    public static void assertArrayEquals(double[] expected, double[] actual,
            double delta) {

        Assert.assertEquals(expected.length, actual.length);
        for (int i = 0; i < actual.length; i++) {
            Assert.assertEquals(expected[i], actual[i], delta);
        }
    }

    public static <S, T> void assertMapKeysEqual(Map<S, T> result,
            S... expectedKeys) {

        assertThat(result.keySet(),
                CollectionMatchers.containsExactly(expectedKeys));
    }

    public static <T> void assertSortedEquals(List<T> expected, List<T> actual) {
        String failureMessage = "expected: " + expected + ", but was: "
                + actual;

        Assert.assertEquals(failureMessage, expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(failureMessage, expected.get(i), actual.get(i));
        }
    }

    private AdvancedAsserts() {
    }

}