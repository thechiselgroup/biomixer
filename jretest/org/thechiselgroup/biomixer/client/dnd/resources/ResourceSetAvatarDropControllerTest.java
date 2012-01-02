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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.dnd.test.DndTestHelpers;
import org.thechiselgroup.choosel.core.client.command.CommandManager;
import org.thechiselgroup.choosel.core.client.command.TestUndoableCommandWithDescription;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.ResourceSetTestUtils;
import org.thechiselgroup.choosel.core.client.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.choosel.core.client.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.choosel.core.client.ui.popup.DelayedPopupManager;
import org.thechiselgroup.choosel.core.client.ui.popup.PopupFactory;
import org.thechiselgroup.choosel.core.client.visualization.View;
import org.thechiselgroup.choosel.core.client.visualization.ViewAccessor;
import org.thechiselgroup.choosel.core.client.visualization.model.Slot;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualizationModel;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.google.gwt.user.client.ui.Widget;

public class ResourceSetAvatarDropControllerTest {

    public static class TestDragAvatarDropController extends
            ResourceSetAvatarDropController {

        private TestDragAvatarDropController(Widget dropTarget,
                ResourceSetAvatarDropCommandFactory commandFactory,
                CommandManager commandManager, ViewAccessor viewAccessor,
                DropTargetCapabilityChecker capabilityChecker,
                PopupFactory popupFactory) {

            super(dropTarget, commandFactory, commandManager, viewAccessor,
                    capabilityChecker, popupFactory);
        }

        @Override
        protected DelayedPopupManager createPopup(DragContext context,
                String message) {
            return null;
        }
    }

    @Mock
    private DropTargetCapabilityChecker capabilityChecker;

    @Mock
    private TestUndoableCommandWithDescription command;

    @Mock
    private ResourceSetAvatarDropCommandFactory commandFactory;

    @Mock
    private CommandManager commandManager;

    @Mock
    private ResourceSetAvatar dragAvatar;

    @Mock
    private DragContext dragContext;

    @Mock
    private Widget dropTarget;

    @Mock
    private DelayedPopupManager popup;

    private ResourceSet resources;

    private ResourceSetAvatarDropController underTest;

    @Mock
    private View view;

    @Mock
    private ViewAccessor viewAccessor;

    @Mock
    private VisualizationModel viewModel;

    @Test
    public void addExecutedCommandToCommandManager() {
        String message = "message";

        when(commandFactory.canDrop(eq(dragAvatar))).thenReturn(true);
        when(command.getDescription()).thenReturn(message);

        underTest.onEnter(dragContext);

        verify(underTest, times(1)).createPopup(eq(dragContext), eq(message));
        verify(popup, times(1)).showDelayed();
    }

    /**
     * Because the preview changes the underlying views, another call to canDrop
     * might yield false as result. Thus canDrop should not be called while in
     * preview state - instead assumed to be true.
     */
    @Test
    public void assumeCanDropWhenAlreadyPreviewing() {
        when(commandFactory.canDrop(eq(dragAvatar))).thenReturn(true, false);

        underTest.onEnter(dragContext);
        underTest.onDrop(dragContext);

        verify(command, times(1)).execute();
        verify(commandManager, times(1)).execute(command);
    }

    @Test
    public void hidePopupOnDrop() {
        when(commandFactory.canDrop(eq(dragAvatar))).thenReturn(true);

        underTest.onEnter(dragContext);
        underTest.onDrop(dragContext);

        verify(popup, times(1)).hideDelayed();
    }

    @Test
    public void hidePopupOnLeave() {
        when(commandFactory.canDrop(eq(dragAvatar))).thenReturn(true);

        underTest.onEnter(dragContext);
        underTest.onLeave(dragContext);

        verify(popup, times(1)).hideDelayed();
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge bridge = MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);
        DndTestHelpers.mockDragClientBundle(bridge);

        underTest = spy(new TestDragAvatarDropController(dropTarget,
                commandFactory, commandManager, viewAccessor,
                capabilityChecker, mock(PopupFactory.class)));

        when(underTest.createPopup(any(DragContext.class), any(String.class)))
                .thenReturn(popup);

        dragContext.draggable = dragAvatar;
        when(commandFactory.createCommand(eq(dragAvatar))).thenReturn(command);
        when(
                capabilityChecker.isValidDrop(any(Slot[].class),
                        any(ResourceSet.class))).thenReturn(true);
        when(viewAccessor.findView(any(Widget.class))).thenReturn(view);
        when(view.getModel()).thenReturn(viewModel);
        resources = ResourceSetTestUtils.createResources(1, 2);
        when(dragAvatar.getResourceSet()).thenReturn(resources);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }

    /**
     * Because the preview changes the underlying views, another call to canDrop
     * might yield false as result. Thus canDrop should not be called while in
     * preview state - instead assumed to be true.
     */
    @Test
    public void undoEvenWhenCanDropSwitchedToFalse() {
        when(commandFactory.canDrop(eq(dragAvatar))).thenReturn(true, false);

        underTest.onEnter(dragContext);
        underTest.onLeave(dragContext);

        verify(command, times(1)).execute();
        verify(command, times(1)).undo();
    }

    @Test
    public void useMessageFromCommandInPopup() {
        when(commandFactory.canDrop(eq(dragAvatar))).thenReturn(true);

        underTest.onDrop(dragContext);

        verify(command, times(1)).execute();
        //should not re-execute the command... just add it to the stack
        verify(commandManager, times(1)).execute(command);
    }
}
