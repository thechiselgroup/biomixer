package org.thechiselgroup.biomixer.client.core.util.animation;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimatable;

public class GwtNodeAnimationFactory implements NodeAnimationFactory {

    @Override
    public NodeAnimation createNodeAnimation(LayoutNodeAnimatable animatable) {
        return new GwtNodeAnimation(animatable);
    }

}
