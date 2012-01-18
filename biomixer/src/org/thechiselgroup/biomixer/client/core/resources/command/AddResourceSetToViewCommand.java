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

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.command.AbstractUndoableCommand;
import org.thechiselgroup.biomixer.client.core.development.DevelopmentSettings;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.HasDescription;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Window;

/**
 * Adds a labeled resource set to a resource model as an explicitly displayed
 * resource set.
 */
public class AddResourceSetToViewCommand extends AbstractUndoableCommand
        implements HasDescription {

    private List<Resource> alreadyContainedResources;

    private String description;

    private ResourceSet resourceSet;

    protected ResourceModel resourceModel;

    public AddResourceSetToViewCommand(ResourceModel resourceModel,
            ResourceSet resourceSet) {

        this(resourceModel, resourceSet, "Add resource set '"
                + resourceSet.getLabel() + "' to resource model");
    }

    public AddResourceSetToViewCommand(ResourceModel resourceModel,
            ResourceSet resourceSet, String description) {

        assert resourceModel != null;
        assert resourceSet != null;
        assert resourceSet.hasLabel();
        assert description != null;

        this.description = description;
        this.resourceModel = resourceModel;
        this.resourceSet = resourceSet;
    }

    // TODO add view name / label once available
    @Override
    public String getDescription() {
        return description;
    }

    public ResourceModel getResourceModel() {
        return resourceModel;
    }

    public ResourceSet getResourceSet() {
        return resourceSet;
    }

    @Override
    public void performExecute() {
        assert !resourceModel.containsResourceSet(resourceSet);
        assert alreadyContainedResources == null;

        alreadyContainedResources = new ArrayList<Resource>();
        alreadyContainedResources.addAll(resourceSet.toList());
        alreadyContainedResources.retainAll(resourceModel.getResources()
                .toList());

        if (DevelopmentSettings.isBenchmarkEnabled()) {
            Duration duration = new Duration();
            resourceModel.addResourceSet(resourceSet);
            Window.alert("ResourceModel.addResourceSet execution time: "
                    + duration.elapsedMillis() + " ms");
        } else {
            resourceModel.addResourceSet(resourceSet);
        }

        assert resourceModel.containsResourceSet(resourceSet);
        assert alreadyContainedResources != null;
    }

    @Override
    public void performUndo() {
        assert resourceModel.containsResourceSet(resourceSet);
        assert alreadyContainedResources != null;

        resourceModel.removeResourceSet(resourceSet);
        resourceModel.addUnnamedResources(alreadyContainedResources);
        alreadyContainedResources = null;

        assert !resourceModel.containsResourceSet(resourceSet);
        assert alreadyContainedResources == null;
    }

}