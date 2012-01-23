package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.ArrayList;
import java.util.List;

public class ThrowablesContainer {

    private final List<ThrowableCaught> throwables = new ArrayList<ThrowableCaught>();

    private final List<ThrowablesContainerEventListener> eventListeners = new ArrayList<ThrowablesContainerEventListener>();

    public void addListener(ThrowablesContainerEventListener listener) {
        eventListeners.add(listener);
    }

    public void addThrowableCaught(ThrowableCaught throwableCaught) {
        throwables.add(throwableCaught);
        fireThrowableAddedEvent(new ThrowableCaughtEvent(throwableCaught, this));
    }

    private void fireThrowableAddedEvent(ThrowableCaughtEvent event) {
        for (ThrowablesContainerEventListener listener : eventListeners) {
            listener.onThrowableCaughtAdded(event);
        }
    }

    private void fireThrowableRemovedEvent(ThrowableCaughtEvent event) {
        for (ThrowablesContainerEventListener listener : eventListeners) {
            listener.onThrowableCaughtRemoved(event);
        }
    }

    public List<ThrowableCaught> getThrowablesCaught() {
        return throwables;
    }

    public void removeListener(ThrowablesContainerEventListener listener) {
        eventListeners.remove(listener);
    }

    public void removeThrowableCaught(ThrowableCaught throwableCaught) {
        assert throwables.contains(throwableCaught);

        throwables.remove(throwableCaught);
        fireThrowableRemovedEvent(new ThrowableCaughtEvent(throwableCaught,
                this));
    }
}
