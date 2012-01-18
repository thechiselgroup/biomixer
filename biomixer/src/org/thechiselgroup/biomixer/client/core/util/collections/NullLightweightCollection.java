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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class NullLightweightCollection<T> implements
        LightweightCollection<T> {

    @SuppressWarnings("rawtypes")
    private static NullLightweightCollection NULL_LIGHTWEIGHT_COLLECTION = new NullLightweightCollection();

    @SuppressWarnings("unchecked")
    public static <T> NullLightweightCollection<T> nullLightweightCollection() {
        return NULL_LIGHTWEIGHT_COLLECTION;
    }

    private NullLightweightCollection() {
    }

    @Override
    public boolean contains(T t) {
        return false;
    }

    @Override
    public T getFirstElement() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return NullIterator.nullIterator();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public List<T> toList() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "[]";
    }

}