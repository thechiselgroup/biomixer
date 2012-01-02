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
package org.thechiselgroup.biomixer.client.workbench.workspace.command;

import java.util.HashMap;
import java.util.List;

import org.thechiselgroup.biomixer.client.workbench.ui.dialog.AbstractDialog;
import org.thechiselgroup.biomixer.client.workbench.ui.dialog.DialogCallback;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewLoader;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewPreview;
import org.thechiselgroup.choosel.core.client.command.AsyncCommandExecutor;
import org.thechiselgroup.choosel.core.client.visualization.View;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ConfigureSharedViewsDialog extends AbstractDialog {

    private final AsyncCommandExecutor asyncCommandExecutor;

    private RadioButton selectedButton;

    private List<ViewPreview> viewPreviews;

    private final int RADIO_COLUMN = 0;

    private final int TYPE_COLUMN = 1;

    private final int TITLE_COLUMN = 2;

    private final int DATE_COLUMN = 3;

    private Label urlLabel;

    private Label embedLabel;

    private Label viewLoading;

    private TextBox viewUrl;

    private TextArea viewEmbed;

    private ViewLoader loader;

    private VerticalPanel content;

    private VerticalPanel viewDisplay;

    private FlexTable selectContent;

    private final int EMBED_HEIGHT = 400;

    private final int EMBED_WIDTH = 480;

    private final String EMBED_POSTTEXT = "Created with <a href=\"http://choosel-mashups.appspot.com\">Choosel</a>";

    private final String LOADING_TEXT = "Please wait while we load the preview for the selected view...";

    private HashMap<RadioButton, ViewPreview> buttonsToViews = new HashMap<RadioButton, ViewPreview>();

    public ConfigureSharedViewsDialog(List<ViewPreview> views,
            AsyncCommandExecutor asyncCommandExecutor, ViewLoader loader) {

        assert views != null;
        assert asyncCommandExecutor != null;
        assert loader != null;

        viewPreviews = views;
        this.asyncCommandExecutor = asyncCommandExecutor;
        this.loader = loader;
    }

    @Override
    public void cancel() {
    }

    @Override
    public Widget getContent() {
        initPanels();
        initSelectPanel();
        return content;
    }

    @Override
    public String getHeader() {
        return "Configure existing views";
    }

    @Override
    public String getOkayButtonLabel() {
        return "Delete";
    }

    @Override
    public String getWindowTitle() {
        return "Configure Shared Views";
    }

    @Override
    public void init(DialogCallback callback) {
        super.init(callback);
        setOkayButtonEnabled(selectedButton != null);
    }

    private void initPanels() {
        content = new VerticalPanel();
        content.setWidth("100%");

        selectContent = new FlexTable();
        VerticalPanel detailsContent = new VerticalPanel();
        detailsContent.setWidth("100%");
        content.add(selectContent);
        content.add(detailsContent);

        FlexTable urlPanel = new FlexTable();
        VerticalPanel displayPanel = new VerticalPanel();
        detailsContent.add(displayPanel);
        detailsContent.add(urlPanel);

        viewDisplay = new VerticalPanel();
        viewLoading = new Label(LOADING_TEXT);
        displayPanel.add(viewLoading);
        displayPanel.add(viewDisplay);

        urlLabel = new Label("URL:");
        urlPanel.setWidget(0, 0, urlLabel);

        viewUrl = new TextBox();
        urlPanel.setWidget(1, 0, viewUrl);

        embedLabel = new Label("Embed Code:");
        urlPanel.setWidget(0, 1, embedLabel);

        viewEmbed = new TextArea();
        urlPanel.setWidget(1, 1, viewEmbed);

        showLoading();
        viewLoading.setVisible(false);
    }

    private void initSelectPanel() {
        // TODO extract radio group widget
        // TODO autogenerate group id
        String groupId = "group";

        selectContent.setWidth("100%");
        selectContent.setBorderWidth(0);
        selectContent.setCellSpacing(5);
        selectContent.setWidget(0, TYPE_COLUMN, new Label("Type"));
        selectContent.setWidget(0, TITLE_COLUMN, new Label("Title"));
        selectContent.setWidget(0, DATE_COLUMN, new Label("Date"));

        for (final ViewPreview view : viewPreviews) {
            int viewRow = selectContent.getRowCount();

            Label viewType = new Label(view.getType());
            Label viewTitle = new Label(view.getName());
            Label viewDate = new Label(view.getDate().toLocaleString());

            final RadioButton rButton = new RadioButton(groupId);
            buttonsToViews.put(rButton, view);
            rButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    showDialog(view.getId());
                    setSelectedButton(rButton);
                }

            });

            Button openButton = new Button("Open View");
            openButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    LoadViewAsWindowCommand loadWorkspaceCommand = new LoadViewAsWindowCommand(
                            view.getId(), loader);
                    asyncCommandExecutor.execute(loadWorkspaceCommand);
                }

            });

            selectContent.setWidget(viewRow, RADIO_COLUMN, rButton);
            selectContent.setWidget(viewRow, TYPE_COLUMN, viewType);
            selectContent.setWidget(viewRow, TITLE_COLUMN, viewTitle);
            selectContent.setWidget(viewRow, DATE_COLUMN, viewDate);

        }
    }

    @Override
    public void okay() {
        ViewPreview viewPreview = buttonsToViews.get(selectedButton);
        DeleteViewCommand deleteViewCommand = new DeleteViewCommand(
                viewPreview.getId(), loader);

        asyncCommandExecutor.execute(deleteViewCommand);
    }

    private void setSelectedButton(RadioButton radioButton) {
        assert radioButton != null;
        selectedButton = radioButton;
        setOkayButtonEnabled(true);
    }

    private void showContent() {
        viewDisplay.setVisible(true);
        urlLabel.setVisible(true);
        viewUrl.setVisible(true);
        embedLabel.setVisible(true);
        viewEmbed.setVisible(true);
        viewLoading.setVisible(false);
    }

    private void showDialog(final Long id) {
        viewLoading.setVisible(false);
        viewLoading.setText(LOADING_TEXT);
        showLoading();

        loader.loadView(id, new AsyncCallback<View>() {

            @Override
            public void onFailure(Throwable caught) {
                viewLoading
                        .setText("Sorry, the specified view is not available.");
            }

            @Override
            public void onSuccess(final View view) {
                viewDisplay.clear();
                view.asWidget().setPixelSize(300, 200);
                viewDisplay.add(view.asWidget());

                String url = "http://" + Window.Location.getHost()
                        + Window.Location.getPath() + "?viewId="
                        + id.toString();
                String gwtHost = Window.Location.getParameter("gwt.codesvr");
                if (gwtHost != null) {
                    url += "&gwt.codesvr=" + gwtHost;
                }

                String embed = "<iframe src=\""
                        + url
                        + "\" width=\""
                        + EMBED_WIDTH
                        + "\" height=\""
                        + EMBED_HEIGHT
                        + "\">Sorry, your browser doesn't support iFrames</iframe><br /><a href=\""
                        + url + "&nw\">Open in Choosel</a>. " + EMBED_POSTTEXT;

                viewUrl.setText(url);
                viewEmbed.setText(embed);

                showContent();
            }

        });

    }

    private void showLoading() {
        viewDisplay.setVisible(false);
        urlLabel.setVisible(false);
        viewUrl.setVisible(false);
        embedLabel.setVisible(false);
        viewEmbed.setVisible(false);
        viewLoading.setVisible(true);
    }
}