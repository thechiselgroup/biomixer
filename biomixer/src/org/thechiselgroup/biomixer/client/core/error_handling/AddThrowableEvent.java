package org.thechiselgroup.biomixer.client.core.error_handling;

import com.google.gwt.event.shared.GwtEvent;

public class AddThrowableEvent extends GwtEvent<AddRemoveThrowableEventHandler> {

    @Override
    protected void dispatch(AddRemoveThrowableEventHandler handler) {
        // TODO Auto-generated method stub
        handler.onAddThrowable();
    }

    @Override
    public Type getAssociatedType() {
        // TODO Auto-generated method stub
        return null;
    }

}
