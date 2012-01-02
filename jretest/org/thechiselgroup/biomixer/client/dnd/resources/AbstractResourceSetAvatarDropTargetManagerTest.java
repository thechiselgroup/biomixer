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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.ui.popup.PopupFactory;
import org.thechiselgroup.biomixer.client.core.visualization.ViewAccessor;
import org.thechiselgroup.biomixer.client.dnd.test.DndTestHelpers;

import com.google.gwt.user.client.ui.Widget;

public class AbstractResourceSetAvatarDropTargetManagerTest {

    public static class TestDragAvatarDropTargetManager extends
            AbstractResourceSetAvatarDropTargetManager {

        public TestDragAvatarDropTargetManager(CommandManager commandManager,
                ResourceSetAvatarDragController dragController,
                ViewAccessor viewAccessor,
                DropTargetCapabilityChecker capabilityChecker,
                PopupFactory popupFactory) {

            super(commandManager, dragController, viewAccessor,
                    capabilityChecker, popupFactory);
        }

        @Override
        protected ResourceSetAvatarDropCommandFactory createCommandFactory(
                Widget widget, ViewAccessor viewAccessor) {
            return null;
        }
    }

    @Mock
    private DropTargetCapabilityChecker capabilityChecker;

    @Mock
    private CommandManager commandManager;

    @Mock
    private ResourceSetAvatarDragController dragController;

    private AbstractResourceSetAvatarDropTargetManager underTest;

    @Mock
    private ViewAccessor viewAccessor;

    @Mock
    private Widget widget;

    @Test
    public void disposeRemovesDropController() {
        underTest.disableDropTarget(widget);
        verify(dragController, times(1))
                .unregisterDropControllerFor(eq(widget));
    }

    @Test
    public void registerDropController() {
        underTest.enableDropTarget(widget);

        ArgumentCaptor<ResourceSetAvatarDropController> captor = ArgumentCaptor
                .forClass(ResourceSetAvatarDropController.class);

        verify(dragController, times(1)).registerDropController(
                captor.capture());

        assertEquals(widget, captor.getValue().getDropTarget());
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge bridge = MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);
        DndTestHelpers.mockDragClientBundle(bridge);

        underTest = spy(new TestDragAvatarDropTargetManager(commandManager,
                dragController, viewAccessor, capabilityChecker,
                mock(PopupFactory.class)));
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }

}
