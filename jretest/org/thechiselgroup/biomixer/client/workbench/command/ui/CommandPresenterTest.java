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
package org.thechiselgroup.biomixer.client.workbench.command.ui;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.thechiselgroup.biomixer.client.workbench.client.command.ui.CommandPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.IsWidget;

public class CommandPresenterTest {

    private interface CommandDisplay extends HasClickHandlers, IsWidget {

    }

    protected ClickHandler clickHandler;

    @Mock
    private Command command;

    @Mock
    private CommandDisplay display;

    private CommandPresenter presenter;

    @Test
    public void callExecuteOnClick() {
        clickHandler.onClick(new ClickEvent() {
        });

        verify(command, times(1)).execute();
    }

    @Test
    public void initialState() {
        verify(display).addClickHandler(any(ClickHandler.class));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // TODO extract to utility class with reflection
        when(display.addClickHandler(any(ClickHandler.class))).thenAnswer(
                new Answer<HandlerRegistration>() {
                    @Override
                    public HandlerRegistration answer(
                            InvocationOnMock invocation) {

                        clickHandler = (ClickHandler) invocation.getArguments()[0];
                        return null;
                    }
                });

        presenter = new CommandPresenter(display, command);
        presenter.init();
    }

}