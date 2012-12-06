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
import org.thechiselgroup.biomixer.client.search.ConceptSearchCommand;
import org.thechiselgroup.biomixer.client.search.OntologySearchCommand;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphOntologyOverviewViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.visualization_component.matrix.ConceptMatrixViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.visualization_component.text.TextViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.visualization_component.timeline.TimeLineViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.workbench.init.WorkbenchInitializer;

import com.google.inject.Inject;

public class BioMixerWorkbench extends WorkbenchInitializer {

    @Inject
    private ConceptSearchCommand ncboConceptSearchCommand;

    @Inject
    private OntologySearchCommand ncboOntologySearchCommand;

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
        initNCBOSearchConceptsField();
        initNCBOSearchOntologiesField();

        // TODO Why is this prepared with strings that are indexed into a
        // registry when we can hard-code classes used or use injection?
        addCreateWindowActionToToolbar(VIEWS_PANEL, "Graph",
                GraphViewContentDisplayFactory.ID);
        addCreateWindowActionToToolbar(VIEWS_PANEL, "Ontology Overview",
                GraphOntologyOverviewViewContentDisplayFactory.ID);
        addCreateWindowActionToToolbar(VIEWS_PANEL, "Mapping Matrix",
                ConceptMatrixViewContentDisplayFactory.ID);
        addCreateWindowActionToToolbar(VIEWS_PANEL, "Text",
                TextViewContentDisplayFactory.ID);
        addCreateWindowActionToToolbar(VIEWS_PANEL, "Timeline",
                TimeLineViewContentDisplayFactory.ID);
        addCreateWindowActionToToolbar(VIEWS_PANEL, "Comment",
                WorkbenchInitializer.WINDOW_CONTENT_COMMENT);
    }

    @Override
    protected void initCustomPanels() {
        addToolbarPanel(ncboConceptSearchCommand.getContentType(),
                "BioPortal Concept Search");
        addToolbarPanel(ncboOntologySearchCommand.getContentType(),
                "BioPortal Ontology Search");
    }

    private void initNCBOSearchConceptsField() {
        TextCommandPresenter presenter = new TextCommandPresenter(
                ncboConceptSearchCommand, "Search Concepts");

        presenter.init();

        addWidget(ncboConceptSearchCommand.getContentType(),
                presenter.getTextBox());
        addWidget(ncboConceptSearchCommand.getContentType(),
                presenter.getExecuteButton());
    }

    private void initNCBOSearchOntologiesField() {
        TextCommandPresenter presenter = new TextCommandPresenter(
                ncboOntologySearchCommand, "Search Ontologies");

        presenter.init();

        presenter.setAllowEmpty(true);

        addWidget(ncboOntologySearchCommand.getContentType(),
                presenter.getTextBox());
        addWidget(ncboOntologySearchCommand.getContentType(),
                presenter.getExecuteButton());
    }

}
