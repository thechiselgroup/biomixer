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
package org.thechiselgroup.biomixer.client.dnd.popup;

import org.thechiselgroup.choosel.core.client.ui.popup.DefaultPopupManagerFactory;
import org.thechiselgroup.choosel.core.client.ui.popup.Popup;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupFactory;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupManager;

import com.google.inject.Inject;

/**
 * This factory should be used instead of {@link DefaultPopupManagerFactory} if
 * there are elements inside the popup that can be dragged out of it.
 * 
 * @author Lars Grammel
 */
public class DragSupportingPopupManagerFactory extends
        DefaultPopupManagerFactory {

    @Inject
    public DragSupportingPopupManagerFactory(PopupFactory popupFactory) {
        super(popupFactory);
    }

    @Override
    protected PopupManager doCreatePopupManager(Popup popup) {
        return new DragSupportingPopupManager(popup);
    }

}