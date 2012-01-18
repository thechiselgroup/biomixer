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
package org.thechiselgroup.biomixer.client.core.visualization.model.comparators;

import java.util.Comparator;

import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

public class VisualItemTextSlotComparator implements Comparator<VisualItem> {

    private Slot slot;

    public VisualItemTextSlotComparator(Slot slot) {
        this.slot = slot;
    }

    @Override
    public int compare(VisualItem o1, VisualItem o2) {
        String s1 = o1.<String> getValue(slot);
        String s2 = o2.<String> getValue(slot);
        return s1.compareTo(s2);
    }

}