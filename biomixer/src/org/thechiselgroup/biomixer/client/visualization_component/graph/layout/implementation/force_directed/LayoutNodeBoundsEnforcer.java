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

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Used to make sure that the next calculated position for a node doesn't cause
 * all or part of it to go off the visible graph area.
 * 
 * @author drusk
 * 
 */
public class LayoutNodeBoundsEnforcer {

    private LayoutGraph graph;

    public LayoutNodeBoundsEnforcer(LayoutGraph graph) {
        this.graph = graph;
    }

    /**
     * Returns the top left coordinates that should be used to make sure that
     * the entire node stays within the graph bounds while still being as close
     * to the requested position as possible.
     * 
     * @param node
     *            the node which is to be positioned
     * @param requestedLeftX
     *            the desired left x position, which may cause part of the node
     *            to be outside the graph
     * @param requestedTopY
     *            the desired top y position, which may cause part of the node
     *            to be outside the graph
     * @return the position of the top left corner of the node so that the whole
     *         node is still in the graph bounds
     */
    public PointDouble getRestrictedPosition(LayoutNode node,
            double requestedLeftX, double requestedTopY) {
        BoundsDouble bounds = graph.getBounds();
        SizeDouble nodeSize = node.getSize();

        double adjustedLeftX = requestedLeftX;
        double adjustedTopY = requestedTopY;

        if (requestedLeftX < bounds.getLeftX()) {
            adjustedLeftX = bounds.getLeftX();
        }
        if (requestedTopY < bounds.getTopY()) {
            adjustedTopY = bounds.getTopY();
        }
        if (requestedLeftX + nodeSize.getWidth() > bounds.getRightX()) {
            adjustedLeftX = bounds.getRightX() - nodeSize.getWidth();
        }
        if (requestedTopY + nodeSize.getHeight() > bounds.getBottomY()) {
            adjustedTopY = bounds.getBottomY() - nodeSize.getHeight();
        }

        return new PointDouble(adjustedLeftX, adjustedTopY);
    }
}
