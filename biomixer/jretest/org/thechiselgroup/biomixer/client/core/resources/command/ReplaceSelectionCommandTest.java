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
package org.thechiselgroup.biomixer.client.core.resources.command;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.SelectionModel;

public class ReplaceSelectionCommandTest {

    private ReplaceSelectionCommand command;

    private ResourceSet sourceSet;

    private ResourceSet targetSet;

    @Mock
    private SelectionModel selectionModel;

    @Test
    public void setNewSelectionSetOnExecute() {
        setUpCommand(ResourceSetTestUtils.createResources(1, 2, 3),
                ResourceSetTestUtils.createResources());

        command.execute();

        verify(selectionModel, times(1)).setSelection(eq(targetSet));
    }

    @Test
    public void setOldSelectionSetOnUndo() {
        setUpCommand(ResourceSetTestUtils.createResources(1, 2, 3),
                ResourceSetTestUtils.createResources());

        command.execute();
        command.undo();

        verify(selectionModel, times(1)).setSelection(eq(targetSet)); // from
                                                                      // execute
        verify(selectionModel, times(1)).setSelection(eq(sourceSet));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    private void setUpCommand(ResourceSet sourceSet, ResourceSet targetSet) {
        this.sourceSet = sourceSet;
        this.targetSet = targetSet;

        this.command = new ReplaceSelectionCommand(selectionModel, targetSet);

        when(selectionModel.getSelection()).thenReturn(sourceSet);
    }

}
