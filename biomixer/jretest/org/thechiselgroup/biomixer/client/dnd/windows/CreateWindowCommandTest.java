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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;

public class CreateWindowCommandTest {

    private CreateWindowCommand command;

    @Mock
    private Desktop desktop;

    @Mock
    private WindowContent viewContent;

    @Mock
    private WindowPanel windowPanel;

    @Test
    public void createWindowOnExecute() {
        command.execute();
        verify(desktop, times(1)).createWindow(viewContent);
    }

    @Test
    public void removeWindowOnUndo() {
        when(desktop.createWindow(eq(viewContent))).thenReturn(windowPanel);

        command.execute();
        command.undo();
        verify(desktop, times(1)).removeWindow(windowPanel);
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        command = new CreateWindowCommand(desktop, viewContent);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }

}
