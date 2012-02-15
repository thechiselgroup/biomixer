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

import java.util.Iterator;
import java.util.Map;

public class IdentifiablesSet<T extends Identifiable> implements Iterable<T> {

    private Map<String, T> content = CollectionFactory.createStringMap();

    public boolean contains(String id) {
        assert id != null;
        return content.containsKey(id);
    }

    public boolean contains(T t) {
        assert t != null;
        return contains(t.getId());
    }

    public T get(String id) {
        assert id != null;
        assert contains(id) : "no object with id '" + id + "' available";
        return content.get(id);
    }

    public LightweightCollection<T> getAll() {
        LightweightList<T> results = CollectionFactory.createLightweightList();
        results.addAll(content.values());
        return results;
    }

    @Override
    public Iterator<T> iterator() {
        return content.values().iterator();
    }

    public void put(T... ts) {
        assert ts != null;
        for (T t : ts) {
            put(t);
        }
    }

    public void put(T t) {
        assert t != null;
        content.put(t.getId(), t);
    }

    @Override
    public String toString() {
        return content.toString();
    }
}
