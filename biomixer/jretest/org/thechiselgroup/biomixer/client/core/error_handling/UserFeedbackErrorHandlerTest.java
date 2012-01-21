/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.core.error_handling;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class UserFeedbackErrorHandlerTest {

    private UserFeedbackErrorHandler underTest;

    private final String errorMessage = "errorMessage";

    @Before
    public void setUp() {
        underTest = new UserFeedbackErrorHandler();
    }

    @Test
    public void singleListener() {
        ThrowablesContainerEventListener listener = mock(ThrowablesContainerEventListener.class);
        underTest.addListener(listener);

        Throwable thrown = new Throwable(errorMessage);
        underTest.handleError(thrown);

        // XXX implement equals for ThrowableCaughtEvent and ThrowableCaught?
        // The timestamp might be problematic
        ArgumentCaptor<ThrowableCaughtEvent> argument = ArgumentCaptor
                .forClass(ThrowableCaughtEvent.class);
        verify(listener).onThrowableCaughtAdded(argument.capture());
        assertThat(argument.getValue().getThrowableCaught().getThrowable(),
                equalTo(thrown));
    }

}
