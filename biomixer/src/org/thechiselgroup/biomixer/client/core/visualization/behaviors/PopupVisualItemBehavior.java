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

import org.thechiselgroup.biomixer.client.core.resources.ui.DetailsWidgetHelper;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManager;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManagerFactory;
import org.thechiselgroup.biomixer.client.core.util.DisposeUtil;
import org.thechiselgroup.biomixer.client.core.visualization.model.MappedHandlerVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;

import com.google.gwt.dom.client.NativeEvent;

/**
 * Manages {@link VisualItem} popups in a single view.
 */
public class PopupVisualItemBehavior extends
        MappedHandlerVisualItemBehavior<PopupManager> {

    private DetailsWidgetHelper detailsWidgetHelper;

    private PopupManagerFactory popupManagerFactory;

    public PopupVisualItemBehavior(DetailsWidgetHelper detailsWidgetHelper,
            PopupManagerFactory popupManagerFactory, DisposeUtil disposeUtil) {
        super(disposeUtil);
        assert detailsWidgetHelper != null;
        assert popupManagerFactory != null;

        this.detailsWidgetHelper = detailsWidgetHelper;
        this.popupManagerFactory = popupManagerFactory;
    }

    @Override
    protected PopupManager createHandler(VisualItem visualItem) {
        return popupManagerFactory.createPopupManager(detailsWidgetHelper
                .createDetailsWidget(visualItem));
    }

    @Override
    protected void onInteraction(VisualItem visualItem,
            VisualItemInteraction interaction, PopupManager popupManager) {

        switch (interaction.getEventType()) {
        case DRAG_START:
            popupManager.hidePopup();
            break;
        case MOUSE_MOVE:
            popupManager.onMouseMove(interaction.getClientX(),
                    interaction.getClientY());
            break;
        case MOUSE_DOWN:
            if (interaction.hasNativeEvent()) {
                NativeEvent nativeEvent = interaction.getNativeEvent();
                popupManager.onMouseDown(nativeEvent);
            }
            break;
        case MOUSE_OUT:
            popupManager.onMouseOut(interaction.getClientX(),
                    interaction.getClientY());
            break;
        case MOUSE_OVER:
            popupManager.onMouseOver(interaction.getClientX(),
                    interaction.getClientY());
            break;
        }
    }

}