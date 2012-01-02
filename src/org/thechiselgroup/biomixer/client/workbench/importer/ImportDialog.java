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
package org.thechiselgroup.biomixer.client.workbench.importer;

import org.thechiselgroup.biomixer.client.workbench.ui.dialog.AbstractDialog;
import org.thechiselgroup.choosel.core.client.importer.CSVStringTableParser;
import org.thechiselgroup.choosel.core.client.importer.Importer;
import org.thechiselgroup.choosel.core.client.importer.ParseException;
import org.thechiselgroup.choosel.core.client.importer.StringTable;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.ResourceSetContainer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ImportDialog extends AbstractDialog {

    private static final String CSS_IMPORT_PANEL = "choosel-ImportPanel";

    private static final String CSS_IMPORT_PANEL_DATA_SET_NAME = "choosel-ImportPanel-DataSetName";

    private static final String CSS_IMPORT_PANEL_LABEL = "choosel-ImportPanel-Label";

    private static final String CSS_IMPORT_PANEL_ERRORS = "choosel-ImportPanel-Errors";

    private static final String CSS_IMPORT_PANEL_HELP = "choosel-ImportPanel-Help";

    private TextArea pasteArea;

    private ResourceSetContainer targetContainer;

    private Importer importer;

    private TextBox nameTextBox;

    private Label errorLabel;

    private FlowPanel errorPanel;

    public ImportDialog(Importer importer, ResourceSetContainer targetContainer) {
        assert targetContainer != null;
        assert importer != null;

        this.importer = importer;
        this.targetContainer = targetContainer;
    }

    @Override
    public void cancel() {
    }

    @Override
    public Widget getContent() {
        VerticalPanel panel = new VerticalPanel();
        panel.addStyleName(CSS_IMPORT_PANEL);

        errorPanel = new FlowPanel();
        errorPanel.setStyleName(CSS_IMPORT_PANEL_ERRORS);
        errorPanel.add(new Label("Data could not be imported: "));
        errorLabel = new Label("");
        errorPanel.add(errorLabel);
        errorPanel.setVisible(false);
        panel.add(errorPanel);

        HTML help = new HTML();
        help.setStyleName(CSS_IMPORT_PANEL_HELP);
        help.setHTML("Here you can import your own data into Choosel"
                + " (limited to 400 rows).<br/>See <a target=\"_blank\" href=\""
                + GWT.getModuleBaseURL() + "html/csvexamples.html"
                + "\">example CSV data</a>"
                + " or <a target=\"_blank\" href=\"" + GWT.getModuleBaseURL()
                + "html/help_import.html"
                + "\">open help</a> for more information.");
        panel.add(help);

        FlowPanel namePanel = new FlowPanel();
        namePanel.addStyleName(CSS_IMPORT_PANEL_DATA_SET_NAME);
        Label nameLabel = new Label("Name of data set:");
        nameLabel.setStyleName(CSS_IMPORT_PANEL_LABEL);
        namePanel.add(nameLabel);
        nameTextBox = new TextBox();
        namePanel.add(nameTextBox);
        panel.add(namePanel);

        Label contentLabel = new Label("Paste CSV data below:");
        contentLabel.addStyleName(CSS_IMPORT_PANEL_LABEL);
        panel.add(contentLabel);

        pasteArea = new TextArea();
        panel.add(pasteArea);
        panel.setCellHeight(pasteArea, "100%");

        return panel;
    }

    @Override
    public String getHeader() {
        return "Import CSV";
    }

    @Override
    public String getOkayButtonLabel() {
        return "Import";
    }

    @Override
    public String getWindowTitle() {
        return "Import";
    }

    @Override
    public void handleException(Exception ex) {
        if (ex instanceof ParseException) {
            String msg = "";
            if (((ParseException) ex).getLineNumber() != -1) {
                msg += "Line " + ((ParseException) ex).getLineNumber() + " ";
            }
            if (((ParseException) ex).getUnparseableValue() != null) {
                msg += "\"" + ((ParseException) ex).getUnparseableValue()
                        + "\" ";
            }
            msg += ex.getMessage();
            errorLabel.setText(msg);
        } else {
            errorLabel.setText(ex.getMessage());
        }

        errorPanel.setVisible(true);
    }

    @Override
    public void okay() throws ParseException {
        String pastedText = pasteArea.getText();

        if (pastedText.length() > 75000) {
            throw new ParseException(
                    "The pasted text is too big. "
                            + "This demo supports only up to 75000 characters in the pasted text.");
        }

        StringTable parsedRows = new CSVStringTableParser().parse(pastedText);

        if (parsedRows.getColumnCount() > 20) {
            throw new ParseException(
                    "Too many columns. This demo supports only up to 20 columns.");
        }
        if (parsedRows.getRowCount() > 400) {
            throw new ParseException(
                    "Too many rows. This demo supports only up to 400 rows.");
        }

        ResourceSet parsedResources = importer.createResources(parsedRows);
        parsedResources.setLabel(nameTextBox.getText());
        targetContainer.addResourceSet(parsedResources);
    }
}