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

import org.thechiselgroup.biomixer.client.BioMixerDetailsWidgetHelper.VisualItemVerticalPanel;
import org.thechiselgroup.biomixer.client.core.fx.Opacity;
import org.thechiselgroup.biomixer.client.core.resources.ui.DetailsWidgetHelper;
import org.thechiselgroup.biomixer.client.core.ui.popup.Popup;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManager;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManagerFactory;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupOpacityChangedEvent;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupOpacityChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.util.DisposeUtil;
import org.thechiselgroup.biomixer.client.core.visualization.model.MappedHandlerVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 * Manages {@link VisualItem} popups in a single view.
 */
public class PopupVisualItemBehavior extends
        MappedHandlerVisualItemBehavior<PopupManager> implements
        PopupOpacityChangedEventHandler {

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
        // The contents of the widget will get refreshed via the opacity change listener
        Widget detailsWidget = detailsWidgetHelper
                .createDetailsWidget(visualItem);
        PopupManager popupManager = popupManagerFactory
                .createPopupManager(detailsWidget);
        
        Popup popup = popupManager.getPopup();
        popup.addHandler(this, PopupOpacityChangedEvent.TYPE);
        VisualItemVerticalPanel contentWidget = (VisualItemVerticalPanel) (popup
                .getContentWidget());
        contentWidget.setVisualItem(visualItem);

        return popupManager;
    }

    @Override
    public void onOpacityChangeStarted(PopupOpacityChangedEvent event) {
        // When the tool tip has become completely visible, and only then, we
        // want to ensure that the most up to date info is loaded. This was
        // motivated by the number of concepts for an ontology being loaded
        // after the tool tip has already been constructed.
        if (event.getOpacity() == Opacity.SEMI_TRANSPARENT) {
            // This triggers as the opacity animation ends, because the
            // SEMI_TRANSPARENT is the end point.
            // The event is currently only fired at three opacity levels (those
            // defined as constants within the Opacity class), so it's not
            // wasteful.
            Popup popup = event.getPopup();

            VisualItemVerticalPanel contentWidget = (VisualItemVerticalPanel) (popup
                    .getContentWidget());
            VisualItem visualItem = contentWidget.getVisualItem();
            detailsWidgetHelper.refreshDetailsWidget(visualItem, contentWidget);
        } else if (event.getOpacity() == Opacity.OPAQUE) {
            // Nothing right now...
        } else if (event.getOpacity() == Opacity.TRANSPARENT) {
            // Nothing right now...
        }
    }

    @Override
    protected void onInteraction(VisualItem visualItem,
            VisualItemInteraction interaction, PopupManager popupManager) {

        switch (interaction.getEventType()) {
        case DRAG_START:
            // Tried fixing up Chrome dragging here with preventDefault() and
            // stopPropagation(), but had success more generally elsewhere.
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