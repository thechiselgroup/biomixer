package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class ForceDirectedLayoutComputation extends AbstractLayoutComputation {

    public ForceDirectedLayoutComputation(LayoutGraph graph, Executor executor,
            ErrorHandler errorHandler) {
        super(graph, executor, errorHandler);
    }

    @Override
    protected boolean computeIteration() throws RuntimeException {
        BoundsDouble graphBounds = graph.getBounds();

        // XXX try running test layout loader with this commented out, then
        // again with it in

        // List<LayoutNode> allNodes = graph.getAllNodes();
        // for (LayoutNode layoutNode : allNodes) {
        // layoutNode.setX(0);
        // }

        // TODO place nodes

        // not continuous for now
        return false;
    }

}
