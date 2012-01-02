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
package org.thechiselgroup.biomixer.client.core.resources.command;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

public class MergeResourceSetsCommand extends
        AddResourceSetToResourceSetCommand {

    private ResourceModel resourceModel;

    public MergeResourceSetsCommand(ResourceSet sourceSet,
            ResourceSet targetSet, ResourceModel resourceModel) {

        super(sourceSet, targetSet);

        assert resourceModel != null;
        this.resourceModel = resourceModel;
    }

    @Override
    public String getDescription() {
        return "Merge resource set '" + addedSet.getLabel()
                + "' into resource set '" + modifiedSet.getLabel() + "'";
    }

    public ResourceModel getResourceModel() {
        return resourceModel;
    }

    @Override
    public void performExecute() {
        super.performExecute();

        resourceModel.removeResourceSet(addedSet);
    }

    @Override
    public void performUndo() {
        super.performUndo();

        resourceModel.addResourceSet(addedSet);
    }
}