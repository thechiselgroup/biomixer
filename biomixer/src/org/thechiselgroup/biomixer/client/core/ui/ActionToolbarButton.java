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

import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManager;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManagerFactory;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

public class ActionToolbarButton extends Button {

    private Action action;

    private Label popupLabel;

    public ActionToolbarButton(Action action,
            PopupManagerFactory popupManagerFactory) {

        assert action != null;
        assert popupManagerFactory != null;

        this.action = action;

        initMouseHandlers();
        initActionChangeHandler();
        initPopup(popupManagerFactory);

        update();
    }

    private void initActionChangeHandler() {
        action.addActionChangedHandler(new ActionChangedEventHandler() {
            @Override
            public void onActionChanged(ActionChangedEvent event) {
                update();
            }
        });
    }

    private void initMouseHandlers() {
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                action.execute();
            }
        });
    }

    private void initPopup(PopupManagerFactory popupManagerFactory) {
        popupLabel = new Label();
        PopupManager popupManager = popupManagerFactory
                .createPopupManager(popupLabel);
        popupManager.linkToWidget(this);
    }

    // TODO do not show popup if disabled?
    protected void update() {
        setEnabled(action.isEnabled());
        setText(action.getName());
        popupLabel.setText(action.getName());
    }

}