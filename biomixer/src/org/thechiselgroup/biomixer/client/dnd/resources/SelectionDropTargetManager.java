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
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupFactory;
import org.thechiselgroup.biomixer.client.core.visualization.ViewAccessor;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SelectionDropTargetManager extends
        AbstractResourceSetAvatarDropTargetManager {

    @Inject
    public SelectionDropTargetManager(CommandManager commandManager,
            ResourceSetAvatarDragController dragController,
            ViewAccessor viewAccessor,
            DropTargetCapabilityChecker capabilityChecker,
            PopupFactory popupFactory) {

        super(commandManager, dragController, viewAccessor, capabilityChecker,
                popupFactory);
    }

    @Override
    protected ResourceSetAvatarDropCommandFactory createCommandFactory(
            Widget dropTarget, ViewAccessor viewAccessor) {

        assert dropTarget instanceof ResourceSetAvatar;

        return new SelectionPresenterDropCommandFactory(dropTarget,
                viewAccessor);
    }
}
