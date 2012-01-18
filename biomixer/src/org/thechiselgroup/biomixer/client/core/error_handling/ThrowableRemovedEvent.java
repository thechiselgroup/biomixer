package org.thechiselgroup.biomixer.client.core.error_handling;

public class ThrowableRemovedEvent extends AbstractThrowableEvent {

    public ThrowableRemovedEvent(ThrowableCaught throwableCaught, Object source) {
        super(throwableCaught, source);
    }

}
