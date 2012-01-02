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

import org.thechiselgroup.biomixer.client.dnd.DragProxyUtils;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.choosel.core.client.ui.CSS;
import org.thechiselgroup.choosel.core.client.ui.ZIndex;

import com.google.gwt.dom.client.Element;

public class DraggableResourceSetAvatar extends ResourceSetAvatar {

    private static final String CSS_DND_PROXY_ALPHA = "dndProxyAlpha";

    private ResourceSetAvatar latestProxy = null;

    /**
     * For proxies, this points to the original avatar.
     */
    private ResourceSetAvatar originalAvatar = null;

    public DraggableResourceSetAvatar(String text, String enabledCSSClass,
            ResourceSet resources, ResourceSetAvatarType type) {
        super(text, enabledCSSClass, resources, type);
    }

    public DraggableResourceSetAvatar(String text, String enabledCSSClass,
            ResourceSet resources, ResourceSetAvatarType type, Element element) {
        super(text, enabledCSSClass, resources, type, element);
    }

    public ResourceSetAvatar createProxy() {
        DraggableResourceSetAvatar clone = new DraggableResourceSetAvatar(
                getText(), enabledCSSClass, resourceSet, type);

        clone.addStyleName(getEnabledCSSClass());
        clone.addStyleName(CSS_DND_PROXY_ALPHA);
        clone.originalAvatar = this;

        CSS.setZIndex(clone.getElement(), ZIndex.DRAG_AVATAR);

        latestProxy = clone;
        return latestProxy;
    }

    @Override
    public void dispose() {
        super.dispose();

        originalAvatar = null;
        latestProxy = null;
    }

    private boolean isProxy() {
        return originalAvatar != null;
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        /*
         * Workaround for the problem that mouseout/over events do not get
         * triggered if a HTML element is created below the cursor. Events would
         * be hard to implement in this case, because the parent might not know
         * about the child (e.g. window might not know about some widget created
         * inside presenters).
         */
        if (isProxy()) {
            DragProxyUtils.fireDragProxyAttached(originalAvatar);
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        /*
         * Workaround for the problem that mouseout/over events do not get
         * triggered if a HTML element is created below the cursor. Events would
         * be hard to implement in this case, because the parent might not know
         * about the child (e.g. window might not know about some widget created
         * inside presenters).
         */
        if (isProxy()) {
            DragProxyUtils.fireDragProxyDetached(originalAvatar);
        }
    }

}
