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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.choosel.core.client.command.UndoableCommand;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.ResourceSetTestUtils;
import org.thechiselgroup.choosel.core.client.resources.command.AddResourceSetToResourceSetCommand;
import org.thechiselgroup.choosel.core.client.resources.command.MergeResourceSetsCommand;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.choosel.core.client.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.choosel.core.client.visualization.View;
import org.thechiselgroup.choosel.core.client.visualization.ViewAccessor;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualizationModel;
import org.thechiselgroup.choosel.core.client.visualization.model.extensions.ResourceModel;

public class ResourceSetPresenterDropCommandFactoryTest {

    @Mock
    private ViewAccessor accessor;

    @Mock
    private ResourceSetAvatar dragAvatar;

    private ResourceSetPresenterDropCommandFactory dropCommandFactory;

    @Mock
    private ResourceSet sourceSet;

    @Mock
    private ResourceSetAvatar targetDragAvatar;

    @Mock
    private ResourceSet targetSet;

    @Mock
    private View view;

    @Mock
    private ResourceModel resourceModel;

    @Mock
    private VisualizationModel viewModel;

    @Test
    public void canDropByDefault() {
        when(targetSet.isModifiable()).thenReturn(true);
        assertEquals(true, dropCommandFactory.canDrop(dragAvatar));
    }

    @Test
    public void cannotDropIfAllResourcesAreAlreadyContained() {
        sourceSet = ResourceSetTestUtils.createResources(1, 2);
        targetSet = ResourceSetTestUtils.createResources(1, 2, 3, 4);

        when(targetDragAvatar.getResourceSet()).thenReturn(targetSet);
        when(dragAvatar.getResourceSet()).thenReturn(sourceSet);

        assertEquals(false, dropCommandFactory.canDrop(dragAvatar));
    }

    @Test
    public void cannotDropOnSameResources() {
        when(targetDragAvatar.getResourceSet()).thenReturn(sourceSet);
        when(sourceSet.isModifiable()).thenReturn(true);

        assertEquals(false, dropCommandFactory.canDrop(dragAvatar));
    }

    @Test
    public void cannotDropOnUnmodifiableResourceSets() {
        when(targetSet.isModifiable()).thenReturn(false);
        assertEquals(false, dropCommandFactory.canDrop(dragAvatar));
    }

    @Test
    public void createAddResourceSetToResourceSetCommand() {
        when(targetSet.isModifiable()).thenReturn(true);

        UndoableCommand result = dropCommandFactory.createCommand(dragAvatar);

        assertNotNull(result);
        assertEquals(true, result instanceof AddResourceSetToResourceSetCommand);
        assertEquals(true, !(result instanceof MergeResourceSetsCommand));

        AddResourceSetToResourceSetCommand result2 = (AddResourceSetToResourceSetCommand) result;

        assertEquals(sourceSet, result2.getAddedSet());
        assertEquals(targetSet, result2.getModifiedSet());
    }

    @Test
    public void createAddResourceSetToResourceSetCommandWhenAvatarsFromSameViewButSourceTypeIsSelection() {
        when(targetSet.isModifiable()).thenReturn(true);
        when(accessor.findView(dragAvatar)).thenReturn(view);
        when(dragAvatar.getType()).thenReturn(ResourceSetAvatarType.SELECTION);

        UndoableCommand result = dropCommandFactory.createCommand(dragAvatar);

        assertNotNull(result);
        assertEquals(true, !(result instanceof MergeResourceSetsCommand));

        AddResourceSetToResourceSetCommand result2 = (AddResourceSetToResourceSetCommand) result;

        assertEquals(sourceSet, result2.getAddedSet());
        assertEquals(targetSet, result2.getModifiedSet());
    }

    @Test
    public void createMergeResourceCommandWhenAvatarsFromSameViewAndSourceTypeIsSet() {
        when(targetSet.isModifiable()).thenReturn(true);
        when(accessor.findView(dragAvatar)).thenReturn(view);
        when(dragAvatar.getType()).thenReturn(ResourceSetAvatarType.SET);

        UndoableCommand result = dropCommandFactory.createCommand(dragAvatar);

        assertNotNull(result);
        assertEquals(true, result instanceof MergeResourceSetsCommand);

        MergeResourceSetsCommand result2 = (MergeResourceSetsCommand) result;

        assertEquals(sourceSet, result2.getAddedSet());
        assertEquals(targetSet, result2.getModifiedSet());
        assertEquals(resourceModel, result2.getResourceModel());
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        when(targetDragAvatar.getResourceSet()).thenReturn(targetSet);
        when(dragAvatar.getResourceSet()).thenReturn(sourceSet);
        when(accessor.findView(targetDragAvatar)).thenReturn(view);
        when(view.getModel()).thenReturn(viewModel);
        when(view.getResourceModel()).thenReturn(resourceModel);

        dropCommandFactory = new ResourceSetPresenterDropCommandFactory(
                targetDragAvatar, accessor);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }
}
