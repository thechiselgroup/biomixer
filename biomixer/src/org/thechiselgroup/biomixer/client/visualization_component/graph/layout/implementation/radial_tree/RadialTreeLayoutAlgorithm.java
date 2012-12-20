package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.radial_tree;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.executor.DirectExecutor;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class RadialTreeLayoutAlgorithm extends AbstractLayoutAlgorithm {

    private Executor executor = new DirectExecutor();

    private ErrorHandler errorHandler;

    private final NodeAnimator nodeAnimator;

    public RadialTreeLayoutAlgorithm(ErrorHandler errorHandler,
            NodeAnimator nodeAnimator) {
        this.errorHandler = errorHandler;
        this.nodeAnimator = nodeAnimator;
    }

    @Override
    protected AbstractLayoutComputation getLayoutComputation(LayoutGraph graph) {
        return new RadialTreeLayoutComputation(graph, executor, errorHandler,
                nodeAnimator);
    }

}
