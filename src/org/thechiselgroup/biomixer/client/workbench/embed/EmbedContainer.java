/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.workbench.embed;

import org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class EmbedContainer {

    private Label informationLabel;

    private final AbsolutePanel rootPanel;

    @Inject
    public EmbedContainer(
            @Named(ChooselInjectionConstants.ROOT_PANEL) AbsolutePanel rootPanel) {

        assert rootPanel != null;

        this.rootPanel = rootPanel;
    }

    public void init() {
        Window.enableScrolling(false);

        informationLabel = new Label();
        rootPanel.add(informationLabel);
    }

    public void setInfoText(String text) {
        // TODO make visible
        // XXX should be usable after setWidget got called
        informationLabel.setText(text);
    }

    public void setWidget(final Widget widget) {
        assert widget != null;

        // XXX allow for multiple calls

        rootPanel.remove(informationLabel);
        rootPanel.add(widget);

        updateWidgetSize(widget);
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                updateWidgetSize(widget);
            }
        });
    }

    private void updateWidgetSize(Widget widget) {
        widget.setPixelSize(Window.getClientWidth(), Window.getClientHeight());
    }
}