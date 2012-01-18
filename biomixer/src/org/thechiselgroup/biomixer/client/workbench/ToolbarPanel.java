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
package org.thechiselgroup.biomixer.client.workbench;

import org.thechiselgroup.biomixer.client.core.ui.Action;
import org.thechiselgroup.biomixer.client.core.ui.ActionBarPanel;
import org.thechiselgroup.biomixer.client.core.ui.ActionToolbarButton;
import org.thechiselgroup.biomixer.client.core.ui.ActionToolbarImage;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupManagerFactory;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ToolbarPanel implements ActionBarPanel {

    private String name;

    private String id;

    private HorizontalPanel panel;

    private PopupManagerFactory popupManagerFactory;

    public ToolbarPanel(String id, String name,
            PopupManagerFactory popupManagerFactory) {

        assert name != null;
        assert id != null;
        assert popupManagerFactory != null;

        this.name = name;
        this.id = id;
        this.popupManagerFactory = popupManagerFactory;

        this.panel = new HorizontalPanel();
        panel.setSpacing(2);
    }

    public void addAction(Action action) {
        assert action != null;

        if (action.getNormalIconUrl() == null) {
            panel.add(new ActionToolbarButton(action, popupManagerFactory));
        } else {
            panel.add(new ActionToolbarImage(action, popupManagerFactory));
        }
    }

    @Override
    public Widget getContentWidget() {
        return panel;
    }

    @Override
    public String getPanelId() {
        return id;
    }

    @Override
    public String getTitle() {
        return name;
    }
}