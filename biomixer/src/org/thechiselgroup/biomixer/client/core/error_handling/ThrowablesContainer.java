package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.ArrayList;
import java.util.List;

public class ThrowablesContainer {

    private final List<ThrowableCaught> throwables = new ArrayList<ThrowableCaught>();

    public void addThrowableCaught(ThrowableCaught throwable) {
        throwables.add(throwable);
        // fire event
    }

    // event listeners

}
