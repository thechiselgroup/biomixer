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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;

/**
 * <p>
 * Utility class for disposing objects.
 * </p>
 * 
 * @see Disposable
 * 
 * @author Lars Grammel
 */
public final class DisposeUtil {

    /**
     * <p>
     * Disposes {@code o}, if {@code o} is {@link Disposable}.
     * </p>
     * *
     * <p>
     * Usage example for {@link #dispose(Object)} :
     * {@code someField = DisposeUtil.dispose(someField);}
     * </p>
     * 
     * @return {@code null}. Can be used to assign the dispose result to the
     *         field as in {@code someField = DisposeUtil.dispose(someField);}.
     */
    public static <T> T dispose(T o) throws RuntimeException {
        if (o != null && o instanceof Disposable) {
            ((Disposable) o).dispose();
        }

        return null;
    }

    /**
     * <p>
     * Safely disposes {@code o}, if {@code o} is {@link Disposable}. Any
     * {@link Throwable} is report to the {@link ErrorHandler}.
     * </p>
     * <p>
     * Example:
     * {@code someField = DisposeUtil.safelyDispose(someField, errorHandler);}
     * </p>
     * 
     * @return {@code null}. Can be used to assign the dispose result to the
     *         field as in
     *         {@code someField = DisposeUtil.safelyDispose(someField, errorHandler);}
     *         .
     */
    public static <T> T safelyDispose(T o, ErrorHandler errorHandler) {
        try {
            return dispose(o);
        } catch (Throwable ex) {
            errorHandler.handleError(ex);
            return null;
        }
    }

    private DisposeUtil() {
    }

}