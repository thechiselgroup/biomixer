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
package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.util.collections.SingleItemCollection;

import com.google.gwt.event.shared.UmbrellaException;

public final class ExceptionUtil {

    /**
     * Recursively unwrap and add all exceptions from {@link UmbrellaException}.
     */
    private static Collection<Throwable> getCauses(Collection<Throwable> errors) {
        HashSet<Throwable> unwrappedErrors = new HashSet<Throwable>();
        for (Throwable error : errors) {
            if (error instanceof UmbrellaException) {
                Set<Throwable> causes = ((UmbrellaException) error).getCauses();
                unwrappedErrors.addAll(getCauses(causes));
            } else {
                unwrappedErrors.add(error);
            }
        }
        return unwrappedErrors;
    }

    public static Collection<Throwable> getCauses(Throwable error) {
        return getCauses(new SingleItemCollection<Throwable>(error));
    }

    public static String getStackTraceAsString(Throwable error) {
        StackTraceElement[] stackTrace = error.getStackTrace();

        String result = "";
        for (StackTraceElement stackTraceElement : stackTrace) {
            result += stackTraceElement + "<br/>";
        }
        return result;
    }

    private ExceptionUtil() {
    }

}
