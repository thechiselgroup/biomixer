package org.thechiselgroup.biomixer.client.core.visualization;

import org.thechiselgroup.biomixer.shared.core.util.Condition;

public class ViewIsReadyCondition implements Condition {

    private final DefaultView view;

    public ViewIsReadyCondition(DefaultView view) {
        this.view = view;
    }

    @Override
    public boolean isMet() {
        return view.isReady();
    }

}