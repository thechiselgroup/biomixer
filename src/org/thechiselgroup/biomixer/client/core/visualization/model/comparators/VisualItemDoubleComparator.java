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
package org.thechiselgroup.biomixer.client.core.visualization.model.comparators;

import java.util.Comparator;

import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

public class VisualItemDoubleComparator implements Comparator<VisualItem> {

    // for test access
    public static int compare(double value1, double value2) {
        if (value1 > value2) {
            return 1;
        }

        if (value1 < value2) {
            return -1;
        }

        return 0;
    }

    private Slot slot;

    public VisualItemDoubleComparator(Slot slot) {
        assert slot != null;
        this.slot = slot;
    }

    @Override
    public int compare(VisualItem o1, VisualItem o2) {
        assert o1 != null;
        assert o2 != null;

        // TODO just compare numbers instead.
        double v1 = ((Number) o1.getValue(slot)).doubleValue();
        double v2 = ((Number) o2.getValue(slot)).doubleValue();
        return compare(v1, v2);
    }

}