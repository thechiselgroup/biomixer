package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.executor.DirectExecutor;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;

public class ForceDirectedLayoutAlgorithm implements LayoutAlgorithm {

    private Executor executor = new DirectExecutor();

    private ErrorHandler errorHandler;

    public ForceDirectedLayoutAlgorithm(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public LayoutComputation computeLayout(LayoutGraph graph) {
        ForceDirectedLayoutComputation computation = new ForceDirectedLayoutComputation(
                graph, executor, errorHandler);
        computation.run();
        return computation;
    }
}
