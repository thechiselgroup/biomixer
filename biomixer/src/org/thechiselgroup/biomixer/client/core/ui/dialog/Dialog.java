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

import com.google.gwt.user.client.ui.Widget;

/**
 * Default dialog that displays a title, has a custom content, and an OK and
 * Cancel button. The OK and cancel buttons both close the window after being
 * pressed.
 * 
 * @author Lars Grammel
 */
public interface Dialog {

    static final int OK_WITH_CAP_EXIT_CODE = DialogWindow.OK;

    static final int CANCEL_WITHOUT_CAP_EXIT_CODE = DialogWindow.CANCEL;

    void cancel();

    Widget getContent();

    String getHeader();

    /**
     * Informs whether the header should be created for this dialog type. Some
     * dialogs are better off compressed. If false, no such header panel should
     * be created.
     * 
     * @return
     */
    public boolean useHeader();

    /**
     * Controls whether the window will have an 'X' displayed to close it
     * without using a button response.
     * 
     * @return
     */
    public boolean isCloseable();

    String getOkayButtonLabel();

    String getCancelButtonLabel();

    String getWindowTitle();

    public int getWidth();

    public int getHeight();

    void handleException(Exception ex);

    public void init(DialogCallback callback);

    public void setDialogExitCallback(DialogExitCallback exitCallback);

    void okay() throws Exception;

    /**
     * Gives information about how the dialog was exited. Is particularly useful
     * for synchronous modal dialogs.
     * 
     * A <code>Null</code> value is intended to indicate that the dialog has not
     * yet exited, or has exited with some serious error. Other integer codes
     * are defined by implementing classes, but should include codes
     * corresponding to 'Ok' and 'Cancel' responses from the user. Additional
     * codes could correspond to other options or input available. Anything that
     * cannot be easily represented with a single return value should use a
     * callback or other approach.
     * 
     * @return
     */
    Integer getExitCode();

    /**
     * @see Dialog#getExitCode()
     * 
     * @param exitCode
     */
    void setExitCode(int exitCode);
}
