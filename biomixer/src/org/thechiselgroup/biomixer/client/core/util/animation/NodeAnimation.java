package org.thechiselgroup.biomixer.client.core.util.animation;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimatable;

public abstract class NodeAnimation implements Animation {

    private final LayoutNodeAnimatable animatable;

    private LayoutNode node;

    public NodeAnimation(LayoutNodeAnimatable animatable) {
        this.animatable = animatable;
        this.node = animatable.getLayoutNode();
    }

    @Override
    public void cancel() {
        /*
         * By default the node moves back to its original position if the
         * animation is cancelled. However, we want it to hold at its current
         * position.
         */
        double cancelledX = node.getX();
        double cancelledY = node.getY();

        doCancel();

        node.setPosition(cancelledX, cancelledY);
    }

    protected abstract void doCancel();

    protected abstract void doRun(int duration);

    protected void doUpdate(double progress) {
        animatable.update(progress);
    }

    @Override
    public void onUpdate(double progress) {
        doUpdate(progress);
    }

    @Override
    public void run(int duration) {
        doRun(duration);
    }

}
