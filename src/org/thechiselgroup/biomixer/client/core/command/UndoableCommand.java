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

import com.google.gwt.user.client.Command;

/**
 * Undoable command interface. Clients should not implement this interface. Use
 * {@link AbstractUndoableCommand} instead.
 * 
 * @author Lars Grammel
 * 
 */
public interface UndoableCommand extends Command {

    /**
     * @return True, if {@link #execute()} has been called and {@link #undo()}
     *         has not been called after that. Returns false if {@link #undo()}
     *         has been called.
     */
    boolean hasExecuted();

    // TODO introduce AbstractUndoableCommand with appropriate template methods

    void undo();

}