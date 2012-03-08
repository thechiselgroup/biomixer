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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class VerticalTreeLayoutComputation extends AbstractLayoutComputation {

    public VerticalTreeLayoutComputation(LayoutGraph graph, Executor executor,
            ErrorHandler errorHandler) {
        super(graph, executor, errorHandler);
    }

    @Override
    protected boolean computeIteration() throws RuntimeException {
        List<DirectedNodeNetwork> treesOnGraph = new NetworkBuilder().getNetworks(graph);
        assert treesOnGraph.size() >= 1;

        BoundsDouble graphBounds = graph.getBounds();

        // XXX if there is more than one tree this will give them each the same
        // width to work with. It might be better to take into account the
        // widths of the trees as well.
        double availableWidthForEachTree = graphBounds.getWidth()
                / treesOnGraph.size();

        for (int i = 0; i < treesOnGraph.size(); i++) {
            DirectedNodeNetwork tree = treesOnGraph.get(i);

            double verticalSpacing = graphBounds.getHeight()
                    / (tree.getNumberOfNodesOnLongestPath() + 1);
            double currentY = verticalSpacing;

            for (int j = 0; j < tree.getNumberOfNodesOnLongestPath(); j++) {
                List<NetworkNode> nodesAtDepth = tree.getNodesAtDistanceFromRoot(j);

                double horizontalSpacing = availableWidthForEachTree
                        / (nodesAtDepth.size() + 1);

                double currentX = i * availableWidthForEachTree
                        + horizontalSpacing;
                for (NetworkNode treeNode : nodesAtDepth) {
                    LayoutNode layoutNode = treeNode.getLayoutNode();
                    PointDouble topLeft = getTopLeftForCentreAt(currentX,
                            currentY, layoutNode);
                    layoutNode.setPosition(topLeft.getX(), topLeft.getY());
                    currentX += horizontalSpacing;
                }

                currentY += verticalSpacing;
            }

        }

        // this is not a continuous layout, it only needs the one iteration
        return false;
    }

}
