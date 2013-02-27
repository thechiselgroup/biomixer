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
package org.thechiselgroup.biomixer.client.core.visualization.behaviors.rendered_items;

import org.thechiselgroup.biomixer.client.BioMixerDetailsWidgetRenderedArcHelper;
import org.thechiselgroup.biomixer.client.BioMixerDetailsWidgetRenderedArcHelper.RenderedArcVerticalPanel;
import org.thechiselgroup.biomixer.client.core.fx.Opacity;
import org.thechiselgroup.biomixer.client.core.ui.popup.Popup;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManager;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManagerFactory;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupOpacityChangedEvent;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupOpacityChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.util.DisposeUtil;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.GraphDisplayController;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RenderedItemPopupManager implements
        PopupOpacityChangedEventHandler {

    @Inject
    private BioMixerDetailsWidgetRenderedArcHelper detailsWidgetHelper;

    @Inject
    private PopupManagerFactory popupManagerFactory;

    @Inject
    private DisposeUtil disposeUtil;

    private RenderedArc currentArcToDisplayPopup;

    protected PopupManager currentPopupManager;

    @Inject
    public RenderedItemPopupManager() {
    }

    static public class ArcChooselEventHandler implements ChooselEventHandler {
        private final RenderedArc renderedArc;

        private final RenderedItemPopupManager renderedItemPopupManager;

        private final GraphDisplayController graphDisplayController;

        public ArcChooselEventHandler(RenderedArc renderedArc,
                RenderedItemPopupManager popupManager,
                GraphDisplayController graphDisplayController) {
            this.renderedArc = renderedArc;
            this.renderedItemPopupManager = popupManager;
            this.graphDisplayController = graphDisplayController;

        }

        @Override
        public void onEvent(ChooselEvent event) {
            // TODO Not sure how to handle this. I need to create and get rid of
            // popupmanagers as arcs are hovered and unhovered, but I think I
            // have to pass events on to the popupmanagers as well. Is any
            // timing important here?
            if (event.getEventType().equals(ChooselEvent.Type.MOUSE_OVER)) {
                graphDisplayController.onArcMouseOver(renderedArc);
                renderedItemPopupManager.addArcDelayedPopup(renderedArc);
            }

            switch (event.getEventType()) {
            case DRAG_START:
                // Tried fixing up Chrome dragging here with preventDefault()
                // and
                // stopPropagation(), but had success more generally elsewhere.
                renderedItemPopupManager.currentPopupManager.hidePopup();
                break;
            case MOUSE_MOVE:
                renderedItemPopupManager.currentPopupManager.onMouseMove(
                        event.getClientX(), event.getClientY());
                break;
            // case MOUSE_DOWN:
            // if (interaction.hasNativeEvent()) {
            // NativeEvent nativeEvent = interaction.getNativeEvent();
            // popupManager.onMouseDown(nativeEvent);
            // }
            // break;
            case MOUSE_OUT:
                renderedItemPopupManager.currentPopupManager.onMouseOut(
                        event.getClientX(), event.getClientY());
                break;
            case MOUSE_OVER:
                renderedItemPopupManager.currentPopupManager.onMouseOver(
                        event.getClientX(), event.getClientY());
                break;
            }

            if (event.getEventType().equals(ChooselEvent.Type.MOUSE_OUT)) {
                graphDisplayController.onArcMouseOut(renderedArc);
                renderedItemPopupManager.removeArcDelayedPopup(renderedArc);
            }
        }
    }

    public void addArcDelayedPopup(RenderedArc renderedArc) {
        if (null != renderedArc
                && renderedArc.equals(this.currentArcToDisplayPopup)) {
            return;
        }
        removeArcDelayedPopup(this.currentArcToDisplayPopup);
        this.currentArcToDisplayPopup = renderedArc;
        this.currentPopupManager = createHandler(renderedArc);
    }

    public void removeArcDelayedPopup(RenderedArc renderedArc) {
        if (null == this.currentArcToDisplayPopup
                || !renderedArc.equals(this.currentArcToDisplayPopup)) {
            return;
        }
        this.currentArcToDisplayPopup = null;
        this.currentPopupManager.hidePopup();
        // TODO Surely I must do something to dispose of this.
        disposeUtil.safelyDispose(this.currentPopupManager);
        this.currentPopupManager = null;

    }

    protected PopupManager createHandler(RenderedArc renderedArc) {
        // The contents of the widget will get refreshed via the opacity change
        // listener
        Widget detailsWidget = detailsWidgetHelper
                .createDetailsWidget(renderedArc);
        PopupManager popupManager = popupManagerFactory
                .createPopupManager(detailsWidget);

        Popup popup = popupManager.getPopup();
        popup.addHandler(this, PopupOpacityChangedEvent.TYPE);
        RenderedArcVerticalPanel contentWidget = (RenderedArcVerticalPanel) (popup
                .getContentWidget());
        contentWidget.setRenderedArc(renderedArc);

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
            // Popup popup = event.getPopup();
            //
            // VisualItemVerticalPanel contentWidget = (VisualItemVerticalPanel)
            // (popup
            // .getContentWidget());
            // VisualItem visualItem = contentWidget.getVisualItem();
            // detailsWidgetHelper.refreshDetailsWidget(visualItem,
            // contentWidget);
        } else if (event.getOpacity() == Opacity.OPAQUE) {
            // Nothing right now...
        } else if (event.getOpacity() == Opacity.TRANSPARENT) {
            // Nothing right now...
        }
    }
}