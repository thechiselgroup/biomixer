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
package org.thechiselgroup.biomixer.client.core.ui.popup;

import org.thechiselgroup.biomixer.client.core.fx.Opacity;

import com.google.gwt.event.shared.GwtEvent;

public class PopupOpacityChangedEvent extends
        GwtEvent<PopupOpacityChangedEventHandler> {

    public static final GwtEvent.Type<PopupOpacityChangedEventHandler> TYPE = new GwtEvent.Type<PopupOpacityChangedEventHandler>();

    private final Popup popup;

    private final int opacity;

    public PopupOpacityChangedEvent(Popup popup) {
        assert popup != null;

        this.opacity = popup.getOpacity();
        this.popup = popup;

        assert opacity >= Opacity.TRANSPARENT;
        assert opacity <= Opacity.OPAQUE;
    }

    @Override
    protected void dispatch(PopupOpacityChangedEventHandler handler) {
        handler.onOpacityChangeStarted(this);
    }

    @Override
    public GwtEvent.Type<PopupOpacityChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public int getOpacity() {
        return opacity;
    }

    public Popup getPopup() {
        return popup;
    }

}