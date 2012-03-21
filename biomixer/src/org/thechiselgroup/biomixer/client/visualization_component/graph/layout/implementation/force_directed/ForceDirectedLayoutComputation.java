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
        List<LayoutNode> allNodes = graph.getAllNodes();

        for (ForceNode currentNode : forceNodes) {
            LayoutNode layoutNode = currentNode.getLayoutNode();
            Vector2D force = new Vector2D(0, 0);

            for (LayoutNode otherNode : allNodes) {
                force.add(forceCalculator.getRepulsionForce(layoutNode,
                        otherNode));
            }

            for (LayoutArc arc : layoutNode.getConnectedArcs()) {
                force.add(forceCalculator.getAttractionForce(layoutNode, arc));
            }

            currentNode.updateVelocity(force, timeStep, damping);
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
