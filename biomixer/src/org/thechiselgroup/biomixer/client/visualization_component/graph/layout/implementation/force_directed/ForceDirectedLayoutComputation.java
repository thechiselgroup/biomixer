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
import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
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

    private double energyThreshold = 0.5;

    private final double timeStep;

    private final double damping;

    private List<ForceNode> forceNodes = new ArrayList<ForceNode>();

    private ForceCalculator forceCalculator;

    public ForceDirectedLayoutComputation(ForceCalculator forceCalculator,
            double timeStep, double damping, LayoutGraph graph,
            Executor executor, ErrorHandler errorHandler) {
        super(graph, executor, errorHandler);
        this.forceCalculator = forceCalculator;
        this.timeStep = timeStep;
        this.damping = damping;
        initForceNodes();
    }

    @Override
    protected boolean computeIteration() throws RuntimeException {
        double kineticEnergy = 0;
        for (ForceNode currentNode : forceNodes) {
            Vector2D netForce = new Vector2D(0, 0);
            for (LayoutNode otherNode : graph.getAllNodes()) {
                netForce.add(forceCalculator.getForce(
                        currentNode.getLayoutNode(), otherNode));
            }

            currentNode.updateVelocity(netForce, timeStep, damping);
            currentNode.updatePosition(timeStep);

            kineticEnergy += currentNode.getKineticEnergy();
        }

        /*
         * Continue computing iterations until kinetic energy is close to 0
         * (within a small threshold value)
         */
        return kineticEnergy > energyThreshold;
    }

    private void initForceNodes() {
        for (LayoutNode unanchoredNode : graph.getUnanchoredNodes()) {
            forceNodes.add(new ForceNode(unanchoredNode));
        }
    }
}
