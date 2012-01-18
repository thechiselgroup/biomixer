package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.Date;
import java.util.EventObject;

public class ThrowableCaughtEvent extends EventObject {

    private final Date timeStamp;

    private final Throwable throwable;

    // maybe some additional information

    public ThrowableCaughtEvent(Throwable throwable, Object source) {
        super(source);
        this.timeStamp = new Date();
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

}
