package org.thechiselgroup.biomixer.client.core.error_handling;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class UserFeedbackErrorHandlerTest {

    private UserFeedbackErrorHandler underTest;

    @Before
    public void setUp() {
        underTest = new UserFeedbackErrorHandler();
    }

    @Test
    public void singleListener() {
        TestThrowableEventListener listener = new TestThrowableEventListener();
        underTest.addListener(listener);
        assertThat(listener.getStatus(),
                equalTo(TestThrowableEventListener.Status.NOT_NOTIFIED));

        String errorMessage = "generic error";
        underTest.handleError(new Throwable(errorMessage));
        assertThat(listener.getStatus(),
                equalTo(TestThrowableEventListener.Status.NOTIFIED));

        ThrowableCaughtEvent throwableCaughtEvent = listener
                .getThrowableCaught();
        assertTrue(throwableCaughtEvent.getSource().equals(underTest));
        assertThat(throwableCaughtEvent.getThrowable().getMessage(),
                equalTo(errorMessage));
    }

}
