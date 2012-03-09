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

import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class CircleLayoutComputation extends AbstractLayoutComputation {

    private static double verticalPaddingPercent = 0.05;

    private static double horizontalPaddingPercent = 0.05;

    public CircleLayoutComputation(LayoutGraph graph, Executor executor,
            ErrorHandler errorHandler) {
        super(graph, executor, errorHandler);
    }

    @Override
    protected boolean computeIteration() throws RuntimeException {

        List<LayoutNode> allNodes = graph.getAllNodes();
        double angleBetweenNodes = 360.0 / allNodes.size();

        // get radius
        BoundsDouble graphBounds = graph.getBounds();
        double graphWidth = graphBounds.getWidth();
        double graphHeight = graphBounds.getHeight();

        double radiusX = graphWidth / 2 - horizontalPaddingPercent * graphWidth
                - getMaxNodeWidth(allNodes) / 2;

        double radiusY = graphHeight / 2 - verticalPaddingPercent * graphHeight
                - getMaxNodeHeight(allNodes) / 2;

        // TODO: allow varying radius if radiusX and radiusY are not equal
        double radius = Math.min(radiusX, radiusY);

        for (int i = 0; i < allNodes.size(); i++) {
            LayoutNode layoutNode = allNodes.get(i);

            double nodeAngleRadians = Math.toRadians(i * angleBetweenNodes);
            double deltaXFromGraphCentre = radius * Math.sin(nodeAngleRadians);
            double deltaYFromGraphCentre = -radius * Math.cos(nodeAngleRadians);

            PointDouble graphCentre = getGraphCentre();
            double x = graphCentre.getX() + deltaXFromGraphCentre;
            double y = graphCentre.getY() + deltaYFromGraphCentre;

            PointDouble topLeft = getTopLeftForCentreAt(x, y, layoutNode);
            layoutNode.setPosition(topLeft.getX(), topLeft.getY());
        }

        // this is not a continuous layout
        return false;
    }

    private double getMaxNodeHeight(List<LayoutNode> nodes) {
        double maxHeight = 0.0;
        for (LayoutNode layoutNode : nodes) {
            double height = layoutNode.getSize().getHeight();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }
        return maxHeight;
    }

    private double getMaxNodeWidth(List<LayoutNode> nodes) {
        double maxWidth = 0.0;
        for (LayoutNode layoutNode : nodes) {
            double width = layoutNode.getSize().getWidth();
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }
}
