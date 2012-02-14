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

import org.thechiselgroup.biomixer.client.core.util.Initializable;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;

// TODO need command factory or command with execute(String) -- insertion buggy
// TODO presenter-display separation
public class TextCommandPresenter implements Initializable {

    private String buttonLabel;

    private Command command;

    private Button executeButton;

    private HasTextParameter hasTextParameter;

    private TextBox textBox;

    public <T extends Command & HasTextParameter> TextCommandPresenter(
            T command, String buttonLabel) {

        assert command != null;
        assert buttonLabel != null;

        this.command = command;
        this.hasTextParameter = command;
        this.buttonLabel = buttonLabel;
    }

    public Button getExecuteButton() {
        return executeButton;
    }

    public TextBox getTextBox() {
        return textBox;
    }

    @Override
    public void init() {
        textBox = new TextBox();
        executeButton = new Button(buttonLabel);

        TextBoxActionHandler handler = new TextBoxActionHandler() {
            @Override
            protected void execute() {
                String query = textBox.getText().trim();
                if (query.length() == 0) {
                    return;
                }
                hasTextParameter.initParameter(query);

                // remove focus after enter / click
                textBox.setFocus(false);
                executeButton.setFocus(false);

                command.execute();
            }
        };

        executeButton.addClickHandler(handler);
        textBox.addKeyUpHandler(handler);
    }

}
