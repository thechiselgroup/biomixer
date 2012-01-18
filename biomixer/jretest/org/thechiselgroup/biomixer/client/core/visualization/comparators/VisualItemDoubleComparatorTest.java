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
package org.thechiselgroup.biomixer.client.core.visualization.comparators;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.visualization.model.comparators.VisualItemDoubleComparator;

public class VisualItemDoubleComparatorTest {

    @Test
    public void greater() {
        assertEquals(1, VisualItemDoubleComparator.compare(3, 1));
    }

    @Test
    public void less() {
        assertEquals(-1, VisualItemDoubleComparator.compare(-1, 2));
    }

    @Test
    public void orderIsStable1() {
        assertEquals(0, VisualItemDoubleComparator.compare(-1, -1));
    }

    @Test
    public void orderIsStable2() {
        assertEquals(0, VisualItemDoubleComparator.compare(1, 1));
    }

}