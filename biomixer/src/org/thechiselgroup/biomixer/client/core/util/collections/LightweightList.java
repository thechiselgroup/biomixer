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
package org.thechiselgroup.biomixer.client.core.util.collections;

import java.util.Collection;

/**
 * Minimalistic list for intermediary data processing. A JavaScript
 * implementation is available. <code>Null</code> values are not supported.
 * 
 * @author Lars Grammel
 */
public interface LightweightList<T> extends LightweightCollection<T> {

    /**
     * Adds an element to this list.
     * 
     * @param t
     *            Element. Must not be null.
     */
    void add(T t);

    /**
     * Adds all elements in <code>collection</code> to this list. There must not
     * be any null elements.
     */
    void addAll(Iterable<? extends T> collection);

    /**
     * Adds all elements in <code>array</code> to this list. There must not be
     * any null elements.
     */
    void addAll(T[] array);

    /**
     * Returns the element at the specified index.
     * 
     * @throws IndexOutOfBoundsException
     *             if i >= |size()| or i < 0
     */
    T get(int i);

    /**
     * Converts this {@link LightweightList} to an array testing purposes.
     * 
     * @see Collection#toArray(Object[])
     */
    T[] toArray(T[] array);

}