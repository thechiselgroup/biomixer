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

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.dnd.windows.Desktop;
import org.thechiselgroup.choosel.core.client.command.CommandManager;

public class DefaultWorkspaceManagerTest {

    @Mock
    private CommandManager commandManager;

    @Mock
    private Desktop desktop;

    private DefaultWorkspaceManager underTest;

    @Test
    public void clearCommandManagerOnCreateNewWorkspace() {
        underTest.createNewWorkspace();

        verify(commandManager).clear();
    }

    @Test
    public void clearCommandManagerOnSetWorkspace() {
        underTest.setWorkspace(new Workspace());

        verify(commandManager).clear();
    }

    @Test
    public void clearDesktopOnCreateNewWorkspace() {
        underTest.createNewWorkspace();

        verify(desktop).clearWindows();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new DefaultWorkspaceManager(desktop, commandManager);
    }

}
