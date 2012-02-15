package org.thechiselgroup.biomixer.client.core.ui.widget.listbox;

import java.util.EventObject;

public class VisibilityChangeEvent extends EventObject {

    private final boolean visible;

    public VisibilityChangeEvent(boolean visible, Object source) {
        super(source);
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

}
