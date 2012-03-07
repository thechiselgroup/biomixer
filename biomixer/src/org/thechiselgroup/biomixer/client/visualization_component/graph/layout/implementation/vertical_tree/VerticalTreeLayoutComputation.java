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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
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
        List<Tree> treesOnGraph = new TreeFactory().getTrees(graph);
        assert treesOnGraph.size() >= 1;

        BoundsDouble graphBounds = graph.getBounds();

        // XXX if there is more than one tree this will give them each the same
        // width to work with. It might be better to take into account the
        // widths of the trees as well.
        double availableWidthForEachTree = graphBounds.getWidth()
                / treesOnGraph.size();

        for (int i = 0; i < treesOnGraph.size(); i++) {
            Tree tree = treesOnGraph.get(i);

            double verticalSpacing = graphBounds.getHeight()
                    / (tree.getHeight() + 1);
            double currentY = verticalSpacing;

            for (int j = 0; j < tree.getHeight(); j++) {
                List<TreeNode> nodesAtDepth = tree.getNodesAtDepth(j);

                double horizontalSpacing = availableWidthForEachTree
                        / (nodesAtDepth.size() + 1);

                double currentX = i * availableWidthForEachTree
                        + horizontalSpacing;
                for (TreeNode treeNode : nodesAtDepth) {
                    LayoutNode layoutNode = treeNode.getLayoutNode();
                    layoutNode.setX(getLeftXForCentreAt(currentX, layoutNode));
                    layoutNode.setY(getTopYForCentreAt(currentY, layoutNode));
                    currentX += horizontalSpacing;
                }

                currentY += verticalSpacing;
            }

        }

        // this is not a continuous layout, it only needs the one iteration
        return false;
    }

    // Might be useful for other layouts such as horizontal tree
    private double getLeftXForCentreAt(double x, LayoutNode node) {
        return x - node.getSize().getWidth() / 2;
    }

    // Might be useful for other layouts such as horizontal tree
    private double getTopYForCentreAt(double y, LayoutNode node) {
        return y - node.getSize().getHeight() / 2;
    }

}
