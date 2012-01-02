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
package org.thechiselgroup.biomixer.client.workbench.ui.dialog;

import org.thechiselgroup.biomixer.client.dnd.windows.AbstractWindowManager;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowPanel;
import org.thechiselgroup.choosel.core.client.command.NullCommandManager;
import org.thechiselgroup.choosel.core.client.ui.ActionBar;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupManagerFactory;
import org.thechiselgroup.choosel.core.client.ui.shade.ShadeManager;
import org.thechiselgroup.choosel.core.client.util.RemoveHandle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class DialogWindowManager extends AbstractWindowManager {

    private Dialog dialog;

    private RemoveHandle shadeHandle;

    private ShadeManager shadeManager;

    private PopupManagerFactory popupManagerFactory;

    private boolean modal;

    DialogWindowManager(AbsolutePanel boundaryPanel, Dialog dialog,
            ShadeManager shadeManager, PopupManagerFactory popupManagerFactory,
            boolean modal) {

        super(boundaryPanel, new NullCommandManager());
        this.dialog = dialog;
        this.shadeManager = shadeManager;
        this.popupManagerFactory = popupManagerFactory;
        this.modal = modal;
    }

    protected void cancelDialog(DialogWindow window) {
        dialog.cancel();
        window.close();
    }

    @Override
    public void close(WindowPanel window) {
        assert window instanceof DialogWindow;

        getBoundaryPanel().remove(window);
        hideShade();
    }

    private void hideShade() {
        shadeHandle.remove();
    }

    public void init() {
        /*
         * IMPLEMENTATION NOTE: The shade needs to be removed if an exception
         * occurs, otherwise the user might get locked in a state where he/she
         * cannot remove the shade.
         */
        try {
            showShade();

            final DialogWindow dialogWindow = new DialogWindow(
                    popupManagerFactory);

            // initialization order important (breaks otherwise)
            dialogWindow.init(this, dialog);

            if (!modal) {
                shadeManager.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        cancelDialog(dialogWindow);
                    }
                });
            }

            getBoundaryPanel().add(dialogWindow);

            // TODO variable calculation of window size
            // XXX this fixes problem that dialog window takes up whole screen
            dialogWindow.setPixelSize(500, 600);

            /*
             * display centered below action bar offsets are useless here --
             * why? -- use content instead (not exact)
             */
            int x = (getBoundaryPanel().getOffsetWidth() - dialogWindow
                    .getWidth()) / 2;

            // TODO extract offset (variable)
            dialogWindow.setLocation(x, ActionBar.ACTION_BAR_HEIGHT_PX + 10);
        } catch (RuntimeException e) {
            hideShade();
            throw e;
        } catch (Error e) {
            hideShade();
            throw e;
        }
    }

    private void showShade() {
        shadeHandle = shadeManager.showShade();
    }
}