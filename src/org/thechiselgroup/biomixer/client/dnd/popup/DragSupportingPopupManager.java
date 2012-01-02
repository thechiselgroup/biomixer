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
package org.thechiselgroup.biomixer.client.dnd.popup;

import org.thechiselgroup.biomixer.client.dnd.DragProxyAttachedEvent;
import org.thechiselgroup.biomixer.client.dnd.DragProxyAttachedEventHandler;
import org.thechiselgroup.biomixer.client.dnd.DragProxyDetachedEvent;
import org.thechiselgroup.biomixer.client.dnd.DragProxyDetachedEventHandler;
import org.thechiselgroup.choosel.core.client.ui.popup.DefaultPopupManager;
import org.thechiselgroup.choosel.core.client.ui.popup.Popup;

import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

public class DragSupportingPopupManager extends DefaultPopupManager {

    public DragSupportingPopupManager(Popup popup) {
        super(popup);

        /*
         * Sends a popup-mouse-out if a dnd operation that was started over the
         * popup is finished.
         */
        // TODO dispose registration?
        popup.addHandler(new DragProxyDetachedEventHandler() {
            @Override
            public void onDragProxyDetached(DragProxyDetachedEvent event) {
                if (isEnabled()) {
                    state.onPopupMouseOut(DragSupportingPopupManager.this);
                }
            }
        }, DragProxyDetachedEvent.TYPE);
    }

    @Override
    public <T extends Widget & HasAllMouseHandlers & HasAttachHandlers> HandlerRegistration linkToWidget(
            T widget) {

        final HandlerRegistration superRegistration = super
                .linkToWidget(widget);

        /*
         * Sends a source-mouse-out if a dnd operation is started on the source
         * widget.
         */
        final HandlerRegistration dndStartRegistration = widget.addHandler(
                new DragProxyAttachedEventHandler() {
                    @Override
                    public void onDragProxyAttached(DragProxyAttachedEvent event) {
                        state.onSourceMouseOut(DragSupportingPopupManager.this);
                    }
                }, DragProxyAttachedEvent.TYPE);

        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                superRegistration.removeHandler();
                dndStartRegistration.removeHandler();
            }
        };
    }
}