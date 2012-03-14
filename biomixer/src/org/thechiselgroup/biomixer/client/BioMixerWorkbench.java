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
package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.core.ui.TextCommandPresenter;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.text.TextVisualization;
import org.thechiselgroup.biomixer.client.visualization_component.timeline.TimeLine;
import org.thechiselgroup.biomixer.client.workbench.init.WorkbenchInitializer;

import com.google.inject.Inject;

public class BioMixerWorkbench extends WorkbenchInitializer {

    public static final String NCBO_SEARCH = "ncbo-search";

    @Inject
    private ConceptSearchCommand ncboConceptSearchCommand;

    @Override
    protected void initActionBarContent() {
        initWorkspacePanel();
        initHelpPanel();
        initDeveloperModePanel();

        initCustomActions();
    }

    @Override
    protected void initActionBarPanels() {
        addToolbarPanel(WORKSPACE_PANEL, "Workspace");
        initCustomPanels();
        addToolbarPanel(VIEWS_PANEL, "Views");
        addToolbarPanel(HELP_PANEL, "Help");

        if (runsInDevelopmentMode()) {
            addToolbarPanel(DEVELOPER_MODE_PANEL, "Dev Mode");
        }
    }

    @Override
    protected void initCustomActions() {
        initNCBOSearchField();

        addCreateWindowActionToToolbar(VIEWS_PANEL, "Graph", Graph.ID);
        addCreateWindowActionToToolbar(VIEWS_PANEL, "Text",
                TextVisualization.ID);
        addCreateWindowActionToToolbar(VIEWS_PANEL, "Timeline", TimeLine.ID);
        addCreateWindowActionToToolbar(VIEWS_PANEL, "Comment",
                WorkbenchInitializer.WINDOW_CONTENT_COMMENT);
    }

    @Override
    protected void initCustomPanels() {
        addToolbarPanel(NCBO_SEARCH, "BioPortal Concept Search");
    }

    private void initNCBOSearchField() {
        TextCommandPresenter presenter = new TextCommandPresenter(
                ncboConceptSearchCommand, "Search");

        presenter.init();

        addWidget(NCBO_SEARCH, presenter.getTextBox());
        addWidget(NCBO_SEARCH, presenter.getExecuteButton());
    }

}
