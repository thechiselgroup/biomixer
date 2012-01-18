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
package org.thechiselgroup.biomixer.client.core.util.collections;

import java.util.List;
import java.util.NoSuchElementException;

import org.thechiselgroup.biomixer.shared.core.util.ForTest;

/**
 * Generic lightweight collection. LightweightCollection provides a read-only
 * interface that extends {@link Iterable} and only contains the most important
 * operations and thus facilitate the implementation of optimized JavaScript
 * versions.
 * 
 * @author Lars Grammel
 */
public interface LightweightCollection<T> extends Iterable<T> {

    /**
     * Tests if the element is contained in this collection. <b>PERFORMANCE</b>:
     * Differs from implementation to implementation. Worst case: linear to size
     * of collection.
     */
    boolean contains(T t);

    /**
     * @return First item from the collection
     * 
     * @throws NoSuchElementException
     *             Thrown if the collection is empty.
     */
    T getFirstElement() throws NoSuchElementException;

    /**
     * @return {@code true} if there are no elements in this collection, and
     *         false if there are.
     */
    boolean isEmpty();

    /**
     * @return The number of elements in this collection.
     */
    int size();

    /**
     * <b>INTENDED FOR TEST USAGE.</b> Converts this lightweight collection into
     * a List. This usually has a fairly high performance penalty and is only
     * recommended for testing.
     */
    @ForTest
    List<T> toList();

}