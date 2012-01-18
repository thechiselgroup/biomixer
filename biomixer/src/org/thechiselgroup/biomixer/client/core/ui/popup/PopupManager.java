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

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

public interface PopupManager {

    int getHideDelay();

    Popup getPopup();

    int getShowDelay();

    void hidePopup();

    boolean isEnabled();

    <T extends Widget & HasAllMouseHandlers & HasAttachHandlers> HandlerRegistration linkToWidget(
            T widget);

    /**
     * Mouse down triggers either drag-and-drop operations (left mouse button,
     * popup gets hidden) or context menu operations (right mouse buttons, popup
     * gets shown)
     */
    void onMouseDown(NativeEvent event);

    void onMouseMove(int clientX, int clientY);

    void onMouseOut(int clientX, int clientY);

    void onMouseOver(int clientX, int clientY);

    void setEnabled(boolean enabled);

    void setHideDelay(int delay);

    void setShowDelay(int showDelay);

}