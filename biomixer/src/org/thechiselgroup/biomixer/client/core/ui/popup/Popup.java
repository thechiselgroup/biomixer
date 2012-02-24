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
package org.thechiselgroup.biomixer.client.core.ui.popup;

import org.thechiselgroup.biomixer.client.core.fx.Opacity;
import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeInt;
import org.thechiselgroup.biomixer.client.core.geometry.Point;

import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>
 * Fixed position container that supports opacity change effects on its content.
 * </p>
 * 
 * @see PopupOpacityChangedEventHandler
 * 
 * @author Lars Grammel
 */
public interface Popup {

    String DEFAULT_POPUP_CSS_CLASS = "popups-Popup";

    double DEFAULT_EFFECT_DURATION = 0.5;

    // TODO extract into interface
    <H extends EventHandler> HandlerRegistration addDomHandler(final H handler,
            DomEvent.Type<H> type);

    // TODO extract into interface
    <H extends EventHandler> HandlerRegistration addHandler(final H handler,
            GwtEvent.Type<H> type);

    Widget getContentWidget();

    Point getLocation();

    int getOpacity();

    /**
     * @return Size in pixels.
     */
    DefaultSizeInt getSize();

    void hide();

    void hide(double durationInSeconds);

    void setContentWidget(Widget contentWidget);

    void setLocation(Point location);

    /**
     * Changes the opacity in {@link #DEFAULT_EFFECT_DURATION} seconds.
     * 
     * @param opacity
     *            {@link Opacity} between 0 (transparent) and 100 (opaque).
     * 
     * @see #setOpacity(int, double)
     */
    void setOpacity(int opacity);

    /**
     * <p>
     * Changes the opacity.
     * </p>
     * <p>
     * Effect scheduling: The container supports scheduling one future event
     * (opacity transition) while a event transition is in progress. If an
     * effect is currently running and a future effect is already scheduled,
     * that transition will be overridden by the new opacity effect.
     * </p>
     * <p>
     * After the transition to the new opacity is finished, a
     * {@link PopupOpacityChangedEvent} is fired.
     * </p>
     * 
     * @param opacity
     *            {@link Opacity} between 0 (transparent) and 100 (opaque).
     * @param durationInSeconds
     *            Effect duration in seconds. Has to be >= 0. If effect duration
     *            is 0, the effect is carried out immediately.
     */
    void setOpacity(int opacity, double durationInSeconds);

}