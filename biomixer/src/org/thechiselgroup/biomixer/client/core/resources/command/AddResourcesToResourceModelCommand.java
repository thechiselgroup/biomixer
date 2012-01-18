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

import org.thechiselgroup.biomixer.client.core.command.AbstractUndoableCommand;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.HasDescription;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

/**
 * Adds resources to a resource model - not explictly, but to unnamed set.
 */
public class AddResourcesToResourceModelCommand extends AbstractUndoableCommand
        implements HasDescription {

    private LightweightList<Resource> addedResources;

    private ResourceSet resources;

    private ResourceModel resourceModel;

    public AddResourcesToResourceModelCommand(ResourceModel resourceModel,
            ResourceSet resources) {

        assert resourceModel != null;
        assert resources != null;

        this.resourceModel = resourceModel;
        this.resources = resources;
    }

    // TODO add view name / label once available
    @Override
    public String getDescription() {
        return "Add resources (" + resources.getLabel() + ") to view";
    }

    @Override
    public void performExecute() {
        assert addedResources == null;

        ResourceSet viewResources = resourceModel.getResources();
        addedResources = CollectionFactory.createLightweightList();
        for (Resource resource : resources) {
            if (!viewResources.contains(resource)) {
                addedResources.add(resource);
            }
        }

        resourceModel.addUnnamedResources(addedResources);

        assert addedResources != null;
        assert resourceModel.containsResources(resources);
    }

    @Override
    public void performUndo() {
        assert addedResources != null;
        assert resourceModel.containsResources(resources);

        resourceModel.removeUnnamedResources(addedResources);
        addedResources = null;

        assert addedResources == null;
    }

}