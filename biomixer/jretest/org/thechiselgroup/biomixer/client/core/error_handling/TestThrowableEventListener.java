package org.thechiselgroup.biomixer.client.core.error_handling;


public class TestThrowableEventListener implements ThrowableEventListener {

    public enum Status {
        NOTIFIED, NOT_NOTIFIED
    }

    private Status status = Status.NOT_NOTIFIED;

    private ThrowableCaughtEvent event;

    public Status getStatus() {
        return status;
    }

    public ThrowableCaughtEvent getThrowableCaught() {
        if (event == null) {
            return null;
        }
        return event;
    }

    @Override
    public void notifyOfThrowableEvent(ThrowableCaughtEvent event) {
        this.event = event;
        status = Status.NOTIFIED;
    }

}
