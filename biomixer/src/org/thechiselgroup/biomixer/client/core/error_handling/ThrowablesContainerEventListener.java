package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.EventListener;

public interface ThrowablesContainerEventListener extends EventListener {

    void onThrowableCaughtAdded(ThrowableCaughtEvent event);

    void onThrowableCaughtRemoved(ThrowableRemovedEvent event);

}
