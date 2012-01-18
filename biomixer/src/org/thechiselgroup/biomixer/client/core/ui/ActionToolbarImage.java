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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class ActionToolbarImage extends Image {

    private Action action;

    private boolean mouseOver = false;

    private Label popupLabel;

    public ActionToolbarImage(Action action,
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
        addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                mouseOver = true;
                if (action.isEnabled()) {
                    setUrl(action.getHighlightedIconUrl());
                }
            }
        });
        addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                mouseOver = false;
                if (action.isEnabled()) {
                    setUrl(action.getNormalIconUrl());
                }
            }
        });
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
        if (action.isEnabled()) {
            if (mouseOver) {
                setUrl(action.getHighlightedIconUrl());
            } else {
                setUrl(action.getNormalIconUrl());
            }
        } else {
            setUrl(action.getDisabledIconUrl());
        }

        // TODO name in bold, break, description if available
        popupLabel.setText(action.getName());
    }

}