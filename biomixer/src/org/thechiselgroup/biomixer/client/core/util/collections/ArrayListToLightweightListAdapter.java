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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

// TODO expose interface with set/remove in a transparent way (e.g. in factory)
// TODO rename to ArrayListBasedLightweightList
public class ArrayListToLightweightListAdapter<T> implements LightweightList<T> {

    private List<T> delegate = new ArrayList<T>();

    @Override
    public void add(T t) {
        delegate.add(t);
    }

    @Override
    public void addAll(Iterable<? extends T> collection) {
        for (T t : collection) {
            add(t);
        }
    }

    @Override
    public void addAll(T[] array) {
        for (T t : array) {
            add(t);
        }
    }

    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean contains(T t) {
        return delegate.contains(t);
    }

    @Override
    public T get(int i) {
        return delegate.get(i);
    }

    @Override
    public T getFirstElement() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return get(0);
    }

    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    public T remove(int index) {
        return delegate.remove(index);
    }

    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    public T set(int index, T element) {
        return delegate.set(index, element);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public T[] toArray(T[] array) {
        return delegate.toArray(array);
    }

    @Override
    public List<T> toList() {
        return delegate;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
