package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.Date;

public class ThrowableCaught {

    private final Date timeStamp;

    private final Throwable throwable;

    // maybe some additional information

    // TODO test constructor where you can set the date
    // & use it in the unit tests
    public ThrowableCaught(Throwable throwable) {
        this.timeStamp = new Date();
        this.throwable = throwable;
    }

    // @Override
    // public boolean equals(Object other) {
    // return other instanceof ThrowableCaught
    // && timeStamp.equals(((ThrowableCaught) other).getTimeStamp())
    // && throwable.equals(((ThrowableCaught) other).getThrowable());
    // }

    public Throwable getThrowable() {
        return throwable;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return timeStamp.toString() + ": " + throwable.getLocalizedMessage();
    }

}
