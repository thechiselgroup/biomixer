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
package org.thechiselgroup.biomixer.client.core.util;

public final class ObjectUtils {

    /**
     * Equals method that works if both objects are null.
     */
    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }

        if (o1 == null || o2 == null) {
            return false;
        }

        assert o1 != null && o2 != null;

        return o1.equals(o2);
    }

    /**
     * {@link #toString()} static method that can handle <code>null</code> cases
     * (returns empty String for those).
     */
    public static String toString(Object o) {
        return o == null ? "" : o.toString();
    }

    private ObjectUtils() {

    }

}
