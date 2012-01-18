package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.EventObject;

public abstract class AbstractThrowableEvent extends EventObject {

    private final ThrowableCaught throwableCaught;

    protected AbstractThrowableEvent(ThrowableCaught throwableCaught,
            Object source) {
        super(source);
        this.throwableCaught = throwableCaught;
    }

    public ThrowableCaught getThrowableCaught() {
        return throwableCaught;
    }

}
