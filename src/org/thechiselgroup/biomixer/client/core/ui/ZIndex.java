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
package org.thechiselgroup.biomixer.client.core.ui;

import org.thechiselgroup.biomixer.client.core.ui.popup.DefaultPopupFactory;

public final class ZIndex {

    public static final int DESKTOP_WINDOW_BASE = 1200;

    public static final int DIALOG = 1300;

    public static final int DRAG_AVATAR = 1500;

    public static final int POPUP = DefaultPopupFactory.DEFAULT_POPUP_Z_INDEX;

    public static final int SHADE = 1275;

    private ZIndex() {

    }

}