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

import java.util.Comparator;

// TODO move to core
public class InvertedComparator<T> implements Comparator<T> {

    private Comparator<T> delegate;

    public InvertedComparator(Comparator<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int compare(T o1, T o2) {
        return delegate.compare(o2, o1);
    };

}