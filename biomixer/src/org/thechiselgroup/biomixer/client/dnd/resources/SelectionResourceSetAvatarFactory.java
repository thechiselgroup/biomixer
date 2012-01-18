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

import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.command.ReplaceSelectionCommand;
import org.thechiselgroup.biomixer.client.core.resources.ui.DelegatingResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.visualization.ViewAccessor;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.SelectionModel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class SelectionResourceSetAvatarFactory extends
        DelegatingResourceSetAvatarFactory {

    private CommandManager commandManager;

    private ViewAccessor viewAccessor;

    public SelectionResourceSetAvatarFactory(ResourceSetAvatarFactory delegate,
            ViewAccessor viewAccessor, CommandManager commandManager) {

        super(delegate);

        this.viewAccessor = viewAccessor;
        this.commandManager = commandManager;
    }

    @Override
    public ResourceSetAvatar createAvatar(ResourceSet resources) {
        final ResourceSetAvatar avatar = delegate.createAvatar(resources);

        avatar.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SelectionModel selectionModel = viewAccessor.findView(avatar)
                        .getSelectionModel();
                setSelection(selectionModel, avatar.getResourceSet());
            }
        });

        return avatar;
    }

    protected void setSelection(SelectionModel selectionModel,
            ResourceSet resourceSet) {

        ResourceSet result = resourceSet;
        if (resourceSet.equals(selectionModel.getSelection())) {
            result = null;
        }
        commandManager.execute(new ReplaceSelectionCommand(selectionModel,
                result));
    }
}