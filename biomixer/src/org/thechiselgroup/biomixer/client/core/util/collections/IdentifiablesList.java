/*******************************************************************************
 * Copyright 2012 David Rusk 
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

/**
 * Maintains a <code>List</code> of {@link Identifiable}s along with an
 * {@link IdentifiablesSet}. The set provides quick lookup by id, while the list
 * allows order to be maintained.
 * 
 * @author drusk
 * 
 */
public class IdentifiablesList<T extends Identifiable> implements Iterable<T> {

    private List<T> list = new ArrayList<T>();

    private IdentifiablesSet<T> identifiablesSet = new IdentifiablesSet<T>();

    public void add(T t) {
        assert t != null;
        list.add(t);
        identifiablesSet.put(t);
    }

    public List<T> asList() {
        return list;
    }

    public boolean contains(String id) {
        assert id != null;
        return identifiablesSet.contains(id);
    }

    public boolean contains(T t) {
        assert t != null;
        return contains(t.getId());
    }

    public T get(String id) {
        assert id != null;
        assert contains(id) : "no object with id '" + id + "' available";
        return identifiablesSet.get(id);
    }

    public Iterable<String> getIds() {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                final Iterator<T> identifiablesIterator = list.iterator();

                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return identifiablesIterator.hasNext();
                    }

                    @Override
                    public String next() {
                        return identifiablesIterator.next().getId();
                    }

                    @Override
                    public void remove() {
                        identifiablesIterator.remove();
                    }
                };
            }
        };
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    public void remove(String id) {
        assert id != null;
        list.remove(get(id));
        identifiablesSet.remove(id);
    }

    public int size() {
        assert list.size() == identifiablesSet.size();
        return list.size();
    }

}
