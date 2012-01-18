package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.ArrayList;
import java.util.List;

public class ThrowablesContainer {

    private final List<ThrowableCaughtEvent> throwables = new ArrayList<ThrowableCaughtEvent>();

    private final List<ThrowableEventListener> eventListeners = new ArrayList<ThrowableEventListener>();

    public void addListener(ThrowableEventListener listener) {
        eventListeners.add(listener);
    }

    public void addThrowableCaught(ThrowableCaughtEvent throwableCaughtEvent) {
        throwables.add(throwableCaughtEvent);
        fireEvent(throwableCaughtEvent);
    }

    private void fireEvent(ThrowableCaughtEvent event) {
        for (ThrowableEventListener listener : eventListeners) {
            listener.notifyOfThrowableEvent(event);
        }
    }

    public List<ThrowableCaughtEvent> getThrowablesCaught() {
        return throwables;
    }

    public void removeListener(ThrowableEventListener listener) {
        eventListeners.remove(listener);
    }

}
