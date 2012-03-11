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

    private Widget widget;

    @Inject
    public EmbedContainer(
            @Named(ChooselInjectionConstants.ROOT_PANEL) AbsolutePanel rootPanel) {

        assert rootPanel != null;

        this.rootPanel = rootPanel;
    }

    public void init() {
        Window.enableScrolling(false);
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                if (widget != null) {
                    updateWidgetSize(widget);
                }
            }
        });

        informationLabel = new Label();
    }

    public void setInfoText(String text) {
        informationLabel.setText(text);
        setWidget(informationLabel);
    }

    public void setWidget(final Widget widget) {
        assert widget != null;

        if (this.widget != null) {
            rootPanel.remove(this.widget);
        }

        this.widget = widget;

        rootPanel.add(widget);
        updateWidgetSize(widget);
    }

    private void updateWidgetSize(Widget widget) {
        widget.setPixelSize(Window.getClientWidth(), Window.getClientHeight());
    }
}