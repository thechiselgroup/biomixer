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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Native JavaScript implementation of a String --> Value map.
 * 
 * @author Lars Grammel
 * 
 * @param <T>
 *            value type
 * 
 * @see {@link "http://blog.xkoder.com/2008/07/10/javascript-associative-arrays-demystified/"}
 */
public final class JavaScriptStringToObjectMap<T> implements Map<String, T> {

    private final static class InternalJsArray<T> extends JavaScriptObject {

        protected InternalJsArray() {
        }

        public final native InternalMapEntry<T> get(int index) /*-{
                                                               return this[index];
                                                               }-*/;

        public final native int length() /*-{
                                         return this.length;
                                         }-*/;

    }

    private static class InternalMapEntry<T> implements Map.Entry<String, T> {

        private final String key;

        private T value;

        public InternalMapEntry(String key, T value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public T setValue(T newValue) {
            T oldValue = value;
            value = newValue;
            return oldValue;
        }

    }

    /**
     * Sorted String to value map written in JavaScript. Caches sorted key array
     * and sorted map entry array (created lazily).
     */
    private final static class NativeMap<T> extends JavaScriptObject {

        /*
         * IMPLEMENTATION NOTE: although this internal map requires String keys,
         * we use objects to prevent unnecessary class casting. When profiling
         * in Chrome 8, casting turned out to be an expensive operation in GWT.
         */

        protected NativeMap() {

        }

        // @formatter:off
        public native void clear() /*-{
            for (var key in this.map) {
                delete this.map[key];
            }
            this.keys = null;
            this.entries = null;
        }-*/;
        // @formatter:on

        private native boolean containsKey(Object key) /*-{
                                                       return key in this.map;
                                                       }-*/;

        /**
         * @return sorted map entry array
         */
        // @formatter:off
        private native InternalJsArray<T> getEntryJsArray() /*-{
            // return if initialized
            if (this.entries != null) {
                return this.entries;
            }

            // initialize key cache if required 
            if (this.keys == null) {
                this.keys = [];
                for (var key in this.map) {
                    this.keys.push(key);
                }
                this.keys.sort();
            }

            // calculate value array
            this.entries = [];
            for (var i in this.keys) {
                this.entries.push(this.map[this.keys[i]]);
            }

            return this.entries;
        }-*/;
        // @formatter:on

        /**
         * @return sorted key array
         */
        // @formatter:off
        private native JsArrayString getKeyJsArray() /*-{
            if (this.keys != null) {
                return this.keys;
            }

            this.keys = [];
            for (var key in this.map) {
                this.keys.push(key);
            }
            this.keys.sort();
            return this.keys;
        }-*/;
        // @formatter:on

        private native InternalMapEntry<T> getMapEntry(Object key) /*-{
                                                                   return this.map[key];
                                                                   }-*/;

        private native void putMapEntry(Object key, InternalMapEntry<T> entry) /*-{
                                                                               this.map[key] = entry;
                                                                               this.keys = null;
                                                                               this.entries = null;
                                                                               }-*/;

        private native void remove(Object key) /*-{
                                               delete this.map[key];
                                               this.keys = null;
                                               this.entries = null;
                                               }-*/;

    }

    private int size = 0;

    private Set<Map.Entry<String, T>> entrySet = new Set<Map.Entry<String, T>>() {

        @Override
        public boolean add(Map.Entry<String, T> e) {
            throw new UnsupportedOperationException("add not supported");
        }

        @Override
        public boolean addAll(Collection<? extends Map.Entry<String, T>> c) {
            throw new UnsupportedOperationException("addAll not supported");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear not supported");
        }

        @Override
        public boolean contains(Object o) {
            InternalJsArray<T> entryJsArray = jsMap.getEntryJsArray();

            for (int i = 0; i < entryJsArray.length(); i++) {
                if (entryJsArray.get(i).equals(o)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean isEmpty() {
            return JavaScriptStringToObjectMap.this.isEmpty();
        }

        @Override
        public Iterator<Map.Entry<String, T>> iterator() {
            final InternalJsArray<T> entryJsArray = jsMap.getEntryJsArray();

            return new Iterator<Map.Entry<String, T>>() {

                private int currentIndex = 0;

                @Override
                public boolean hasNext() {
                    return currentIndex < entryJsArray.length();
                }

                @Override
                public Map.Entry<String, T> next() {
                    return entryJsArray.get(currentIndex++);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException(
                            "remove not supported");
                }
            };
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("remove not supported");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("removeAll not supported");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("retainAll not supported");
        }

        @Override
        public int size() {
            return JavaScriptStringToObjectMap.this.size();
        }

        @Override
        public Object[] toArray() {
            InternalJsArray<T> entryJsArray = jsMap.getEntryJsArray();

            Object[] result = new Object[entryJsArray.length()];
            for (int i = 0; i < result.length; i++) {
                result[i] = entryJsArray.get(i);
            }

            return result;
        }

        @Override
        public <S> S[] toArray(S[] a) {
            InternalJsArray<T> entryJsArray = jsMap.getEntryJsArray();

            for (int i = 0; i < a.length; i++) {
                a[i] = (S) entryJsArray.get(i);
            }

            return a;
        }

        @Override
        public String toString() {
            String result = "JavaScriptStringToObjectMap.EntrySet[";
            for (Entry<String, T> entry : this) {
                result += entry.getKey();
                result += "=";
                result += entry.getValue();
                result += ";";
            }
            return result + "]";
        }
    };

    private Collection<T> values = new Collection<T>() {

        @Override
        public boolean add(T e) {
            throw new UnsupportedOperationException("add not supported");
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException("addAll not supported");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear not supported");
        }

        @Override
        public boolean contains(Object o) {
            JsArrayString keyArray = jsMap.getKeyJsArray();

            for (int i = 0; i < keyArray.length(); i++) {
                if (jsMap.getMapEntry(keyArray.get(i)).getValue().equals(o)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean isEmpty() {
            return JavaScriptStringToObjectMap.this.isEmpty();
        }

        @Override
        public Iterator<T> iterator() {
            final InternalJsArray<T> entryJsArray = jsMap.getEntryJsArray();

            return new Iterator<T>() {

                private int currentIndex = 0;

                @Override
                public boolean hasNext() {
                    return currentIndex < entryJsArray.length();
                }

                @Override
                public T next() {
                    return entryJsArray.get(currentIndex++).getValue();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException(
                            "remove not supported");
                }
            };
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("remove not supported");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("removeAll not supported");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("retainAll not supported");
        }

        @Override
        public int size() {
            return JavaScriptStringToObjectMap.this.size();
        }

        @Override
        public Object[] toArray() {
            InternalJsArray<T> entryJsArray = jsMap.getEntryJsArray();

            Object[] result = new Object[entryJsArray.length()];
            for (int i = 0; i < result.length; i++) {
                result[i] = entryJsArray.get(i).value;
            }

            return result;
        }

        @Override
        public <S> S[] toArray(S[] a) {
            InternalJsArray<T> entryJsArray = jsMap.getEntryJsArray();

            for (int i = 0; i < a.length; i++) {
                a[i] = (S) entryJsArray.get(i).value;
            }

            return a;
        }

        @Override
        public String toString() {
            String result = "JavaScriptStringToObjectMap.Values[";
            for (T value : this) {
                result += value;
                result += ";";
            }
            return result + "]";
        }
    };

    private Set<String> keySet = new Set<String>() {

        @Override
        public boolean add(String e) {
            throw new UnsupportedOperationException("add not supported");
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            throw new UnsupportedOperationException("addAll not supported");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear not supported");
        }

        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean isEmpty() {
            return JavaScriptStringToObjectMap.this.isEmpty();
        }

        @Override
        public Iterator<String> iterator() {
            final JsArrayString keyArray = jsMap.getKeyJsArray();

            return new Iterator<String>() {

                private int currentIndex = 0;

                @Override
                public boolean hasNext() {
                    return currentIndex < keyArray.length();
                }

                @Override
                public String next() {
                    return keyArray.get(currentIndex++);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException(
                            "remove not supported");
                }
            };
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("remove not supported");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("removeAll not supported");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("retainAll not supported");
        }

        @Override
        public int size() {
            return JavaScriptStringToObjectMap.this.size();
        }

        @Override
        public Object[] toArray() {
            JsArrayString keyArray = jsMap.getKeyJsArray();

            Object[] result = new Object[keyArray.length()];
            for (int i = 0; i < result.length; i++) {
                result[i] = keyArray.get(i);
            }

            return result;
        }

        @Override
        public <S> S[] toArray(S[] a) {
            JsArrayString keyArray = jsMap.getKeyJsArray();

            for (int i = 0; i < a.length; i++) {
                a[i] = (S) keyArray.get(i);
            }

            return a;
        }

        @Override
        public String toString() {
            String result = "JavaScriptStringToObjectMap.KeySet[";
            for (String key : this) {
                result += key;
                result += ";";
            }
            return result + "]";
        }
    };

    private NativeMap<T> jsMap;

    public JavaScriptStringToObjectMap() {
        this.jsMap = createNativeMap();
    }

    @Override
    public void clear() {
        this.jsMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return jsMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (T t : this.values()) {
            if (t.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private native NativeMap<T> createNativeMap() /*-{
                                                  var nativeMap = new Object();
                                                  nativeMap.map = new Object(); // associative array
                                                  nativeMap.keys = null; // cache for sorted keys
                                                  nativeMap.entries = null; // cache for sorted entries
                                                  return nativeMap;
                                                  }-*/;

    @Override
    public Set<java.util.Map.Entry<String, T>> entrySet() {
        return entrySet;
    }

    @Override
    public T get(Object key) {
        if (!containsKey(key)) {
            return null;
        }

        return jsMap.getMapEntry(key).value;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Set<String> keySet() {
        return keySet;
    }

    @Override
    public T put(String key, T value) {
        if (!containsKey(key)) {
            size++;
        }

        T previousValue = get(key);
        jsMap.putMapEntry(key, new InternalMapEntry<T>(key, value));
        return previousValue;
    }

    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        for (Map.Entry<? extends String, ? extends T> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public T remove(Object key) {
        if (!containsKey(key)) {
            return null;
        }

        assert key instanceof String;

        size--;

        T previousValue = get(key);
        jsMap.remove(key);
        return previousValue;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        String result = "JavaScriptStringToObjectMap[";
        for (Entry<String, T> entry : entrySet) {
            result += entry.getKey();
            result += "=";
            result += entry.getValue();
            result += ";";
        }
        return result + "]";
    }

    @Override
    public Collection<T> values() {
        return values;
    }

}
