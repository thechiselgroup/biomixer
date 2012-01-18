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

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;

public class DefaultPopupFactory implements PopupFactory {

    public final static int DEFAULT_POPUP_Z_INDEX = 1400;

    private Panel rootPanel;

    private int zIndex;

    @Inject
    public DefaultPopupFactory() {
        this(RootPanel.get(), DEFAULT_POPUP_Z_INDEX);
    }

    public DefaultPopupFactory(Panel rootPanel, int zIndex) {
        this.rootPanel = rootPanel;
        this.zIndex = zIndex;
    }

    @Override
    public Popup createPopup() {
        return new DefaultPopup(rootPanel, zIndex);
    }

}