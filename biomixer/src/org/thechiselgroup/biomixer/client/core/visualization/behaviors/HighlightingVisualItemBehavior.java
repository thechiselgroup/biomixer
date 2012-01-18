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
package org.thechiselgroup.biomixer.client.core.visualization.behaviors;

import org.thechiselgroup.biomixer.client.core.visualization.model.MappedHandlerVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;

/**
 * Manages {@link VisualItem} highlighting in a single view.
 */
public class HighlightingVisualItemBehavior extends
        MappedHandlerVisualItemBehavior<HighlightingManager> {

    private HighlightingModel highlightingModel;

    public HighlightingVisualItemBehavior(HighlightingModel highlightingModel) {
        assert highlightingModel != null;

        this.highlightingModel = highlightingModel;
    }

    @Override
    protected HighlightingManager createHandler(VisualItem visualItem) {
        return new HighlightingManager(highlightingModel,
                visualItem.getResources());
    }

    @Override
    protected void onInteraction(VisualItem visualItem,
            VisualItemInteraction interaction, HighlightingManager manager) {

        switch (interaction.getEventType()) {
        case DRAG_END:
        case MOUSE_OUT:
            setHighlighting(manager, visualItem, false);
            break;
        case MOUSE_OVER:
            setHighlighting(manager, visualItem, true);
            break;
        }
    }

    /**
     * Hook method for subclasses
     */
    protected void setHighlighting(HighlightingManager manager,
            VisualItem visualItem, boolean shouldHighlight) {

        manager.setHighlighting(shouldHighlight);
    }

}
