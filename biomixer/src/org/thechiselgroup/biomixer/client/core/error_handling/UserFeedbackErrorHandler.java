package org.thechiselgroup.biomixer.client.core.error_handling;

public class UserFeedbackErrorHandler implements ErrorHandler {

    private final ThrowablesContainer throwablesContainer = new ThrowablesContainer();

    public void addListener(ThrowablesContainerEventListener listener) {
        throwablesContainer.addListener(listener);
    }

    @Override
    public void handleError(Throwable error) {
        throwablesContainer.addThrowableCaught(new ThrowableCaught(error));
    }

    public void removeListener(ThrowablesContainerEventListener listener) {
        throwablesContainer.removeListener(listener);
    }

}
