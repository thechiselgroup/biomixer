package org.thechiselgroup.biomixer.client.core.util.animation;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimatable;

import com.google.gwt.animation.client.Animation;

public class GwtNodeAnimation extends NodeAnimation {

    private Animation gwtAnimation;

    public GwtNodeAnimation(LayoutNodeAnimatable animatable) {
        super(animatable);
        init();
    }

    @Override
    protected void doCancel() {
        gwtAnimation.cancel();
    }

    @Override
    protected void doRun(int duration) {
        gwtAnimation.run(duration);
    }

    private void init() {
        gwtAnimation = new Animation() {

            @Override
            protected void onUpdate(double progress) {
                doUpdate(progress);
            }
        };

    }

}
