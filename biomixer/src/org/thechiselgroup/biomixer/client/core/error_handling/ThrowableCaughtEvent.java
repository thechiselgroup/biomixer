package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.EventObject;

public class ThrowableCaughtEvent extends EventObject {

    private final ThrowableCaught throwableCaught;

    public ThrowableCaughtEvent(ThrowableCaught throwableCaught, Object source) {
        super(source);
        this.throwableCaught = throwableCaught;
    }

    public ThrowableCaught getThrowableCaught() {
        return throwableCaught;
    }

}
