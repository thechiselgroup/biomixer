package org.thechiselgroup.biomixer.client.core.error_handling;

public class UserFeedbackErrorHandler implements ErrorHandler {

    private final ThrowablesContainer throwablesContainer = new ThrowablesContainer();

    @Override
    public void handleError(Throwable error) {
        throwablesContainer.addThrowableCaught(new ThrowableCaught(error));
    }

}
