package org.thechiselgroup.biomixer.client.core.util.animation;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimatable;

public class NullNodeAnimation extends NodeAnimation {

    public NullNodeAnimation(LayoutNodeAnimatable animatable) {
        super(animatable);
    }

    @Override
    protected void doCancel() {
        // nothing to cancel
    }

    @Override
    protected void doRun(int duration) {
        doUpdate(1d);
    }

}
