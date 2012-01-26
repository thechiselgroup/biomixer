package org.thechiselgroup.biomixer.client.core.error_handling;

public class ThrowablesContainerErrorHandler implements ErrorHandler {

    private final ThrowablesContainer throwablesContainer;

    public ThrowablesContainerErrorHandler(
            ThrowablesContainer throwablesContainer) {
        this.throwablesContainer = throwablesContainer;
    }

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
