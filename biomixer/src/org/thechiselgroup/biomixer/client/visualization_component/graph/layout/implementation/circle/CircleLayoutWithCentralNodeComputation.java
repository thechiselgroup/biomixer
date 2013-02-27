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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.circle;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.IdentifiableLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.IdentifiableLayoutNode;

public class CircleLayoutWithCentralNodeComputation extends
        CircleLayoutComputation {

    private String centralNodeUri;

    public CircleLayoutWithCentralNodeComputation(double minAngle,
            double maxAngle, IdentifiableLayoutGraph graph, Executor executor,
            ErrorHandler errorHandler, NodeAnimator nodeAnimator,
            String centralNodeUri) {
        super(minAngle, maxAngle, graph, executor, errorHandler, nodeAnimator);
        this.centralNodeUri = centralNodeUri;
    }

    @Override
    protected boolean computeIteration() throws RuntimeException {
        List<LayoutNode> allNodes = graph.getAllNodes();

        assert allNodes.size() >= 1;

        // Identify the central node
        LayoutNode centralLayoutNode = null;
        for (LayoutNode node : allNodes) {
            if (((IdentifiableLayoutNode) node).getId().equals(centralNodeUri)) {
                centralLayoutNode = node;
                break;
            }
        }

        // special case if there is just one node
        if (allNodes.size() == 1) {
            LayoutNode singleNode = allNodes.get(0);
            PointDouble topLeft = singleNode.getTopLeftForCentreAt(graph
                    .getBounds().getCentre());
            animateTo(singleNode, topLeft, animationDuration);
            return false;
        }

        if (null != centralLayoutNode) {
            // Get the central node out of the shell mix...but don't modify the
            // data
            // from the graph! It's all exposed!
            List<LayoutNode> allOtherNodes = new ArrayList<LayoutNode>();
            allOtherNodes.addAll(allNodes);
            allOtherNodes.remove(centralLayoutNode);
            allNodes = allOtherNodes;
        }

        placeNodes(allNodes);

        if (null != centralLayoutNode) {
            // We placed all the other nodes, now place the central node.
            PointDouble topLeft = centralLayoutNode.getTopLeftForCentreAt(graph
                    .getBounds().getCentre());
            animateTo(centralLayoutNode, topLeft, animationDuration);
        }

        // this is not a continuous layout
        return false;
    }

}
