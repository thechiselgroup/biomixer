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

import com.google.gwt.user.client.ui.Button;

public abstract class AbstractDialog implements Dialog, DialogExtension {

    private DialogCallback callback;

    private DialogWindow window;

    /**
     * Default implementation handles OK and Cancel button presses. Clients may
     * override, but are responsible for closing the window if they do.
     */
    @Override
    public void buttonPressed(int code, Button button, DialogWindow window) {
        try {
            switch (code) {
            case DialogWindow.OK:
                okay();
                window.close();
                break;
            case DialogWindow.CANCEL:
                cancel();
                window.close();
                break;
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Default implementation creates an OK and a cancel button. Clients may
     * override to create their own custom buttons.
     */
    @Override
    public void createButtons(DialogWindow window) {
        window.createButton(DialogWindow.OK, getOkayButtonLabel());
        window.createButton(DialogWindow.CANCEL, DialogWindow.CANCEL_LABEL);
    }

    @Override
    public void dialogCreated(DialogWindow window) {
        this.window = window;
    }

    @Override
    public void handleException(Exception ex) {
        throw new RuntimeException(ex); // TODO better handling
    }

    @Override
    public void init(DialogCallback callback) {
        assert callback != null;

        this.callback = callback;

    }

    /**
     * Sets the enablement of the button with the given code.
     * 
     * @param code
     * @param enabled
     */
    public void setButtonEnablement(int code, boolean enabled) {
        window.setButtonEnabled(code, enabled);
    }

    protected void setOkayButtonEnabled(boolean enabled) {
        // TODO find better initialization order
        if (callback == null) {
            return;
        }

        callback.setOkayButtonEnabled(enabled);
    }

}
