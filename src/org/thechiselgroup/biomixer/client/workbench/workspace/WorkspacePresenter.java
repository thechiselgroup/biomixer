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
package org.thechiselgroup.biomixer.client.workbench.workspace;

import org.thechiselgroup.choosel.core.client.ui.ResizingTextBox;
import org.thechiselgroup.choosel.core.client.util.Initializable;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

// TODO split into different presenters for save & title
public class WorkspacePresenter implements Initializable {

    public static class DefaultWorkspacePresenterDisplay implements
            WorkspacePresenterDisplay {

        private TextBox textBox;

        public DefaultWorkspacePresenterDisplay() {
            this.textBox = new ResizingTextBox(50, 500);
        }

        @Override
        public HasBlurHandlers getTextBlurHandlers() {
            return textBox;
        }

        public TextBox getTextBox() {
            return textBox;
        }

        @Override
        public HasKeyUpHandlers getTextKeyUpHandlers() {
            return textBox;
        }

        @Override
        public HasText getWorkspaceNameText() {
            return textBox;
        }

    }

    public interface WorkspacePresenterDisplay {

        HasBlurHandlers getTextBlurHandlers();

        HasKeyUpHandlers getTextKeyUpHandlers();

        HasText getWorkspaceNameText();

    }

    private WorkspacePresenterDisplay display;

    private WorkspaceManager manager;

    @Inject
    public WorkspacePresenter(WorkspaceManager manager,
            WorkspacePresenterDisplay display) {

        this.manager = manager;
        this.display = display;

        String name = manager.getWorkspace().getName();
        display.getWorkspaceNameText().setText(name);
    }

    @Override
    public void init() {
        display.getTextKeyUpHandlers().addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateWorkspaceName();
            }

        });

        display.getTextBlurHandlers().addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                updateWorkspaceName();
            }
        });

        manager.addSwitchedWorkspaceEventHandler(new WorkspaceSwitchedEventHandler() {
            @Override
            public void onWorkspaceSwitched(WorkspaceSwitchedEvent event) {
                display.getWorkspaceNameText().setText(
                        event.getWorkspace().getName());
            }
        });
    }

    private void updateWorkspaceName() {
        manager.getWorkspace()
                .setName(display.getWorkspaceNameText().getText());
    }
}