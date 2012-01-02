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
package org.thechiselgroup.biomixer.client.workbench.ui.messages;

import org.thechiselgroup.choosel.core.client.ui.ActionBar;
import org.thechiselgroup.choosel.core.client.ui.CSS;
import org.thechiselgroup.choosel.core.client.ui.ZIndex;
import org.thechiselgroup.choosel.core.client.util.RemoveHandle;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;

public class DefaultMessageManager implements MessageManager {

    private static final String CSS_DIALOG_MESSAGE = "dialog-message";

    private Element messageElement;

    private AbsolutePanel parentPanel;

    @Inject
    public DefaultMessageManager() {
        parentPanel = RootPanel.get(); // TODO inject
    }

    public void hideMessage() {
        assert messageElement != null;

        parentPanel.getElement().removeChild(messageElement);
        messageElement = null;
    }

    // TODO support multiple messages
    @Override
    public RemoveHandle showMessage(final String message) {
        assert messageElement == null;

        if (message != null) {
            // add message box on dialog layer --> div
            messageElement = DOM.createDiv();

            messageElement.setInnerText(message);
            messageElement.addClassName(CSS_DIALOG_MESSAGE);
            CSS.setZIndex(messageElement, ZIndex.DIALOG);

            // TODO refactor, see below, code duplication
            int x = (parentPanel.getOffsetWidth() - 400) / 2;

            // TODO extract offset (variable)
            DOM.setStyleAttribute(messageElement, CSS.LEFT, x + "px");
            DOM.setStyleAttribute(messageElement, CSS.TOP,
                    (ActionBar.ACTION_BAR_HEIGHT_PX + 10) + "px");

            parentPanel.getElement().appendChild(messageElement);
        }

        return new RemoveHandle() {
            @Override
            public void remove() {
                if (message != null) {
                    hideMessage();
                }
            }
        };
    }

}
