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

import java.util.Iterator;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class DialogPanel extends Panel {

    private static final int BUTTON_BAR_HEIGHT = 36;

    private static final int HEADER_HEIGHT = 37;

    public static final String CSS_DIALOG_PANEL = "choosel-DialogPanel";

    public static final String CSS_DIALOG_PANEL_BUTTONBAR = "choosel-DialogPanel-ButtonBar";

    public static final String CSS_DIALOG_PANEL_HEADER = "choosel-DialogPanel-Header";

    public static final String CSS_DIALOG_PANEL_CONTENT = "choosel-DialogPanel-Content";

    private Label headerLabel;

    private Element panelElement;

    private FlowPanel buttonBar;

    private Widget contentWidget;

    private Element contentTD;

    private boolean useHeader = true;

    public DialogPanel(boolean useHeader) {
        panelElement = DOM.createTable();
        panelElement.setClassName(CSS_DIALOG_PANEL);

        this.useHeader = useHeader;

        initHeaderLabel();
        contentTD = createRow();
        initButtonBar();

        setElement(panelElement);
    }

    public Button createButton(String label) {
        Button button = new Button(label);
        buttonBar.add(button);
        return button;
    }

    protected Element createRow() {
        Element tr = DOM.createTR();
        panelElement.appendChild(tr);
        Element td = DOM.createTD();
        tr.appendChild(td);
        return td;
    }

    // TODO CSS
    public void initButtonBar() {
        buttonBar = new FlowPanel();
        buttonBar.setStyleName(CSS_DIALOG_PANEL_BUTTONBAR);
        Element td = createRow();
        CSS.setHeight(td, BUTTON_BAR_HEIGHT);
        td.appendChild(buttonBar.getElement());
        adopt(buttonBar);
    }

    // TODO set CSS style
    public void initHeaderLabel() {
        headerLabel = new Label();
        headerLabel.setStyleName(CSS_DIALOG_PANEL_HEADER);
        if (this.useHeader) {
            Element td = createRow();
            CSS.setHeight(td, HEADER_HEIGHT);
            td.appendChild(headerLabel.getElement());
            adopt(headerLabel);
        }
    }

    @Override
    public Iterator<Widget> iterator() {
        return new Iterator<Widget>() {

            private int index = 0;

            private Widget getWidget(int index) {
                int count = 0;

                if (headerLabel != null) {
                    if (index == count) {
                        return headerLabel;
                    }
                    count++;
                }

                if (contentWidget != null) {
                    if (index == count) {
                        return contentWidget;
                    }
                    count++;
                }

                if (buttonBar != null) {
                    if (index == count) {
                        return buttonBar;
                    }
                    count++;
                }

                return null;
            }

            @Override
            public boolean hasNext() {
                return getWidget(index) != null;
            }

            @Override
            public Widget next() {
                Widget w = getWidget(index);
                index++;
                return w;
            }

            @Override
            public void remove() {
                int count = 0;

                if (headerLabel != null) {
                    if (index - 1 == count) {
                        removeHeaderLabel();
                    }
                    count++;
                }

                if (contentWidget != null) {
                    if (index - 1 == count) {
                        removeContentWidget();
                    }
                    count++;
                }

                if (buttonBar != null) {
                    if (index - 1 == count) {
                        removeButtonBar();
                    }
                    count++;
                }

                index--;
            }

        };
    }

    @Override
    public boolean remove(Widget child) {
        if (child == contentWidget) {
            removeContentWidget();
            return true;
        }

        if (child == buttonBar) {
            removeButtonBar();
            return true;
        }

        if (child == headerLabel) {
            removeHeaderLabel();
            return true;
        }

        return false;
    }

    private void removeButtonBar() {
        try {
            orphan(buttonBar);
        } finally {
            buttonBar.getElement().removeFromParent();
            buttonBar = null;
        }
    }

    private void removeContentWidget() {
        try {
            orphan(contentWidget);
        } finally {
            contentWidget.getElement().removeFromParent();
            contentWidget = null;
        }
    }

    private void removeHeaderLabel() {
        try {
            orphan(headerLabel);
        } finally {
            headerLabel.getElement().removeFromParent();
            headerLabel = null;
        }
    }

    // TODO code from simple panel
    public void setContent(Widget contentWidget) {
        assert contentWidget != null;
        this.contentWidget = contentWidget;
        contentWidget.addStyleName(CSS_DIALOG_PANEL_CONTENT);
        contentTD.appendChild(contentWidget.getElement());
        adopt(contentWidget);
    }

    public void setHeader(String header) {
        headerLabel.setText(header);
    }

    @Override
    public void setPixelSize(int width, int height) {
        CSS.clearHeight(contentWidget);

        super.setPixelSize(width, height);

        int contentHeight = getOffsetHeight() - headerLabel.getOffsetHeight()
                - buttonBar.getOffsetHeight();

        if (contentHeight < 0) {
            contentHeight = 0;
        }

        CSS.setHeight(contentWidget, contentHeight);
    }

}
