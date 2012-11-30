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
package org.thechiselgroup.biomixer.client.dnd.resources;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetUtils;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;

public class DefaultDropTargetCapabilityChecker implements
        DropTargetCapabilityChecker {

    private List<DataType> getDataTypes(Slot[] slots) {
        List<DataType> dataTypes = new ArrayList<DataType>();
        for (Slot slot : slots) {
            if (!dataTypes.contains(slot.getDataType())) {
                dataTypes.add(slot.getDataType());
            }
        }
        return dataTypes;
    }

    @Override
    public boolean isValidDrop(Slot[] slots, ResourceSet resourceSet,
            VisualizationModel model) {
        assert slots != null : "slots must not be null";
        assert resourceSet != null : "resourceSet must not be null";
        assert model != null : "model must not be null";

        if (!model.getViewContentDisplay().validateDataTypes(resourceSet)) {
            return false;
        }

        List<DataType> dataTypes = getDataTypes(slots);

        // XXX re-enable color resolver?
        // TODO this should use the slot mapping initializer of the view somehow
        dataTypes.remove(DataType.COLOR);
        dataTypes.remove(DataType.NUMBER);

        for (DataType dataType : dataTypes) {
            if (ResourceSetUtils.getPropertyNamesForDataType(resourceSet,
                    dataType).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
