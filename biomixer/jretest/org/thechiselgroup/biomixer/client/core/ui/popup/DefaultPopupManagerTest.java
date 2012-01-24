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
package org.thechiselgroup.biomixer.client.core.ui.popup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.ui.TestMouseOverEvent;

import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

public class DefaultPopupManagerTest {

    public abstract static class MockWidget extends Widget implements
            HasAllMouseHandlers, HasAttachHandlers {
    }

    public static class TestDefaultPopupManager extends DefaultPopupManager {

        public TestDefaultPopupManager(Popup popup) {
            super(popup);
        }

        @Override
        protected void cancelTimer() {
        }

        @Override
        protected Timer createTimer() {
            return null;
        }

        @Override
        protected void startTimer(int delayInMs) {
        }

    }

    @Mock
    private Popup popup;

    private DefaultPopupManager underTest;

    private MockWidget sourceWidget;

    @Test
    public void linkedAttachDoesNotCallPopupManager() {
        ArgumentCaptor<AttachEvent.Handler> argument = ArgumentCaptor
                .forClass(AttachEvent.Handler.class);
        verify(sourceWidget, times(1)).addAttachHandler(argument.capture());

        argument.getValue().onAttachOrDetach(new AttachEvent(true) {
        });

        verify(underTest, times(0)).onDetach();
    }

    @Test
    public void linkedDetachDoesCallPopupManager() {
        ArgumentCaptor<AttachEvent.Handler> argument = ArgumentCaptor
                .forClass(AttachEvent.Handler.class);
        verify(sourceWidget, times(1)).addAttachHandler(argument.capture());

        argument.getValue().onAttachOrDetach(new AttachEvent(false) {
        });

        verify(underTest, times(1)).onDetach();
    }

    @Test
    public void linkedMouseOverCallsPopupManager() {
        ArgumentCaptor<MouseOverHandler> argument = ArgumentCaptor
                .forClass(MouseOverHandler.class);
        verify(sourceWidget, times(1)).addMouseOverHandler(argument.capture());

        argument.getValue().onMouseOver(new TestMouseOverEvent(0, 0));

        verify(underTest, times(1)).onMouseOver(0, 0);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        underTest = spy(new TestDefaultPopupManager(popup));
        sourceWidget = mock(MockWidget.class);

        underTest.linkToWidget(sourceWidget);
    }

}
