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
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class ForceDirectedLayoutComputation extends AbstractLayoutComputation {

    /*
     * XXX once refactoring to separate LayoutNode from rendered node is done,
     * move this into an abstract layout node class. It will only need node2
     * passed in.
     */
    public static boolean areNodesConnected(LayoutNode node1, LayoutNode node2) {
        return getConnectedNodes(node1).contains(node2);
    }

    /*
     * XXX once refactoring to separate LayoutNode from rendered node is done,
     * move this into an abstract layout node class. It will not need any
     * parameters.
     */
    public static List<LayoutNode> getConnectedNodes(LayoutNode node) {
        List<LayoutNode> connectedNodes = new ArrayList<LayoutNode>();
        for (LayoutArc connectedArc : node.getConnectedArcs()) {
            connectedNodes.add(getNodeConnectedByArc(node, connectedArc));
        }
        return connectedNodes;
    }

    /*
     * XXX once refactoring to separate LayoutNode from rendered node is done,
     * move this into an abstract layout node class. It will only need the arc
     * passed in then of course.
     */
    public static LayoutNode getNodeConnectedByArc(LayoutNode currentNode,
            LayoutArc arc) {
        assert arc.getSourceNode().equals(currentNode)
                || arc.getTargetNode().equals(currentNode);
        return arc.getSourceNode().equals(currentNode) ? arc.getTargetNode()
                : arc.getSourceNode();
    }

    private double averageNodeDisplacementThreshold = 2.0;

    /*
     * Should be a number between 0 and 1.
     */
    private final double dampingConstant;

    private ForceCalculator forceCalculator;

    private LayoutNodeBoundsEnforcer boundsEnforcer;

    /*
     * The dampening factors for nodes are maintained individually.
     */
    private Map<LayoutNode, Double> nodeDampeningFactors = new HashMap<LayoutNode, Double>();

    public ForceDirectedLayoutComputation(ForceCalculator forceCalculator,
            double dampingConstant, LayoutGraph graph, Executor executor,
            ErrorHandler errorHandler) {
        super(graph, executor, errorHandler);
        this.forceCalculator = forceCalculator;
        this.dampingConstant = dampingConstant;
        this.boundsEnforcer = new LayoutNodeBoundsEnforcer(graph);
        initializeNodeDampening();
    }

    @Override
    protected boolean computeIteration() throws RuntimeException {
        double totalDisplacement = 0;
        List<LayoutNode> allNodes = graph.getAllNodes();
        for (LayoutNode currentNode : allNodes) {
            Vector2D netForce = new Vector2D(0, 0);
            for (LayoutNode otherNode : getAllNodesExcept(currentNode)) {
                netForce.add(forceCalculator.getForce(currentNode, otherNode));
            }

            updatePosition(netForce, currentNode);
            totalDisplacement += netForce.getMagnitude();
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
        return totalDisplacement / allNodes.size() > averageNodeDisplacementThreshold;
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
     * Updates the position of a node, but makes sure it stays within the graph
     * bounds.
     * 
     * @param netForce
     *            the force which determines the displacement of the node
     * @param node
     *            the node to position.
     */
    private void updatePosition(Vector2D netForce, LayoutNode node) {
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
        Vector2D positionDelta = netForce.scaleBy(getDampening(node));

        /*
         * Need to make sure the next calculated position doesn't cause all or
         * part of the node to go outside the visible graph area
         */
        PointDouble restrictedTopLeft = boundsEnforcer.getRestrictedPosition(
                node, node.getX() + positionDelta.getXComponent(), node.getY()
                        + positionDelta.getYComponent());

        node.setPosition(restrictedTopLeft);
    }
}
