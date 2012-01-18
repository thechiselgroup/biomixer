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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.workbench.workspace.WorkspacePresenter.WorkspacePresenterDisplay;
import org.thechiselgroup.biomixer.shared.core.test.mockito.StubbingArgumentCaptor;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;

public class WorkspacePresenterTest {

    private static final String NEW_WORKSPACE_NAME = "new-workspace-name";

    private static final String TEST_WORKSPACE_NAME = "test-workspace-name";

    private StubbingArgumentCaptor<HandlerRegistration> blurHandlerCaptor;

    @Mock
    private WorkspacePresenterDisplay display;

    private WorkspacePresenter presenter;

    @Mock
    private HasText textContainer;

    @Mock
    private HasBlurHandlers textFieldBlurHandlers;

    @Mock
    private HasKeyUpHandlers textFieldKeyUpHandlers;

    private Workspace workspace;

    @Mock
    private WorkspaceManager workspaceManager;

    @Test
    public void changeWorkspaceNameOnRegularKeyPress() {
        getTextBlurHandler().onBlur(new BlurEvent() {
        });

        verify(textContainer).getText();
        verify(workspace, times(1)).setName(NEW_WORKSPACE_NAME);
    }

    private BlurHandler getTextBlurHandler() {
        return blurHandlerCaptor.getAs(BlurHandler.class, 0);
    }

    @Test
    public void initialState() {
        verify(workspaceManager).getWorkspace();
        verify(textContainer).setText(TEST_WORKSPACE_NAME);
        verify(display).getTextBlurHandlers();
        verify(textFieldBlurHandlers).addBlurHandler(any(BlurHandler.class));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        blurHandlerCaptor = new StubbingArgumentCaptor<HandlerRegistration>();

        workspace = spy(new Workspace());
        workspace.setName(TEST_WORKSPACE_NAME);

        when(workspaceManager.getWorkspace()).thenReturn(workspace);
        when(display.getTextBlurHandlers()).thenReturn(textFieldBlurHandlers);
        when(display.getWorkspaceNameText()).thenReturn(textContainer);
        when(display.getTextKeyUpHandlers()).thenReturn(textFieldKeyUpHandlers);
        when(textContainer.getText()).thenReturn(NEW_WORKSPACE_NAME);

        when(textFieldBlurHandlers.addBlurHandler(any(BlurHandler.class)))
                .thenAnswer(blurHandlerCaptor);

        presenter = new WorkspacePresenter(workspaceManager, display);
        presenter.init();
    }
}