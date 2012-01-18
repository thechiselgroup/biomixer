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

/**
 * Adds a resource set to another resource set by adding the missing resources
 * to the target set.
 */
public class AddResourceSetToResourceSetCommand extends AbstractUndoableCommand
        implements HasDescription {

    protected LightweightList<Resource> addedResources = null;

    protected ResourceSet addedSet;

    protected ResourceSet modifiedSet;

    public AddResourceSetToResourceSetCommand(ResourceSet addedSet,
            ResourceSet modifiedSet) {

        assert addedSet != null;
        assert modifiedSet != null;
        assert modifiedSet.isModifiable();

        this.addedSet = addedSet;
        this.modifiedSet = modifiedSet;
    }

    public ResourceSet getAddedSet() {
        return addedSet;
    }

    @Override
    public String getDescription() {
        return "Add resource set '" + addedSet.getLabel()
                + "' to resource set '" + modifiedSet.getLabel() + "'";
    }

    public ResourceSet getModifiedSet() {
        return modifiedSet;
    }

    @Override
    public void performExecute() {
        if (addedResources == null) {
            addedResources = CollectionFactory.createLightweightList();
            for (Resource resource : addedSet) {
                if (!modifiedSet.contains(resource)) {
                    addedResources.add(resource);
                }
            }
        }

        modifiedSet.addAll(addedResources);

        assert modifiedSet.containsAll(addedSet);
    }

    @Override
    public void performUndo() {
        assert modifiedSet.containsAll(addedSet);

        modifiedSet.removeAll(addedResources);
    }

}