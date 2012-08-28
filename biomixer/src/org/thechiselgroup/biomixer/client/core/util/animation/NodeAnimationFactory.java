package org.thechiselgroup.biomixer.client.core.util.animation;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.LayoutNodeAnimatable;

public interface NodeAnimationFactory {

    NodeAnimation createNodeAnimation(LayoutNodeAnimatable animatable);

}
