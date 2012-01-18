package org.thechiselgroup.biomixer.client.core.error_handling;

public class UserFeedbackErrorHandler implements ErrorHandler {

    private final ThrowablesContainer throwablesContainer = new ThrowablesContainer();

    public void addListener(ThrowableEventListener listener) {
        throwablesContainer.addListener(listener);
    }

    @Override
    public void handleError(Throwable error) {
        throwablesContainer.addThrowableCaught(new ThrowableCaughtEvent(error,
                this));
    }

    public void removeListener(ThrowableEventListener listener) {
        throwablesContainer.removeListener(listener);
    }

}
