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
package org.thechiselgroup.biomixer.client.workbench.client.command.ui;

import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.command.UndoableCommand;
import org.thechiselgroup.biomixer.client.core.ui.Action;
import org.thechiselgroup.biomixer.client.core.util.Initializable;

public class RedoActionStateController extends
        CommandManagerActionStateController implements Initializable {

    public RedoActionStateController(CommandManager commandManager,
            Action action) {
        super(commandManager, action);
    }

    @Override
    protected boolean canPerformOperation() {
        return commandManager.canRedo();
    }

    @Override
    protected UndoableCommand getCommand() {
        return commandManager.getRedoCommand();
    }
}
