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
package org.thechiselgroup.biomixer.client.core.resources.ui.popup;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.ui.WidgetFactory;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ResourceSetAvatarPopupWidgetFactory implements WidgetFactory {

    public static interface HeaderUpdatedEventHandler {

        void headerLabelChanged(String newLabel);

    }

    public static interface ResourceSetAvatarPopupWidgetFactoryAction {

        void execute();

        String getLabel();

    }

    private static final String CSS_POPUP_ACTION = "popup-content-action";

    private static final String CSS_POPUP_CONTENT_HEADER = "popup-content-header";

    private static final String CSS_POPUP_CONTENT_INFO = "popup-content-info";

    private static final String CSS_POPUP_CONTENT_SUBHEADER = "popup-content-subheader";

    private List<ResourceSetAvatarPopupWidgetFactoryAction> actions;

    private String headerText;

    private final HeaderUpdatedEventHandler headerUpdatedHandler;

    private String infoMessage;

    private final String subHeaderText;

    /**
     * @param headerUpdatedHandler
     *            Can be <code>null</code>. If not <code>null</code>, header is
     *            a text field and this handler is called when the content
     *            changes.
     */
    public ResourceSetAvatarPopupWidgetFactory(String headerText,
            String subHeaderText,
            List<ResourceSetAvatarPopupWidgetFactoryAction> actions,
            String infoMessage, HeaderUpdatedEventHandler headerUpdatedHandler) {

        assert headerText != null;
        assert subHeaderText != null;
        assert actions != null;
        assert infoMessage != null;

        this.subHeaderText = subHeaderText;
        this.headerText = headerText;
        this.actions = actions;
        this.infoMessage = infoMessage;
        this.headerUpdatedHandler = headerUpdatedHandler;
    }

    private void addActionsPanel(VerticalPanel panel) {
        if (!actions.isEmpty()) {
            VerticalPanel actionPanel = new VerticalPanel();
            actionPanel.addStyleName(CSS_POPUP_ACTION);

            for (ResourceSetAvatarPopupWidgetFactoryAction action : actions) {
                actionPanel.add(createActionButton(action));
            }

            panel.add(actionPanel);
        }
    }

    private void addHeader(VerticalPanel panel) {
        if (headerUpdatedHandler == null) {
            Label header = new Label(headerText);
            header.addStyleName(CSS_POPUP_CONTENT_HEADER);
            panel.add(header);
        } else {
            final TextBox header = new TextBox();
            header.setText(headerText);
            header.setMaxLength(20); // TODO change to resizable text box
            header.addStyleName(CSS_POPUP_CONTENT_HEADER);
            header.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    headerUpdatedHandler.headerLabelChanged(header.getText());
                }
            });
            header.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent event) {
                    headerUpdatedHandler.headerLabelChanged(header.getText());
                }
            });
            panel.add(header);
        }

        Label subheader = new Label(subHeaderText);
        subheader.addStyleName(CSS_POPUP_CONTENT_SUBHEADER);
        panel.add(subheader);
    }

    private void addInfoText(VerticalPanel panel) {
        HTML infotext = new HTML(infoMessage);
        infotext.addStyleName(CSS_POPUP_CONTENT_INFO);
        panel.add(infotext);
    }

    private Button createActionButton(
            final ResourceSetAvatarPopupWidgetFactoryAction action) {
        Button actionButton = new Button(action.getLabel());
        actionButton.addStyleName(CSS_POPUP_ACTION);
        actionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                action.execute();
            }
        });
        return actionButton;
    }

    // TODO extract into default widget class
    @Override
    public Widget createWidget() {
        VerticalPanel panel = new VerticalPanel();

        addHeader(panel);
        addActionsPanel(panel);
        addInfoText(panel);

        return panel;
    }
}