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
package org.thechiselgroup.biomixer.client.dnd.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.choosel.core.client.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.choosel.core.client.visualization.View;
import org.thechiselgroup.choosel.core.client.visualization.ViewAccessor;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualizationModel;
import org.thechiselgroup.choosel.core.client.visualization.model.extensions.ResourceModel;

public class AllSetCommandFactoryTest {

    @Mock
    private ViewAccessor accessor;

    @Mock
    private ResourceSetAvatar dragAvatar;

    @Mock
    private ResourceSet resources;

    @Mock
    private ResourceSetAvatar targetDragAvatar;

    private AllSetDropCommandFactory underTest;

    @Mock
    private ResourceModel resourceModel;

    @Mock
    private View view;

    @Mock
    private VisualizationModel viewModel;

    @Test
    public void cannotDropIfAllResourcesAreAlreadyContainedInView() {
        when(resourceModel.containsResources(resources)).thenReturn(true);
        when(dragAvatar.getResourceSet()).thenReturn(resources);

        assertEquals(false, underTest.canDrop(dragAvatar));
    }

    @Test
    public void cannotDropIfFromSameView() {
        when(accessor.findView(eq(dragAvatar))).thenReturn(view);

        assertEquals(false, underTest.canDrop(dragAvatar));
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        underTest = new AllSetDropCommandFactory(targetDragAvatar, accessor);

        when(dragAvatar.getResourceSet()).thenReturn(resources);
        when(accessor.findView(targetDragAvatar)).thenReturn(view);
        when(view.getModel()).thenReturn(viewModel);
        when(view.getResourceModel()).thenReturn(resourceModel);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }

}
