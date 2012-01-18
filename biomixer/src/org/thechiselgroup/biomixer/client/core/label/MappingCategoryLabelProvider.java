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
package org.thechiselgroup.biomixer.client.core.label;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;

public class MappingCategoryLabelProvider implements CategoryLabelProvider {

    private final Map<String, String> categoriesToLabels = CollectionFactory
            .createStringMap();

    @Override
    public String getLabel(String category) {
        if (!categoriesToLabels.containsKey(category)) {
            return category;
        }

        return categoriesToLabels.get(category);
    }

    public String mapCategoryToLabel(String category, String label) {
        return categoriesToLabels.put(category, label);
    }

}
