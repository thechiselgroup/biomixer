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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

/**
 * Calculates the movements of graph nodes during iterations of the
 * force-directed layout. The force-directed layout is continuous; iterations
 * continue until some base condition (threshold value) is reached. State is
 * maintained between iterations.
 * 
 * @author drusk
 * 
 */
public class ForceDirectedLayoutComputation extends AbstractLayoutComputation {

    private double averageNodeDisplacementThreshold = 2.0;

    /*
     * Should be a number between 0 and 1.
     */
    private final double dampingConstant;

    private ForceCalculator forceCalculator;

    private int animationDuration;

    private LayoutNodeBoundsEnforcer boundsEnforcer;

    /*
     * The dampening factors for nodes are maintained individually.
     */
    private Map<LayoutNode, Double> nodeDampeningFactors = new HashMap<LayoutNode, Double>();

    public ForceDirectedLayoutComputation(ForceCalculator forceCalculator,
            double dampingConstant, LayoutGraph graph, Executor executor,
            ErrorHandler errorHandler, NodeAnimator nodeAnimator,
            int animationDuration) {
        super(graph, executor, errorHandler, nodeAnimator);
        this.forceCalculator = forceCalculator;
        this.dampingConstant = dampingConstant;
        this.boundsEnforcer = new LayoutNodeBoundsEnforcer(graph);
        initializeNodeDampening();
        this.animationDuration = animationDuration;
    }

    @Override
    protected boolean computeIteration() throws RuntimeException {
        double totalDisplacement = 0;
        List<LayoutNode> mobileNodes = graph.getUnanchoredNodes();
        for (LayoutNode currentNode : mobileNodes) {
            Vector2D netForce = new Vector2D(0, 0);
            for (LayoutNode otherNode : getAllNodesExcept(currentNode)) {
                netForce.add(forceCalculator.getForce(currentNode, otherNode));
            }
            Vector2D positionDelta = getPositionDelta(netForce, currentNode);
            updatePosition(positionDelta, currentNode);
            totalDisplacement += positionDelta.getMagnitude();
        }

        /*
         * Effect of dampening should increase with each iteration.
         */
        increaseDampeningForAllNodes();

        /*
         * Continue computing iterations until the average movement per node on
         * the graph is below a threshold value. Average movement is used so
         * that graphs with a lot of nodes moving tiny amounts don't stay above
         * the threshold longer than a graph with only few nodes moving the same
         * amount.
         */
        return totalDisplacement / mobileNodes.size() > averageNodeDisplacementThreshold;
    }

    /**
     * 
     * @param exceptNode
     *            the node to be excluded
     * @return a list of all nodes except for the specified one.
     */
    private List<LayoutNode> getAllNodesExcept(LayoutNode node) {
        List<LayoutNode> nodes = new ArrayList<LayoutNode>();
        nodes.addAll(graph.getAllNodes());
        nodes.remove(node);
        return nodes;
    }

    /**
     * Retrieves the dampening factor for a node. If the node has just recently
     * been added and has no existing dampening factor, it will be created
     * lazily here.
     * 
     * @param layoutNode
     *            the node to retrieve the dampening factor for
     * @return dampening factor
     */
    private double getDampening(LayoutNode layoutNode) {
        if (!nodeDampeningFactors.containsKey(layoutNode)) {
            nodeDampeningFactors.put(layoutNode, Double.valueOf(1.0));
        }
        return nodeDampeningFactors.get(layoutNode);
    }

    // XXX this should be moved elsewhere, duplicated from
    // BoundsAwareForceCalculator
    private double getOptimalEdgeLength() {
        return Math.sqrt(graph.getBounds().getArea()
                / graph.getAllNodes().size()) / 2;
    }

    /**
     * Calculates the change in position for a node due to a net force. Takes
     * into account the effects of dampening, and also makes sure that the node
     * doesn't go outside the graph's visible area.
     * 
     * @param netForce
     *            a force on a node which should cause a displacement
     * @param node
     *            the node the netForce has been applied to
     * @return the change in position for the node
     */
    private Vector2D getPositionDelta(Vector2D netForce, LayoutNode node) {
        /*
         * If nodes are very close together or very far apart the forces exerted
         * on them can be way too large. Currently to compensate for this, I
         * limit the displacement in the x and y directions for a node in one
         * iteration to be the 'optimalEdgeLength' (see {@link
         * BoundsAwareForceCalculator}). This is kind of arbitrary, and perhaps
         * more appropriate values could be found.
         */
        VectorUtils.limitVectorComponents(netForce, getOptimalEdgeLength());

        /*
         * Damping provides simulated annealing
         */
        Vector2D dampedDelta = netForce.scaleBy(getDampening(node));
        /*
         * Need to make sure the next calculated position doesn't cause all or
         * part of the node to go outside the visible graph area
         */
        return boundsEnforcer.getRestrictedDelta(node, dampedDelta);
    }

    /**
     * Increases the dampening of all the nodes on the graph by a factor of
     * <code>dampingConstant</code>. Note that increasing the dampening means
     * decreasing the numerical value of the dampening factor coefficient.
     */
    private void increaseDampeningForAllNodes() {
        for (Entry<LayoutNode, Double> entry : nodeDampeningFactors.entrySet()) {
            nodeDampeningFactors.put(entry.getKey(), entry.getValue()
                    * dampingConstant);
        }
    }

    /**
     * Start all nodes on the graph off with no damping.
     */
    private void initializeNodeDampening() {
        for (LayoutNode layoutNode : graph.getAllNodes()) {
            nodeDampeningFactors.put(layoutNode, Double.valueOf(1.0));
        }
    }

    /**
     * Updates the position of a node and adds animation to the movement.
     * 
     * @param position
     *            delta the displacement of the node
     * @param node
     *            the node to position.
     */
    private void updatePosition(Vector2D positionDelta, LayoutNode node) {
        animateTo(node,
                new PointDouble(node.getX() + positionDelta.getXComponent(),
                        node.getY() + positionDelta.getYComponent()),
                animationDuration);
    }
}
