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

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ActionBar implements IsWidget {

    // TODO remove constant, replace with dynamic size calculation
    public static final int ACTION_BAR_HEIGHT_PX = 91;

    public static final String CSS_ACTIONBAR = "ActionBar";

    public static final String CSS_ACTIONBAR_PANEL = "ActionBar-panel";

    public static final String CSS_ACTIONBAR_PANEL_CONTENT = "ActionBar-panel-content";

    public static final String CSS_ACTIONBAR_PANEL_EXPANDER = "ActionBar-panel-expander";

    public static final String CSS_ACTIONBAR_PANEL_HEADER = "ActionBar-panel-header";

    public static final String CSS_ACTIONBAR_PANELCONTAINER = "ActionBar-panelcontainer";

    public static final String CSS_ACTIONBAR_TITLE_AREA = "ActionBar-titleArea";

    private HorizontalPanel actionBarPanelContainer;

    private HorizontalPanel actionBarTitleArea;

    private VerticalPanel outerWidget;

    private Map<String, ActionBarPanel> panelsByID = CollectionFactory
            .createStringMap();

    public ActionBar() {
        outerWidget = new VerticalPanel();
        outerWidget.addStyleName(CSS_ACTIONBAR);
        outerWidget.setSpacing(0);

        actionBarTitleArea = new HorizontalPanel();
        actionBarTitleArea.addStyleName(CSS_ACTIONBAR_TITLE_AREA);
        outerWidget.add(actionBarTitleArea);

        actionBarPanelContainer = new HorizontalPanel();
        actionBarPanelContainer.addStyleName(CSS_ACTIONBAR_PANELCONTAINER);
        actionBarPanelContainer.setSpacing(0);
        outerWidget.add(actionBarPanelContainer);
    }

    public void addPanel(ActionBarPanel panel) {
        assert panel != null;
        assert !containsPanel(panel.getPanelId()) : "panel "
                + panel.getPanelId()
                + " was already registered in the ActionBar";

        FlexTable actionBarPanel = new FlexTable();
        actionBarPanel.addStyleName(CSS_ACTIONBAR_PANEL);

        Label header = new Label(panel.getTitle());
        header.addStyleName(CSS_ACTIONBAR_PANEL_HEADER);
        actionBarPanel.setWidget(0, 0, header);
        actionBarPanel.getFlexCellFormatter().setColSpan(0, 0, 2);

        SimplePanel contentPanel = new SimplePanel();
        contentPanel.add(panel.getContentWidget());
        contentPanel.addStyleName(CSS_ACTIONBAR_PANEL_CONTENT);
        actionBarPanel.setWidget(1, 0, contentPanel);

        // TODO reactivate expanders once drop down menus are available
        // actionBarPanel.setWidget(1, 1, new Image(GWT.getModuleBaseURL()
        // + "images/expander-normal.png"));
        // actionBarPanel.getCellFormatter().addStyleName(1, 1,
        // CSS_ACTIONBAR_PANEL_EXPANDER);

        actionBarPanelContainer.add(actionBarPanel);
        panelsByID.put(panel.getPanelId(), panel);
    }

    @Override
    public Widget asWidget() {
        return outerWidget;
    }

    public boolean containsPanel(String panelId) {
        return panelsByID.containsKey(panelId);
    }

    public HorizontalPanel getActionBarTitleArea() {
        return actionBarTitleArea;
    }

    public ActionBarPanel getPanel(String panelId) {
        assert containsPanel(panelId);
        return panelsByID.get(panelId);
    }

}
