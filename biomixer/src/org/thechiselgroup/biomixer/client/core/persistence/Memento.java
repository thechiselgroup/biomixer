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
package org.thechiselgroup.biomixer.client.core.persistence;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class Memento implements Serializable {

    private String factoryId;

    private static final long serialVersionUID = -4396834563728094108L;

    private TreeMap<String, Memento> children = new TreeMap<String, Memento>();

    private TreeMap<String, Serializable> values = new TreeMap<String, Serializable>();

    public Memento() {
        // for GWT
    }

    public Memento(String factoryId) {
        this.factoryId = factoryId;
    }

    public void addChild(String key, Memento child) {
        children.put(key, child);
    }

    // TODO throw exception (invalid state)
    public Memento getChild(String key) {
        return getChildren().get(key);
    }

    // TODO immutable
    public SortedMap<String, Memento> getChildren() {
        return children;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public Serializable getValue(String key) {
        return values.get(key);
    }

    // TODO immutable
    public SortedMap<String, Serializable> getValues() {
        return values;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public Object setValue(String key, Serializable value) {
        return values.put(key, value);
    }

    @Override
    public String toString() {
        return "Memento [factoryId=" + factoryId + ", children=" + children
                + ", values=" + values + "]";
    }

}