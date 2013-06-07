/*******************************************************************************
 * Copyright 2012 David Rusk 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree;

import java.util.List;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

/**
 * The computations for the {@link VerticalTreeLayoutAlgorith} and
 * {@link HorizontalTreeLayoutAlgorithm} are basically the same except for their
 * orientation. This class contains the common functionality, and has template
 * methods for any calculations specific to the vertical or horizontal tree.
 * 
 * @author drusk
 * 
 */
public abstract class AbstractTreeLayoutComputation extends
        AbstractLayoutComputation {

    private static final int animationDuration = 1000;

    private final boolean reversed;

    private final boolean radial;

    /**
     * Traversing the dag normally results in a vertical tree with arrows
     * pointing up or horizontal tree with arrows pointing left. To get a
     * vertical tree with arrows pointing down, or a horizontal tree with arrows
     * pointing right, set <code>reversed</code> to true. To make the absolutely
     * different radial tree, with either root or leaves in the center, and the
     * rest spanned out in rings, pass in <code>radial</code> set as true;
     */
    protected AbstractTreeLayoutComputation(LayoutGraph graph,
            Executor executor, ErrorHandler errorHandler,
            NodeAnimator nodeAnimator, boolean reversed, boolean radial) {
        super(graph, executor, errorHandler, nodeAnimator);
        this.reversed = reversed;
        this.radial = radial;
    }

    @Override
    protected boolean computeIteration() throws RuntimeException {
        /*
         * XXX need to handle the case where there are cycles in the graph.
         */
        CycleDetector cycleDetector = new CycleDetector(graph);

        /* Anchor the nodes in cycles */
        for (LayoutNode node : cycleDetector.getNodesInCycles()) {
            node.setAnchored(true);
        }

        /*
         * Build up the directed acyclic graph structure from the remaining
         * nodes
         */
        List<DirectedAcyclicGraph> dagsOnGraph = new DirectedAcyclicGraphBuilder()
                .getDirectedAcyclicGraphs(graph);
        int numDagsOnGraph = dagsOnGraph.size();
        assert numDagsOnGraph >= 1 || cycleDetector.hasCycles();

        double availableSecondaryDimensionForEachTree = getAvailableSecondaryDimensionForEachTree(numDagsOnGraph
                + cycleDetector.getNumberOfCycles());

        // PointDouble viewCenter = getViewCenter();
        // This didn't work because the values are virtually identical...but
        // maybe not necessarily so.
        PointDouble viewCenter = getRenderedViewCenter();
        // traverse each dag
        for (int i = 0; i < dagsOnGraph.size(); i++) {
            DirectedAcyclicGraph dag = dagsOnGraph.get(i);

            int treeDepth = dag.getNumberOfNodesOnLongestPath();
            // We have to make the path count extended for the case of a
            // set of dags with no common root. In addition, more below...
            int numberOfRoots = dag.getRoots().size();
            boolean radialTreeSkipCentralPosition = false;
            if (!reversed && radial && i == 0 && numberOfRoots > 1) {
                radialTreeSkipCentralPosition = true;
                treeDepth += 1;
            } else if (reversed && radial && i == dagsOnGraph.size() - 1
                    && numberOfRoots == 1) {
                radialTreeSkipCentralPosition = true;
                treeDepth += 1;
            }
            double primaryDimensionSpacing = getPrimaryDimensionSpacing(treeDepth);
            double currentPrimaryDimension = primaryDimensionSpacing;

            /*
             * We have to traverse from the center outwards for the radial case.
             * We will complete each layer of this 2D onion as we go.
             */
            if (radial && !reversed) {
                if (radialTreeSkipCentralPosition) {
                    currentPrimaryDimension += primaryDimensionSpacing;
                }
                boolean topNode = true;
                for (int j = 0; j < dag.getNumberOfNodesOnLongestPath(); j++) {
                    processNodesAlongSecondaryDimensionRadially(i, j,
                            dagsOnGraph.size(), dag,
                            availableSecondaryDimensionForEachTree,
                            currentPrimaryDimension, viewCenter,
                            radialTreeSkipCentralPosition, topNode);
                    topNode = false;
                    currentPrimaryDimension += primaryDimensionSpacing;
                }
            } else if (radial && reversed) {
                if (radialTreeSkipCentralPosition) {
                    currentPrimaryDimension += primaryDimensionSpacing;
                }
                boolean topNode = true;
                for (int j = dag.getNumberOfNodesOnLongestPath() - 1; j >= 0; j--) {
                    processNodesAlongSecondaryDimensionRadially(i, j,
                            dagsOnGraph.size(), dag,
                            availableSecondaryDimensionForEachTree,
                            currentPrimaryDimension, viewCenter,
                            radialTreeSkipCentralPosition, topNode);
                    topNode = false;
                    currentPrimaryDimension += primaryDimensionSpacing;
                }
            } else
            /*
             * Traverse primary dimension, but process along secondary dimension
             * at each step. For a vertical tree that is not reversed, this will
             * be like a typewriter's movement.
             */
            if (reversed) {
                for (int j = dag.getNumberOfNodesOnLongestPath() - 1; j >= 0; j--) {
                    processNodesAlongSecondaryDimension(i, j, dag,
                            availableSecondaryDimensionForEachTree,
                            currentPrimaryDimension);
                    currentPrimaryDimension += primaryDimensionSpacing;
                }
            } else {
                for (int j = 0; j < dag.getNumberOfNodesOnLongestPath(); j++) {
                    processNodesAlongSecondaryDimension(i, j, dag,
                            availableSecondaryDimensionForEachTree,
                            currentPrimaryDimension);
                    currentPrimaryDimension += primaryDimensionSpacing;
                }
            }
        }

        /* Unanchor the nodes in cycles */
        for (LayoutNode node : cycleDetector.getNodesInCycles()) {
            node.setAnchored(false);
        }

        /* Place the nodes in cycles */
        List<Set<LayoutNode>> cycles = cycleDetector.getCycles();
        for (int i = 0; i < cycles.size(); i++) {
            Set<LayoutNode> cycle = cycles.get(i);

            /*
             * XXX place these nodes in a straight line along the primary
             * dimension
             */
            double secondaryDimension = availableSecondaryDimensionForEachTree
                    * (numDagsOnGraph + i + 0.5);
            double primaryDimensionSpacing = getPrimaryDimensionSpacing(cycle
                    .size());
            double currentPrimaryDimension = primaryDimensionSpacing;

            for (LayoutNode node : cycle) {
                PointDouble topLeft = getTopLeftForCentreAt(
                        currentPrimaryDimension, secondaryDimension, node);
                animateTo(node, topLeft, animationDuration);
                currentPrimaryDimension += primaryDimensionSpacing;
            }
        }

        // this is not a continuous layout
        return false;
    }

    /**
     * This gives the graph limits, which are not necessarily the same as the
     * browser rendered limits. But it appears to be the same...
     * 
     * @see AbstractTreeLayoutComputation#getRenderedViewCenter()
     * @return
     */
    private PointDouble getViewCenter() {
        return graph.getBounds().getCentre();
    }

    private PointDouble getRenderedViewCenter() {
        return new PointDouble(graph.getGraphWidget().getElement()
                .getClientWidth() / 2, graph.getGraphWidget().getElement()
                .getClientHeight() / 2);
    }

    protected abstract double getAvailableSecondaryDimensionForEachTree(
            int numDagsOnGraph);

    protected abstract double getPrimaryDimensionSpacing(
            int numberOfNodesOnLongestPath);

    protected abstract PointDouble getTopLeftForCentreAt(
            double currentPrimaryDimension, double currentSecondaryDimension,
            LayoutNode node);

    private void processNodesAlongSecondaryDimension(int i, int j,
            DirectedAcyclicGraph dag,
            double availableSecondaryDimensionForEachTree,
            double currentPrimaryDimension) {

        List<DirectedAcyclicGraphNode> nodesAtDepth = dag
                .getNodesAtDistanceFromRoot(j);

        double secondaryDimensionSpacing = availableSecondaryDimensionForEachTree
                / (nodesAtDepth.size() + 1);

        double currentSecondaryDimension = i
                * availableSecondaryDimensionForEachTree
                + secondaryDimensionSpacing;

        for (DirectedAcyclicGraphNode dagNode : nodesAtDepth) {
            LayoutNode layoutNode = dagNode.getLayoutNode();
            PointDouble topLeft = getTopLeftForCentreAt(
                    currentPrimaryDimension, currentSecondaryDimension,
                    layoutNode);
            animateTo(layoutNode, topLeft, animationDuration);
            currentSecondaryDimension += secondaryDimensionSpacing;
        }

    }

    private void processNodesAlongSecondaryDimensionRadially(int i, int j,
            int numDags, DirectedAcyclicGraph dag,
            double availableSecondaryDimensionForEachTree,
            double currentPrimaryDimension, PointDouble viewCenter,
            boolean radialTreeSkipCentralPosition, boolean topNode) {

        int maxDepth = dag.getNumberOfNodesOnLongestPath();

        // Rename these while leaving args named for caller
        double radianSlicePerTree = availableSecondaryDimensionForEachTree;

        // TODO I feel like it could improve things to change the radiusDepth
        // from being based on the current position to being based on the
        // previous level's actual position radius (of some node therein). The
        // number of nodes on the outside should affect the radius (which maybe
        // it doesn't right now). This would determine the radius...I need to
        // rework this.
        double radiusDepth = currentPrimaryDimension
                * ((topNode && numDags == 1 && !radialTreeSkipCentralPosition) ? 0
                        : 1);

        // TODO This is also vital to the radial tree version
        // We need to interpret the availableSecondaryDimensionForEachTree as
        // angle, and currentPrimaryDimension as radius distance.
        List<DirectedAcyclicGraphNode> nodesAtDepth = dag
                .getNodesAtDistanceFromRoot(j);

        double radianSlicePerNode = radianSlicePerTree / (nodesAtDepth.size());

        double currentRadianPosition = i * radianSlicePerTree
                + radianSlicePerNode;

        for (DirectedAcyclicGraphNode dagNode : nodesAtDepth) {
            LayoutNode layoutNode = dagNode.getLayoutNode();

            // Compute (x,y) from (radiusDepth,currentRadianPosition)
            PointDouble coord = polarToCartesian(radiusDepth,
                    currentRadianPosition);

            PointDouble topLeft = getTopLeftForCentreAt(coord.getX(),
                    coord.getY(), layoutNode);
            topLeft = topLeft.plus(viewCenter);
            animateTo(layoutNode, topLeft, animationDuration);
            currentRadianPosition += radianSlicePerNode;
        }
    }

    private final PointDouble polarToCartesian(double radius, double azimuth) {
        double x = Math.cos(azimuth) * radius;
        double y = Math.sin(azimuth) * radius;
        PointDouble coords = new PointDouble(x, y);
        return coords;
    }
}
