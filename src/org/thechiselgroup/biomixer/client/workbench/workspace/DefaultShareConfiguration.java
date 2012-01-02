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
package org.thechiselgroup.biomixer.client.workbench.workspace;

import org.thechiselgroup.biomixer.client.workbench.authentication.AuthenticationManager;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedInitializer;
import org.thechiselgroup.biomixer.client.workbench.embed.StoredViewEmbedLoader;
import org.thechiselgroup.biomixer.client.workbench.init.ChooselApplicationInitializer;
import org.thechiselgroup.biomixer.client.workbench.init.WorkbenchInitializer;
import org.thechiselgroup.choosel.core.client.ui.SidePanelSection;
import org.thechiselgroup.choosel.core.client.util.url.UrlBuilder;
import org.thechiselgroup.choosel.core.client.visualization.View;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DefaultShareConfiguration implements ShareConfiguration {

    private static final String GWT_CODESVR = "gwt.codesvr";

    private static final String HTTP = "http";

    private VerticalPanel sharePanel;

    private View view;

    private final ViewSaver viewPersistence;

    private Button button;

    private Label label;

    private TextBox textBox;

    // TODO extract into branding - should be injectable
    private final String EMBED_POSTTEXT = "Created with <a href=\"http://choosel-mashups.appspot.com\">Choosel</a>";

    private final int EMBED_HEIGHT = 400;

    private final int EMBED_WIDTH = 480;

    private TextArea textArea;

    private Label embedLabel;

    private final AuthenticationManager authenticationManager;

    public DefaultShareConfiguration(ViewSaver viewPersistence,
            AuthenticationManager authenticationManager) {

        this.viewPersistence = viewPersistence;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Widget asWidget() {
        if (sharePanel == null) {
            init();
        }
        return sharePanel;
    }

    protected String createUrl(Long id, String applicationMode) {
        // TODO create url builder factory that creates same host urls
        UrlBuilder urlBuilder = new UrlBuilder();

        urlBuilder.setProtocol(HTTP);
        urlBuilder.setHost(Window.Location.getHost());
        urlBuilder.setPath(Window.Location.getPath());
        urlBuilder.setParameter(WorkbenchInitializer.VIEW_ID, id.toString());
        urlBuilder.setParameter(
                ChooselApplicationInitializer.APPLICATION_MODE_PARAMETER,
                applicationMode);

        if (ChooselApplicationInitializer.EMBED.equals(applicationMode)) {
            urlBuilder.setParameter(EmbedInitializer.EMBED_MODE_PARAMETER,
                    StoredViewEmbedLoader.EMBED_MODE);
        }

        String gwtHost = Window.Location.getParameter(GWT_CODESVR);
        if (gwtHost != null) {
            urlBuilder.setParameter(GWT_CODESVR, gwtHost);
        }

        return urlBuilder.buildString();
    }

    @Override
    public SidePanelSection[] getSidePanelSections() {
        if (!authenticationManager.isAuthenticated()) {
            return new SidePanelSection[0];
        }

        return new SidePanelSection[] { new SidePanelSection("Share",
                asWidget()) };
    }

    public View getView() {
        return view;
    }

    private void init() {
        sharePanel = new VerticalPanel();

        initShareControls();
    }

    private void initShareControls() {
        button = new Button("Share this");
        label = new Label("Generating Share Information...");
        label.setVisible(false);
        textBox = new TextBox();
        textBox.setVisible(false);
        embedLabel = new Label();
        embedLabel.setVisible(false);
        textArea = new TextArea();
        textArea.setVisible(false);

        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                label.setVisible(false);
                textBox.setVisible(false);
                embedLabel.setVisible(false);
                textArea.setVisible(false);

                label.setText("Generating Share Information...");
                label.setVisible(true);

                viewPersistence.saveView(DefaultShareConfiguration.this);
            }
        });

        sharePanel.add(button);
        sharePanel.add(label);
        sharePanel.add(textBox);
        sharePanel.add(embedLabel);
        sharePanel.add(textArea);
    }

    public void notLoggedIn() {
        label.setText("Sorry, you are not currently authenticated.  Please log in to share views.");
        label.setVisible(true);
        textBox.setVisible(false);
        embedLabel.setVisible(false);
        textArea.setVisible(false);
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void updateSharePanel(Long id) {
        String url = createUrl(id, ChooselApplicationInitializer.EMBED);

        String embed = "<iframe src=\""
                + url
                + "\" width=\""
                + EMBED_WIDTH
                + "\" height=\""
                + EMBED_HEIGHT
                + "\">Sorry, your browser doesn't support iFrames</iframe><br /><a href=\""
                + createUrl(id, ChooselApplicationInitializer.WORKBENCH)
                + "\">Open in Choosel</a>. " + EMBED_POSTTEXT;

        // Hide things while we change them
        label.setVisible(false);
        button.setVisible(false);

        label.setText("Share Link:");
        textBox.setText(url);
        embedLabel.setText("Embed Source:");
        textArea.setText(embed);

        label.setVisible(true);
        textBox.setVisible(true);
        embedLabel.setVisible(true);
        textArea.setVisible(true);
    }

}