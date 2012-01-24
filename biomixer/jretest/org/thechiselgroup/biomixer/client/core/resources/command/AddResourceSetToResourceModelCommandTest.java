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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

public class AddResourceSetToResourceModelCommandTest {

    private ResourceSet resources;

    private AddResourceSetToViewCommand underTest;

    @Mock
    private ResourceModel resourceModel;

    @Test
    public void addAlreadyContainedResourcesOnUndo() {
        ResourceSet viewResources = ResourceSetTestUtils.createResources(1);
        when(resourceModel.getResources()).thenReturn(viewResources);

        setUpCommand(ResourceSetTestUtils.createLabeledResources(1, 2));
        when(resourceModel.containsResourceSet(resources)).thenReturn(false,
                true, true, false); // for assertions to work in command

        underTest.execute();
        underTest.undo();

        ArgumentCaptor<Collection> argument = ArgumentCaptor
                .forClass(Collection.class);
        verify(resourceModel, times(1)).addUnnamedResources(argument.capture());

        assertEquals(
                true,
                argument.getValue().contains(
                        ResourceSetTestUtils.createResource(1)));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private void setUpCommand(ResourceSet resources) {
        this.resources = resources;
        this.underTest = new AddResourceSetToViewCommand(resourceModel,
                resources);
    }

}