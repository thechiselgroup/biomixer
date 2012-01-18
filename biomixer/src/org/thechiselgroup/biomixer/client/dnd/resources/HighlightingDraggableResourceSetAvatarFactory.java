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

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ui.HighlightingResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;

/**
 * This class should be used instead of
 * {@link HighlightingResourceSetAvatarFactory} if the resource set avatars are
 * draggable.
 * 
 * @author Lars Grammel
 */
public class HighlightingDraggableResourceSetAvatarFactory extends
        HighlightingResourceSetAvatarFactory {

    private final ResourceSetAvatarDragController dragController;

    public HighlightingDraggableResourceSetAvatarFactory(
            ResourceSetAvatarFactory delegate, HighlightingModel hoverModel,
            ResourceSetAvatarDragController dragController) {

        super(delegate, hoverModel);
        assert dragController != null;
        this.dragController = dragController;
    }

    private void addDragHandler(DragHandler handler) {
        dragController.addDragHandler(handler);
    }

    @Override
    public ResourceSetAvatar createAvatar(ResourceSet resources) {
        final ResourceSetAvatar avatar = super.createAvatar(resources);

        /**
         * Removes the hover at the end of a drag and drop operation. Because
         * the resource set is already hovered, this saves the effort of
         * highlighting the resources again.
         */
        final DragHandlerAdapter dragHandler = new DragHandlerAdapter() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                removeFromHover();
            }

        };

        addDragHandler(dragHandler);

        avatar.addDisposable(new Disposable() {
            @Override
            public void dispose() {
                removeDragHandler(dragHandler);
            }
        });

        return avatar;
    }

    private void removeDragHandler(DragHandler handler) {
        dragController.removeDragHandler(handler);
    }

}
