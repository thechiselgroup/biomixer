package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.Date;

public class ThrowableCaught {

    private final Date timeStamp;

    private final Throwable throwable;

    // maybe some additional information

    public ThrowableCaught(Throwable throwable) {
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
