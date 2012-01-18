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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.command.CommandAddedEvent;
import org.thechiselgroup.biomixer.client.core.command.CommandAddedEventHandler;
import org.thechiselgroup.biomixer.client.core.command.CommandManagerClearedEvent;
import org.thechiselgroup.biomixer.client.core.command.CommandManagerClearedEventHandler;
import org.thechiselgroup.biomixer.client.core.command.CommandRedoneEvent;
import org.thechiselgroup.biomixer.client.core.command.CommandRedoneEventHandler;
import org.thechiselgroup.biomixer.client.core.command.CommandUndoneEvent;
import org.thechiselgroup.biomixer.client.core.command.CommandUndoneEventHandler;
import org.thechiselgroup.biomixer.client.core.command.DefaultCommandManager;
import org.thechiselgroup.biomixer.client.core.command.UndoableCommand;

public class DefaultCommandManagerTest {

    @Mock
    private UndoableCommand command1;

    @Mock
    private UndoableCommand command2;

    private DefaultCommandManager underTest;

    @Test
    public void addedCommandIsUndoCommand() {
        when(command1.hasExecuted()).thenReturn(true);

        underTest.execute(command1);
        assertEquals(command1, underTest.getUndoCommand());
    }

    @Test
    public void cannotRedoAfterClear() {
        underTest.execute(command1);
        underTest.undo();
        underTest.clear();
        assertEquals(false, underTest.canRedo());
    }

    @Test
    public void cannotRedoByDefault() {
        assertEquals(false, underTest.canRedo());
    }

    @Test
    public void cannotUndoAfterClear() {
        underTest.execute(command1);
        underTest.clear();
        assertEquals(false, underTest.canUndo());
    }

    @Test
    public void cannotUndoAfterUndoneExecutedCommand() {
        underTest.execute(command1);
        underTest.undo();
        assertEquals(false, underTest.canUndo());
    }

    @Test
    public void cannotUndoByDefault() {
        assertEquals(false, underTest.canUndo());
    }

    @Test
    public void canRedo2ndCommandAfterCommandUndone() {
        underTest.execute(command1);
        underTest.execute(command2);
        underTest.undo();
        underTest.undo();
        underTest.redo();
        assertEquals(true, underTest.canRedo());
        assertEquals(command2, underTest.getRedoCommand());
    }

    @Test
    public void canRedoAfterCommandUndone() {
        underTest.execute(command1);
        assertEquals(false, underTest.canRedo());

        underTest.undo();
        assertEquals(true, underTest.canRedo());
    }

    @Test
    public void canUndoAfterAddedCommand() {
        underTest.execute(command1);
        assertEquals(true, underTest.canUndo());
    }

    @Test
    public void canUndoAfterExecutedCommand() {
        underTest.execute(command1);
        assertEquals(true, underTest.canUndo());
    }

    @Test
    public void canUndoRedoneCommand() {
        underTest.execute(command1);
        underTest.undo();
        underTest.redo();
        assertEquals(true, underTest.canUndo());
    }

    @Test
    public void clearRedoStackOnExecute() {
        underTest.execute(command1);
        underTest.undo();
        underTest.execute(command2);
        assertEquals(false, underTest.canRedo());
    }

    @Test
    public void executeCommand() {
        underTest.execute(command1);
        verify(command1, times(1)).execute();
    }

    @Test
    public void executedCommandIsNotReExecuted() {
        when(command1.hasExecuted()).thenReturn(true);
        underTest.execute(command1);
        verify(command1, times(0)).execute();
    }

    @Test
    public void executedCommandIsUndoCommand() {
        underTest.execute(command1);
        assertEquals(command1, underTest.getUndoCommand());
    }

    @Test
    public void fireClearEventOnClear() {
        CommandManagerClearedEventHandler handler = mock(CommandManagerClearedEventHandler.class);

        underTest.execute(command1);
        underTest.addHandler(CommandManagerClearedEvent.TYPE, handler);
        underTest.clear();

        ArgumentCaptor<CommandManagerClearedEvent> argument = ArgumentCaptor
                .forClass(CommandManagerClearedEvent.class);
        verify(handler, times(1)).onCleared(argument.capture());

        assertEquals(null, argument.getValue().getCommand());
        assertEquals(underTest, argument.getValue().getCommandManager());
    }

    @Test
    public void fireCommandAddedEventOnExecute() {
        CommandAddedEventHandler handler = mock(CommandAddedEventHandler.class);

        underTest.addHandler(CommandAddedEvent.TYPE, handler);
        underTest.execute(command1);

        ArgumentCaptor<CommandAddedEvent> argument = ArgumentCaptor
                .forClass(CommandAddedEvent.class);
        verify(handler, times(1)).onCommandAdded(argument.capture());

        assertEquals(command1, argument.getValue().getCommand());
        assertEquals(underTest, argument.getValue().getCommandManager());
    }

    @Test
    public void fireCommandAddedEventWhenAlreadyExecutedCommandIsReExecuted() {
        when(command1.hasExecuted()).thenReturn(true);
        CommandAddedEventHandler handler = mock(CommandAddedEventHandler.class);

        underTest.addHandler(CommandAddedEvent.TYPE, handler);

        underTest.execute(command1);

        ArgumentCaptor<CommandAddedEvent> argument = ArgumentCaptor
                .forClass(CommandAddedEvent.class);
        verify(handler, times(1)).onCommandAdded(argument.capture());

        assertEquals(command1, argument.getValue().getCommand());
        assertEquals(underTest, argument.getValue().getCommandManager());
    }

    @Test
    public void fireCommandRedoneEventOnRedo() {
        CommandRedoneEventHandler handler = mock(CommandRedoneEventHandler.class);

        underTest.addHandler(CommandRedoneEvent.TYPE, handler);
        underTest.execute(command1);
        underTest.undo();
        underTest.redo();

        ArgumentCaptor<CommandRedoneEvent> argument = ArgumentCaptor
                .forClass(CommandRedoneEvent.class);
        verify(handler, times(1)).onCommandRedone(argument.capture());

        assertEquals(command1, argument.getValue().getCommand());
        assertEquals(underTest, argument.getValue().getCommandManager());
    }

    @Test
    public void fireCommandUndoneEventOnUndo() {
        CommandUndoneEventHandler handler = mock(CommandUndoneEventHandler.class);

        underTest.addHandler(CommandUndoneEvent.TYPE, handler);
        underTest.execute(command1);
        underTest.undo();

        ArgumentCaptor<CommandUndoneEvent> argument = ArgumentCaptor
                .forClass(CommandUndoneEvent.class);
        verify(handler, times(1)).onCommandUndone(argument.capture());

        assertEquals(command1, argument.getValue().getCommand());
        assertEquals(underTest, argument.getValue().getCommandManager());
    }

    @Test
    public void redoUndoneCommand() {
        underTest.execute(command1);
        verify(command1, times(1)).execute();
        underTest.undo();
        verify(command1, times(1)).execute();
        underTest.redo();
        verify(command1, times(2)).execute();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new DefaultCommandManager();
    }

    @Test
    public void undoExecutedCommand() {
        underTest.execute(command1);
        verify(command1, times(0)).undo();

        underTest.undo();
        verify(command1, times(1)).undo();
    }

    @Test
    public void undoneCommandIsRedoCommand() {
        underTest.execute(command1);
        underTest.undo();
        assertEquals(command1, underTest.getRedoCommand());
    }

}
