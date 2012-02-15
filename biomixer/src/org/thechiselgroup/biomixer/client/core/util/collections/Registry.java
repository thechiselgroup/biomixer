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

public class Registry<T extends Identifiable> {

    private IdentifiablesSet<T> registeredObjects = new IdentifiablesSet<T>();

    public boolean contains(T t) {
        return registeredObjects.contains(t);
    }

    public T get(String id) {
        assert registeredObjects.contains(id) : "object" + " with id '" + id
                + "' not registered in " + getClass().getName()
                + " (available: " + registeredObjects + ")";
        return registeredObjects.get(id);
    }

    public LightweightCollection<T> getAll() {
        return registeredObjects.getAll();
    }

    public void register(T o) {
        assert o != null;
        assert !registeredObjects.contains(o.getId()) : "Object with id "
                + o.getId() + " is already registered in "
                + getClass().getName();
        registeredObjects.put(o);
    }
}