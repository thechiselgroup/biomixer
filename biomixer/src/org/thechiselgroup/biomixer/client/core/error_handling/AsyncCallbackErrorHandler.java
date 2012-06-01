package org.thechiselgroup.biomixer.client.core.error_handling;


import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncCallbackErrorHandler implements ErrorHandler {

    private final AsyncCallback<?> callback;

    public AsyncCallbackErrorHandler(AsyncCallback<?> callback) {
        assert callback != null;

        this.callback = callback;
    }

    @Override
    public void handleError(Throwable error) {
        callback.onFailure(error);
    }
}