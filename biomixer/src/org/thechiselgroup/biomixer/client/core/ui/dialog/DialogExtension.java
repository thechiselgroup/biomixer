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

/**
 * An extension to the Dialog interface that supports the creation of custom
 * buttons, and the ability to interact with the dialog's parent window.
 * 
 * @author Del
 */
// TODO Merge this into dialog (and break the API :-)
public interface DialogExtension {

    /**
     * Indicates that the button with the given code was pressed within the
     * given window. Clients are responsible for performing all functionality
     * after a button press, including closing the window if necessary.
     * 
     * @param code
     *            the status code of the button.
     * @param button
     *            the button pressed.
     * @param window
     *            the dialog window for the dialog.
     */
    public void buttonPressed(int code, Button button, DialogWindow window);

    /**
     * Extension to create custom buttons in the dialog window. Clients should
     * call {@link DialogWindow#createButton(int, String)} to create the button.
     * Clients will be automatically notified of button presses through the
     * {@link #buttonPressed(int, Button, DialogWindow)} callback.
     * 
     * 
     * @param window
     *            the window to create the button on.
     */
    public void createButtons(DialogWindow window);

    /**
     * Indicates that the dialog has been created on the given parent. Occurs
     * before the dialog contents have been created.
     * 
     * @param window
     *            the window that the dialog is created on.
     */
    public void dialogCreated(DialogWindow window);

}
