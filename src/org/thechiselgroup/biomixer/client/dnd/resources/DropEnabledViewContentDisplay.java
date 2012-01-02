/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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

import org.thechiselgroup.choosel.core.client.visualization.model.DelegatingViewContentDisplay;
import org.thechiselgroup.choosel.core.client.visualization.model.ViewContentDisplay;
import org.thechiselgroup.choosel.core.client.visualization.model.ViewContentDisplayCallback;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItemContainer;

public class DropEnabledViewContentDisplay extends DelegatingViewContentDisplay {

    private ResourceSetAvatarDropTargetManager dropTargetManager;

    public DropEnabledViewContentDisplay(ViewContentDisplay delegate,
            ResourceSetAvatarDropTargetManager dropTargetManager) {

        super(delegate);

        assert dropTargetManager != null;

        this.dropTargetManager = dropTargetManager;
    }

    @Override
    public void dispose() {
        dropTargetManager.disableDropTarget(asWidget());
        dropTargetManager = null;

        super.dispose();
    }

    @Override
    public void init(VisualItemContainer container,
            ViewContentDisplayCallback callback) {

        super.init(container, callback);

        dropTargetManager.enableDropTarget(asWidget());
    }

}
