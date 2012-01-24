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

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @deprecated Use NumberArray and the GWT JsArray classes instead
 */
@Deprecated
public class ArrayUtils {

    // @formatter:off
    public native static void add(Object o, JavaScriptObject array) /*-{
        array.push(o);
    }-*/;

    public native static JavaScriptObject createArray() /*-{
        return new Array();
    }-*/;

    public native static int length(JavaScriptObject array) /*-{
        return array.length;
    }-*/;
    // @formatter:on

    public static double max(double[] values) {
        assert values.length > 0;

        double max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    // XXX using .doubleValue could be a problem at some point
    // e.g. with long values and precision
    public static <T extends Number> T max(List<T> objectList) {
        assert !objectList.isEmpty();

        if (objectList.isEmpty()) {
            throw new IllegalArgumentException("Number List was empty.");
        }
        T max = objectList.get(0);
        for (int i = 1; i < objectList.size(); i++) {
            if (objectList.get(i).doubleValue() > max.doubleValue()) {
                max = objectList.get(i);
            }
        }
        return max;
    }

    public static double min(double[] values) {
        assert values.length > 0;

        double min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    // XXX using .doubleValue could be a problem at some point
    // e.g. with long values and precision
    public static <T extends Number> T min(List<T> objectList) {
        assert !objectList.isEmpty();

        if (objectList.isEmpty()) {
            throw new IllegalArgumentException("Number List was empty.");
        }
        T min = objectList.get(0);
        for (int i = 1; i < objectList.size(); i++) {
            if (objectList.get(i).doubleValue() < min.doubleValue()) {
                min = objectList.get(i);
            }
        }
        return min;
    }

    public native static void pushArray(JavaScriptObject array, double d) /*-{
                                                                          array.push(d);
                                                                          }-*/;

    public native static void pushArray(JavaScriptObject array, int i) /*-{
                                                                       array.push(i);
                                                                       }-*/;

    public native static void pushArray(JavaScriptObject array, String o) /*-{
                                                                          array.push(o);
                                                                          }-*/;

    public native static void remove(Object o, JavaScriptObject array) /*-{
                                                                       var index = array.indexOf(o);
                                                                       if (index != -1) {
                                                                       array.splice(index, 1);
                                                                       }
                                                                       }-*/;

    public static JavaScriptObject toJsArray(List<? extends Object> objectList) {
        JavaScriptObject result = createArray();
        for (int i = 0; i < objectList.size(); i++) {
            add(objectList.get(i), result);
        }
        return result;
    }

    public static JavaScriptObject toJsArray(Object[] array) {
        JavaScriptObject result = createArray();
        for (int i = 0; i < array.length; i++) {
            add(array[i], result);
        }
        return result;
    }

    public static JavaScriptObject toJsArray(String[] array) {
        JavaScriptObject result = createArray();
        for (int i = 0; i < array.length; i++) {
            pushArray(result, array[i]);
        }
        return result;
    }

}
