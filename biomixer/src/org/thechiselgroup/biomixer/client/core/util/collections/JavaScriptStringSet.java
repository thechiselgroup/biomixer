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
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Native JavaScript implements of a String set.
 * 
 * @author Lars Grammel
 * 
 * @param <T>
 *            value type
 * 
 * @see {@link "http://blog.xkoder.com/2008/07/10/javascript-associative-arrays-demystified/"}
 */
public final class JavaScriptStringSet implements Set<String> {

    private static final class NativeStringSet extends JavaScriptObject {

        /*
         * IMPLEMENTATION NOTE: although this internal map requires String keys,
         * we use objects to prevent unnecessary class casting. When profiling
         * in Chrome 8, casting turned out to be an expensive operation in GWT.
         * 
         * IMPLEMENTATION NOTE: we store the values in an internal object named
         * values to avoid conflicts with objects added by GWT automatically in
         * hosted mode on Chrome.
         */

        protected NativeStringSet() {
        }

        private native void add(Object key) /*-{
                                            this.values[key] = key;
                                            }-*/;

        // @formatter:off
        public native void clear() /*-{
            for (var key in this.values) {
                delete this.values[key];
            }
        }-*/;
        // @formatter:on

        private native boolean contains(Object key) /*-{
                                                    return key in this.values;
                                                    }-*/;

        // @formatter:off
        private native JsArrayString getAll() /*-{
            var result = [];
            for (var key in this.values) {
                result.push(key);
            }
            return result;
        }-*/;
        // @formatter:on 

        private native void remove(Object key) /*-{
                                               delete this.values[key];
                                               }-*/;

    }

    private int size = 0;

    private NativeStringSet jsStringSet;

    public JavaScriptStringSet() {
        jsStringSet = createNativeStringSet();
    }

    @Override
    public boolean add(String e) {
        if (e == null) {
            throw new NullPointerException();
        }

        if (contains(e)) {
            return false;
        }

        size++;
        jsStringSet.add(e);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        boolean changed = false;
        for (String e : c) {
            changed |= add(e);
        }
        return changed;
    }

    @Override
    public void clear() {
        jsStringSet.clear();
    }

    @Override
    public boolean contains(Object o) {
        return jsStringSet.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    private native NativeStringSet createNativeStringSet() /*-{
                                                           var nativeSet = new Object();
                                                           nativeSet.values = new Object();
                                                           return nativeSet;
                                                           }-*/;

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<String> iterator() {
        final JsArrayString keyArray = jsStringSet.getAll();

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
                JavaScriptStringSet.this.remove(keyArray.get(currentIndex - 1));
            }
        };
    }

    @Override
    public boolean remove(Object e) {
        if (!contains(e)) {
            return false;
        }

        assert e instanceof String;

        size--;
        jsStringSet.remove(e);
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            changed |= remove(o);
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (String s : this) {
            if (!c.contains(s)) {
                remove(s);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size()];
        JsArrayString values = jsStringSet.getAll();
        for (int i = 0; i < result.length; i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        JsArrayString values = jsStringSet.getAll();
        for (int i = 0; i < a.length; i++) {
            a[i] = (T) values.get(i);
        }
        return a;
    }

    @Override
    public String toString() {
        String result = "JavaScriptStringSet[";
        JsArrayString values = jsStringSet.getAll();
        for (int i = 0; i < values.length(); i++) {
            result += values.get(i);
            result += " ; ";
        }
        result += "]";
        return result;
    }
}
