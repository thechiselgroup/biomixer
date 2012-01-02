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
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.command.AsyncCommandExecutor;
import org.thechiselgroup.biomixer.client.core.ui.dialog.AbstractDialog;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogCallback;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePersistenceManager;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePreview;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class LoadWorkspaceDialog extends AbstractDialog {

    private final AsyncCommandExecutor asyncCommandExecutor;

    private Map<RadioButton, WorkspacePreview> buttonsToWorkspaces = new HashMap<RadioButton, WorkspacePreview>();

    private final WorkspacePersistenceManager persistenceManager;

    private RadioButton selectedButton;

    private List<WorkspacePreview> workspacePreviews;

    public LoadWorkspaceDialog(List<WorkspacePreview> workspacePreviews,
            AsyncCommandExecutor asyncCommandExecutor,
            WorkspacePersistenceManager persistenceManager) {
        this.workspacePreviews = workspacePreviews;
        this.asyncCommandExecutor = asyncCommandExecutor;
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void cancel() {
    }

    @Override
    public Widget getContent() {
        // TODO extract radio group widget
        // TODO autogenerate group id
        String groupId = "group";

        VerticalPanel content = new VerticalPanel();
        for (WorkspacePreview workspace : workspacePreviews) {
            RadioButton radioButton = new RadioButton(groupId,
                    workspace.getName());
            radioButton.setValue(Boolean.FALSE);
            buttonsToWorkspaces.put(radioButton, workspace);

            // highlight + select current workspace
            if (workspace.isCurrentWorkspace()) {
                radioButton.setText(workspace.getName() + " (current)");
                radioButton.setValue(Boolean.TRUE);

                setSelectedButton(radioButton);
            }

            radioButton
                    .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                        @Override
                        public void onValueChange(
                                ValueChangeEvent<Boolean> event) {
                            if (event.getValue() == Boolean.TRUE) {
                                setSelectedButton((RadioButton) event
                                        .getSource());
                            }
                        }
                    });

            content.add(radioButton);
        }

        return content;
    }

    @Override
    public String getHeader() {
        return "Load existing workspace";
    }

    @Override
    public String getOkayButtonLabel() {
        return "Load";
    }

    @Override
    public String getWindowTitle() {
        return "Load";
    }

    @Override
    public void init(DialogCallback callback) {
        super.init(callback);
        setOkayButtonEnabled(selectedButton != null);
    }

    @Override
    public void okay() {
        WorkspacePreview workspacePreview = buttonsToWorkspaces
                .get(selectedButton);
        LoadWorkspaceCommand loadWorkspaceCommand = new LoadWorkspaceCommand(
                workspacePreview.getId(), workspacePreview.getName(),
                persistenceManager);

        asyncCommandExecutor.execute(loadWorkspaceCommand);
    }

    private void setSelectedButton(RadioButton radioButton) {
        assert radioButton != null;
        selectedButton = radioButton;
        setOkayButtonEnabled(true);
    }

}