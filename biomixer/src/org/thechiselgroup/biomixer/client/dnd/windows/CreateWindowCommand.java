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
package org.thechiselgroup.biomixer.client.dnd.windows;

import org.thechiselgroup.biomixer.client.core.command.AbstractUndoableCommand;
import org.thechiselgroup.biomixer.client.core.util.HasDescription;

// TODO store position
public class CreateWindowCommand extends AbstractUndoableCommand implements HasDescription {

    private final WindowContent content;

    private Desktop desktop;

    private WindowPanel windowPanel;

    public CreateWindowCommand(Desktop desktop, WindowContent content) {
        this.desktop = desktop;
        this.content = content;
    }

    @Override
    public void performExecute() {
        windowPanel = desktop.createWindow(content);
    }

    @Override
    public String getDescription() {
        return "Create window '" + content.getLabel() + "'";
    }

    public WindowPanel getWindow() {
        return windowPanel;
    }

    @Override
    public void performUndo() {
        assert windowPanel != null;

        // FIXME animations missing

        desktop.removeWindow(windowPanel);
    }

}