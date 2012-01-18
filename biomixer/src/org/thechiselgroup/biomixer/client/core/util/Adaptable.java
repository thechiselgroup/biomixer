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

public interface Adaptable {

    /**
     * Returns an adapter to {@code T} for this object.
     * 
     * @param <T>
     *            Type this object should be adapted to
     * @param clazz
     *            Type this object should be adapted to
     * @return Adapter that offers functionality defined in {@code T} for this
     *         object.
     * 
     * @throws NoSuchAdapterException
     *             Object cannot be adapted to {@code clazz}
     */
    <T> T adaptTo(Class<T> clazz) throws NoSuchAdapterException;

    /**
     * Tests if this object can be adapted to class {@code clazz}.
     */
    boolean isAdaptableTo(Class<?> clazz);

}