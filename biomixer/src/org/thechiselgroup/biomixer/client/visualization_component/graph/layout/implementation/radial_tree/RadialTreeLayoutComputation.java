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
     * @param pointingUp
     *            Irrelevant for this subclass
     */
    protected RadialTreeLayoutComputation(LayoutGraph graph, Executor executor,
            ErrorHandler errorHandler, NodeAnimator nodeAnimator) {
        super(graph, executor, errorHandler, nodeAnimator);
    }

    @Override
    protected double getAvailableSecondaryDimensionForEachTree(
            int numDagsOnGraph) {
        // This is width or height for vertical or horizontal trees, but
        // needs to be azimuth or angular coordinate.
        // TODO This might be tricky with the inheritance I am trying to use,
        // but an angle is not the same as a width distance...it makes it harder
        // to increment it.
        // return graph.getBounds().getWidth() / numDagsOnGraph;
        return (Math.PI * 2) / numDagsOnGraph;
    }

    @Override
    protected double getPrimaryDimensionSpacing(int numberOfNodesOnLongestPath) {
        // This is height or width for vertical or horizontal trees, but
        // needs to be radius or radial coordinate.

        // Right now, this is radius...I think. But it gets
        // interpreted as y-axis.
        // TODO This gets applied as the y-axis. I was thinking waaay too
        // abstractly before. I will need to respond to this differentially for
        // the radial tree layout than in the regular tree layouts.

        // return graph.getBounds().getHeight() / (numberOfNodesOnLongestPath +
        // 1);
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
        // TODO Compute the coordinate for the node at the specified dimension
        // index.
        // TODO These are essentially position indices, right? And not on a
        // proper metric?
        return node.getTopLeftForCentreAt(currentSecondaryDimension,
                currentPrimaryDimension);
    }

}
