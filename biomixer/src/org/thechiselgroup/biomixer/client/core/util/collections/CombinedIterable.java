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

import java.util.Iterator;

public class CombinedIterable<T> implements Iterable<T> {

    private final Iterable<T> firstIterable;

    private final Iterable<T> secondIterable;

    public CombinedIterable(Iterable<T> firstIterable,
            Iterable<T> secondIterable) {

        assert firstIterable != null;
        assert secondIterable != null;

        this.firstIterable = firstIterable;
        this.secondIterable = secondIterable;
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<T> firstIterator = firstIterable.iterator();
        final Iterator<T> secondIterator = secondIterable.iterator();

        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return firstIterator.hasNext() || secondIterator.hasNext();
            }

            @Override
            public T next() {
                return firstIterator.hasNext() ? firstIterator.next()
                        : secondIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}