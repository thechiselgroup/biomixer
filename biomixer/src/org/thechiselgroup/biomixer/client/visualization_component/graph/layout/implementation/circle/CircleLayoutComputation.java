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
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.LayoutUtils;

public class CircleLayoutComputation extends AbstractLayoutComputation {

    private static double verticalPaddingPercent = 0.05;

    private static double horizontalPaddingPercent = 0.05;

    private final double minAngle;

    private final double maxAngle;

    private static final int animationDuration = 3000;

    public CircleLayoutComputation(double minAngle, double maxAngle,
            LayoutGraph graph, Executor executor, ErrorHandler errorHandler,
            AnimationRunner animationRunner) {
        super(graph, executor, errorHandler, animationRunner);
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
    }

    @Override
    protected boolean computeIteration() throws RuntimeException {
        List<LayoutNode> allNodes = graph.getAllNodes();

        assert allNodes.size() >= 1;

        // special case if there is just one node
        if (allNodes.size() == 1) {
            LayoutNode singleNode = allNodes.get(0);
            PointDouble topLeft = singleNode.getTopLeftForCentreAt(graph
                    .getBounds().getCentre());
            animateTo(singleNode, topLeft, animationDuration);
            return false;
        }

        double angleBetweenNodes = getAngleBetweenNodes(allNodes);

        // get radius
        BoundsDouble graphBounds = graph.getBounds();
        double graphWidth = graphBounds.getWidth();
        double graphHeight = graphBounds.getHeight();

        double radiusX = graphWidth / 2 - horizontalPaddingPercent * graphWidth
                - LayoutUtils.getMaxNodeWidth(allNodes) / 2;

        double radiusY = graphHeight / 2 - verticalPaddingPercent * graphHeight
                - LayoutUtils.getMaxNodeHeight(allNodes) / 2;

        // TODO: allow varying radius if radiusX and radiusY are not equal
        double radius = Math.min(radiusX, radiusY);

        for (int i = 0; i < allNodes.size(); i++) {
            LayoutNode layoutNode = allNodes.get(i);

            double nodeAngleRadians = Math.toRadians(minAngle + i
                    * angleBetweenNodes);
            double deltaXFromGraphCentre = radius * Math.sin(nodeAngleRadians);
            double deltaYFromGraphCentre = -radius * Math.cos(nodeAngleRadians);

            PointDouble graphCentre = graphBounds.getCentre();
            double x = graphCentre.getX() + deltaXFromGraphCentre;
            double y = graphCentre.getY() + deltaYFromGraphCentre;

            PointDouble topLeft = layoutNode.getTopLeftForCentreAt(x, y);
            animateTo(layoutNode, topLeft, animationDuration);
        }

        // this is not a continuous layout
        return false;
    }

    private double getAngleBetweenNodes(List<LayoutNode> allNodes) {
        double angleSpread = maxAngle - minAngle;
        if (angleSpread < 360.0) {
            /*
             * place a node at both maxAngle and minAngle because they normally
             * won't overlap
             */
            return angleSpread / (allNodes.size() - 1);
        } else {
            /*
             * do not place a node at both maxAngle and minAngle because they
             * definitely will overlap
             */
            return angleSpread / allNodes.size();
        }
    }

}
