package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.radial_tree;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.AbstractTreeLayoutComputation;

public class RadialTreeLayoutComputation extends AbstractTreeLayoutComputation {

    /**
     * 
     * 
     * @param graph
     * @param executor
     * @param errorHandler
     * @param nodeAnimator
     * @param leafsToCenter
     * @param pointingUp
     *            Irrelevant for this subclass
     */
    protected RadialTreeLayoutComputation(LayoutGraph graph, Executor executor,
            ErrorHandler errorHandler, NodeAnimator nodeAnimator,
            boolean leafsToCenter) {
        super(graph, executor, errorHandler, nodeAnimator, leafsToCenter, true);
    }

    @Override
    protected double getAvailableSecondaryDimensionForEachTree(
            int numDagsOnGraph) {
        // This is azimuth or angular coordinate.
        // return graph.getBounds().getWidth() / numDagsOnGraph;
        return (Math.PI * 2) / numDagsOnGraph;
    }

    @Override
    protected double getPrimaryDimensionSpacing(int numberOfNodesOnLongestPath) {
        // This is the radial coordinate.

        double smallerDimSize = Math.min(graph.getBounds().getHeight(), graph
                .getBounds().getWidth());
        // divide by 2 because we want radius not diameters
        return (smallerDimSize / 2.0) / (numberOfNodesOnLongestPath + 1)
        // + (smallerDimSize / 2.0)
        ;
    }

    @Override
    protected PointDouble getTopLeftForCentreAt(double currentPrimaryDimension,
            double currentSecondaryDimension, LayoutNode node) {
        return node.getTopLeftForCentreAt(currentSecondaryDimension,
                currentPrimaryDimension);
    }

}
