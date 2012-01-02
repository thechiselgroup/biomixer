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
package org.thechiselgroup.biomixer.client.core.command;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;

public interface CommandManager {

    <H extends CommandManagerEventHandler> HandlerRegistration addHandler(
            Type<H> type, H handler);

    boolean canRedo();

    boolean canUndo();

    void clear();

    /**
     * Executes the command if it has not already been executed. If it has
     * already been executed, then the command is pushed onto the command stack
     * without being executed again.
     */
    void execute(UndoableCommand command);

    UndoableCommand getRedoCommand();

    UndoableCommand getUndoCommand();

    void redo();

    void undo();

}