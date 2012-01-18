package org.thechiselgroup.biomixer.client.core.error_handling;

public class ThrowableCaughtEvent extends AbstractThrowableEvent {

    public ThrowableCaughtEvent(ThrowableCaught throwableCaught, Object source) {
        super(throwableCaught, source);
    }

}
