package org.thechiselgroup.biomixer.client.core.error_handling;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class UserFeedbackErrorHandlerTest {

    private UserFeedbackErrorHandler underTest;

    private final String genericErrorMessage = "generic error";

    @Before
    public void setUp() {
        underTest = new UserFeedbackErrorHandler();
    }

    @Test
    public void singleListener() {
        ThrowablesContainerEventListener listener = mock(ThrowablesContainerEventListener.class);
        underTest.addListener(listener);

        Throwable thrown = new Throwable(genericErrorMessage);
        underTest.handleError(thrown);

        // XXX implement equals for ThrowableCaughtEvent and ThrowableCaught?
        // The timestamp might be problematic
        ArgumentCaptor<ThrowableCaughtEvent> argument = ArgumentCaptor
                .forClass(ThrowableCaughtEvent.class);
        verify(listener).onThrowableCaughtAdded(argument.capture());
        assertTrue(thrown.equals(argument.getValue().getThrowableCaught()
                .getThrowable()));
    }

}
