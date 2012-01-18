package org.thechiselgroup.biomixer.client.core.error_handling;

import com.google.gwt.event.shared.EventHandler;

public interface AddRemoveThrowableEventHandler extends EventHandler {

    void onAddThrowable();

    void onRemoveThrowable();

}
