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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class CollectionUtils {

    public static List<String> asSortedList(Set<String> strings) {
        List<String> sortedList = new ArrayList<String>(strings);
        Collections.sort(sortedList);
        return sortedList;
    }

    // TODO unify with intersects
    public static <T> boolean containsNone(Collection<T> container,
            Collection<T> other) {

        assert container != null;
        assert other != null;

        for (T t : other) {
            if (container.contains(t)) {
                return false;
            }
        }

        return true;
    }

    public static <T> boolean contentEquals(Collection<T> l1, Collection<T> l2) {
        return l1.containsAll(l2) && l1.size() == l2.size();
    }

    public static <T> boolean contentEquals(Iterable<T> i1, Iterable<T> i2) {
        return contentEquals(toList(i1), toList(i2));
    }

    public static String deliminateIterableStringCollection(
            Iterable<String> values, String delimeter) {

        assert values != null;
        assert delimeter != null;

        Iterator<String> iterator = values.iterator();

        if (!iterator.hasNext()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        result.append(iterator.next());

        while (iterator.hasNext()) {
            result.append(delimeter).append(iterator.next());
        }
        return result.toString();
    }

    /**
     * @return intersection of {@code collection1} and {@code collection2}.
     */
    public static <T> Collection<T> getIntersection(Collection<T> collection1,
            Collection<T> collection2) {

        Collection<T> intersection = new ArrayList<T>(collection1);
        intersection.retainAll(collection2);
        return intersection;
    }

    // TODO unify with containsNone
    public static <T> boolean intersects(Collection<T> collection1,
            Collection<T> collection2) {

        return !CollectionUtils.getIntersection(collection1, collection2)
                .isEmpty();
    }

    /**
     * splits a string on a regex delimeter and converts the resulting array to
     * a List
     */
    public static List<String> splitStringToList(String input, String regex) {
        if (input == null) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<String>();
        for (String value : input.split(regex)) {
            results.add(value.trim());
        }

        return results;
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new ArrayList<T>();
        for (T t : iterable) {
            result.add(t);
        }
        return result;
    }

    public static <T> List<T> toList(T... ts) {
        List<T> list = new ArrayList<T>();
        for (T t : ts) {
            list.add(t);
        }
        return list;
    }

    public static <T> Set<T> toSet(Collection<T> collection) {
        Set<T> set = new HashSet<T>();
        set.addAll(collection);
        return set;
    }

    public static Set<String> toSet(String... ts) {
        Set<String> set = CollectionFactory.createStringSet();
        for (String s : ts) {
            set.add(s);
        }
        return set;
    }

    public static <T> Set<T> toSet(T... ts) {
        Set<T> set = new HashSet<T>();
        for (T t : ts) {
            set.add(t);
        }
        return set;
    }

    private CollectionUtils() {

    }

}
