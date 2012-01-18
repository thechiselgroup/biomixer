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

import java.util.List;

import org.thechiselgroup.biomixer.client.core.command.AbstractUndoableCommand;
import org.thechiselgroup.biomixer.client.core.command.UndoableCommand;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.HasDescription;

/**
 * {@link UndoableCommand} that replaces the resources in one
 * {@link ResourceSet} with the resource from another {@link ResourceSet}.
 * 
 * @author Lars Grammel
 */
public class ReplaceResourceSetContentCommand extends AbstractUndoableCommand
        implements HasDescription {

    private List<Resource> originalTargetResources;

    private ResourceSet newContent;

    private ResourceSet resourceSet;

    public ReplaceResourceSetContentCommand(ResourceSet resourceSet,
            ResourceSet newContent) {

        assert newContent != null;
        assert resourceSet != null;

        this.newContent = newContent;
        this.resourceSet = resourceSet;
    }

    @Override
    public String getDescription() {
        return "Replace content of resource set '" + resourceSet.getLabel()
                + "' with content from '" + newContent.getLabel() + "'";
    }

    @Override
    public void performExecute() {
        if (originalTargetResources == null) {
            originalTargetResources = resourceSet.toList();
        }

        resourceSet.clear();
        resourceSet.addAll(newContent);

        assert resourceSet.containsAll(newContent);
        assert resourceSet.size() == newContent.size();
    }

    @Override
    public void performUndo() {
        resourceSet.clear();
        resourceSet.addAll(originalTargetResources);
    }

}