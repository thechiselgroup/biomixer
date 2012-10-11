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
package org.thechiselgroup.biomixer.client.core.ui.dialog;

import org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManagerFactory;
import org.thechiselgroup.biomixer.client.core.ui.shade.ShadeManager;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/*
 * IMPLEMENTATION NOTE: we need to wait until after the fade out operation
 * before calling dialog.okay/canceled as these might block UI updates - but we
 * loose 0.5 seconds on that. 
 * 
 * TODO can we somehow parallelize these things so the UI gets updated & the 
 * server gets called?
 */
public class DialogManager {

    private AbsolutePanel parentPanel;

    private ShadeManager shadeManager;

    private final PopupManagerFactory popupManagerFactory;

    @Inject
    public DialogManager(
            @Named(ChooselInjectionConstants.ROOT_PANEL) AbsolutePanel parentPanel,
            ShadeManager shadeManager, PopupManagerFactory popupManagerFactory) {
        this.parentPanel = parentPanel;
        this.shadeManager = shadeManager;
        this.popupManagerFactory = popupManagerFactory;
    }

    /**
     * Opens up a non-modal dialog. This method is a shorthand notation for
     * {@code show(dialog, false)}.
     * 
     * @param dialog
     *            the dialog to open.
     * 
     * @see #show(Dialog, boolean)
     */
    public void show(Dialog dialog) {
        show(dialog, false);
    }

    /**
     * Opens up a dialog in the given "modal" state. If "modal" is false, then
     * clicking on the background will "cancel" and close the dialog. If it is
     * true, then clicking on the background (outside of the dialog) will not
     * cancel or close the dialog.
     * 
     * @param dialog
     *            the dialog to open.
     * @param modal
     *            the modal state.
     */
    public void show(Dialog dialog, boolean modal) {
        assert dialog != null;

        new DialogWindowManager(parentPanel, dialog, shadeManager,
                popupManagerFactory, modal).init();
    }
}