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

import org.thechiselgroup.biomixer.client.core.visualization.model.MappedHandlerVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;

/**
 * Manages dragging of {@link VisualItem}.
 */
public class DragVisualItemBehavior extends
        MappedHandlerVisualItemBehavior<DragEnabler> {

    private DragEnablerFactory dragEnablerFactory;

    public DragVisualItemBehavior(DragEnablerFactory dragEnablerFactory) {
        assert dragEnablerFactory != null;

        this.dragEnablerFactory = dragEnablerFactory;
    }

    @Override
    protected DragEnabler createHandler(VisualItem visualItem) {
        return dragEnablerFactory.createDragEnabler(visualItem);
    }

    @Override
    protected void onInteraction(VisualItem visualItem,
            VisualItemInteraction interaction, DragEnabler enabler) {

        switch (interaction.getEventType()) {
        case MOUSE_MOVE:
            enabler.onMoveInteraction(interaction);
            break;
        case MOUSE_DOWN:
            if (interaction.hasNativeEvent()) {
                enabler.forwardMouseDownWithEventPosition(interaction
                        .getNativeEvent());
            }
            break;
        case MOUSE_OUT:
            if (interaction.hasNativeEvent()) {
                enabler.forwardMouseOut(interaction.getNativeEvent());
            }
            break;
        case MOUSE_UP:
            if (interaction.hasNativeEvent()) {
                enabler.forwardMouseUp(interaction.getNativeEvent());
            }
            break;
        }
    }

}