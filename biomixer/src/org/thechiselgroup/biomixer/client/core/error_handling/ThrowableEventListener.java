package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.EventListener;

public interface ThrowableEventListener extends EventListener {

    void notifyOfThrowableEvent(ThrowableCaughtEvent event);

}
