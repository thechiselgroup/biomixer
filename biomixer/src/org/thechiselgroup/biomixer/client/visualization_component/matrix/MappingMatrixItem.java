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
package org.thechiselgroup.biomixer.client.visualization_component.matrix;

import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

// TODO set visible method
public class MappingMatrixItem {

	// See NodeItem and ArcItem for what this can be used for.

    private final VisualItem visualItem;

    public MappingMatrixItem(VisualItem visualItem) {

        assert visualItem != null;
        assert Mapping.isMapping(visualItem.getResources().getFirstElement());
        this.visualItem = visualItem;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MappingMatrixItem other = (MappingMatrixItem) obj;
        return true;
    }

  

    @Override
    public String toString() {
        return "MappingMatrixItem: "+visualItem.toString();
    }

}
