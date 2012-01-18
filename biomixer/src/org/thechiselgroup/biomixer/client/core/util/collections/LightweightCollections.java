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

public final class LightweightCollections {

    public static <T> LightweightCollection<T> copy(
            Iterable<? extends T> iterable) {

        return toList(iterable);
    }

    public static <T> LightweightCollection<T> emptyCollection() {
        return NullLightweightCollection.nullLightweightCollection();
    }

    public static <T> LightweightCollection<T> emptySet() {
        return emptyCollection();
    }

    public static <T> LightweightCollection<T> toCollection(
            Iterable<? extends T> values) {

        return toList(values);
    }

    public static <T> LightweightCollection<T> toCollection(T... values) {
        return toList(values);
    }

    public static <T> LightweightList<T> toList(Iterable<? extends T> values) {
        LightweightList<T> result = CollectionFactory.createLightweightList();
        for (T t : values) {
            result.add(t);
        }
        return result;
    }

    public static <T> LightweightList<T> toList(T... values) {
        LightweightList<T> result = CollectionFactory.createLightweightList();
        for (T t : values) {
            result.add(t);
        }
        return result;
    }

    private LightweightCollections() {
    }

    /**
     * Calculates the elements in {@code originalElements} that are not
     * contained in {@code elementsToRemove}.
     * 
     * @return {@code originalElements \ elementsToRemove} ({@code A \ B}, also
     *         known as {@code A - B}). This method does not modify any
     *         parameter.
     * 
     * @see <a
     *      href="http://en.wikipedia.org/wiki/Complement_(set_theory)">Wikipedia
     *      Complement (set theory)</a>
     */
    public static <T> LightweightCollection<T> getRelativeComplement(
            LightweightCollection<T> originalElements,
            LightweightCollection<T> elementsToRemove) {
    
        assert originalElements != null;
        assert elementsToRemove != null;
    
        LightweightList<T> relativeComplement = CollectionFactory
                .createLightweightList();
        for (T element : originalElements) {
            if (!elementsToRemove.contains(element)) {
                relativeComplement.add(element);
            }
        }
        return relativeComplement;
    }

}