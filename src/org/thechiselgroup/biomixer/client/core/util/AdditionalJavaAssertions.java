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
package org.thechiselgroup.biomixer.client.core.util;

import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;

/**
 * Advanced assertions based on the {@code assert} keyword. They would only work
 * if VM assertions are enabled and should not be used in testing.
 * 
 * @author Lars Grammel
 */
public final class AdditionalJavaAssertions {

    public static void assertMapDoesNotContainEmptyLists(
            Map<String, ? extends LightweightCollection<?>> map) {

        for (Entry<String, ? extends LightweightCollection<?>> entry : map
                .entrySet()) {
            assert !entry.getValue().isEmpty() : "empty list at key "
                    + entry.getKey();
        }
    }

    private AdditionalJavaAssertions() {
    }

}