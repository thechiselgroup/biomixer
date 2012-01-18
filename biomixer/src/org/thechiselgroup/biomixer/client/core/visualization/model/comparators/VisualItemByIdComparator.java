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

import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

public class VisualItemByIdComparator implements Comparator<VisualItem> {

    @Override
    public int compare(VisualItem o1, VisualItem o2) {
        return o1.getId().compareTo(o2.getId());
    }

}