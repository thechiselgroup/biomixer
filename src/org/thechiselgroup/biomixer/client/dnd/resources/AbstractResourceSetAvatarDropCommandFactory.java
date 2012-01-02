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

import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.choosel.core.client.visualization.View;
import org.thechiselgroup.choosel.core.client.visualization.ViewAccessor;

import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractResourceSetAvatarDropCommandFactory implements
        ResourceSetAvatarDropCommandFactory {

    private Widget dropTarget;

    private ViewAccessor viewAccessor;

    protected AbstractResourceSetAvatarDropCommandFactory(Widget dropTarget,
            ViewAccessor viewAccessor) {

        assert dropTarget != null;
        assert viewAccessor != null;

        this.dropTarget = dropTarget;
        this.viewAccessor = viewAccessor;
    }

    protected View findView(Widget widget) {
        assert widget != null;
        return viewAccessor.findView(widget);
    }

    /**
     * Returns {@link View} that contains the drop target.
     */
    protected View getTargetView() {
        return findView(dropTarget);
    }

    protected boolean isDragAvatarFromTargetView(ResourceSetAvatar dragAvatar) {
        return getTargetView() == findView(dragAvatar);
    }

}