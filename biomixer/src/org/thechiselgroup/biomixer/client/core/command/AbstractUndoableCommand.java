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

/**
 * Abstract undoable command that keeps track of state.
 * 
 * @author Del Myers
 * 
 */
public abstract class AbstractUndoableCommand implements UndoableCommand {
    private boolean hasExecuted;

    /**
     * Default constructor, sets the executed state to false.
     */
    protected AbstractUndoableCommand() {
        this(false);
    }

    /**
     * Protected constructor that allows clients to set the executed state. For
     * example, if a command is being executed as a result of an interaction in
     * the UI, and the command has already been executed by the user before
     * creation.
     * 
     * @param hasExecuted
     */
    protected AbstractUndoableCommand(boolean hasExecuted) {
        this.hasExecuted = hasExecuted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.Command#execute()
     */
    @Override
    public final void execute() {
        performExecute();
        hasExecuted = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.choosel.core.client.command.UndoableCommand#hasExecuted
     * ()
     */
    @Override
    public boolean hasExecuted() {
        return hasExecuted;
    }

    /**
     * Performs this command's execute functionality.
     */
    public abstract void performExecute();

    /**
     * Performs this command's undo functionality.
     */
    public abstract void performUndo();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.thechiselgroup.choosel.core.client.command.UndoableCommand#undo()
     */
    @Override
    public final void undo() {
        performUndo();
        hasExecuted = false;
    }

}
