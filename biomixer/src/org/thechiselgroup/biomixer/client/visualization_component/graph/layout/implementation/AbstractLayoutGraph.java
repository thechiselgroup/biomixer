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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraphContentChangedEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraphContentChangedListener;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Provides functionality common to all implementations of the
 * {@link LayoutGraph}.
 * 
 * @author drusk
 * 
 */
public abstract class AbstractLayoutGraph implements LayoutGraph {

    private List<LayoutGraphContentChangedListener> contentChangedListeners = new ArrayList<LayoutGraphContentChangedListener>();

    @Override
    public void addContentChangedListener(
            LayoutGraphContentChangedListener listener) {
        this.contentChangedListeners.add(listener);
    }

    protected void fireLayoutGraphContentChangedEvent(
            LayoutGraphContentChangedEvent event) {
        for (LayoutGraphContentChangedListener listener : contentChangedListeners) {
            listener.onContentChanged(event);
        }
    }

    /**
     * 
     * @return the maximum x value used by a node on the graph.
     */
    public double getMaxNodeX() {
        double maxNodeX = 0;
        for (LayoutNode node : getAllNodes()) {
            double nodeRightX = node.getX() + node.getSize().getWidth();
            if (nodeRightX > maxNodeX) {
                maxNodeX = nodeRightX;
            }
        }
        return maxNodeX;
    }

    /**
     * 
     * @return the maximum x value used by a node on the graph.
     */
    public double getMaxNodeY() {
        double maxNodeY = 0;
        for (LayoutNode node : getAllNodes()) {
            double nodeBottomY = node.getY() + node.getSize().getHeight();
            if (nodeBottomY > maxNodeY) {
                maxNodeY = nodeBottomY;
            }
        }
        return maxNodeY;
    }

    @Override
    public BoundsDouble getNodeBounds() {
        double minX = Double.MAX_VALUE;
        double maxX = 0;
        double minY = Double.MAX_VALUE;
        double maxY = 0;
        for (LayoutNode layoutNode : getAllNodes()) {
            SizeDouble size = layoutNode.getSize();
            double nodeLeftX = layoutNode.getX();
            if (nodeLeftX < minX) {
                minX = nodeLeftX;
            }
            double nodeRightX = layoutNode.getX() + size.getWidth();
            if (nodeRightX > maxX) {
                maxX = nodeRightX;
            }
            double nodeTopY = layoutNode.getY();
            if (nodeTopY < minY) {
                minY = nodeTopY;
            }
            double nodeBottomY = layoutNode.getY() + size.getHeight();
            if (nodeBottomY > maxY) {
                maxY = nodeBottomY;
            }
        }
        return new DefaultBoundsDouble(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public List<LayoutNode> getUnanchoredNodes() {
        List<LayoutNode> unanchoredNodes = new ArrayList<LayoutNode>();
        for (LayoutNode layoutNode : getAllNodes()) {
            if (!layoutNode.isAnchored()) {
                unanchoredNodes.add(layoutNode);
            }
        }
        return unanchoredNodes;
    }

    public void shiftContentsHorizontally(int deltaX) {
        for (LayoutNode layoutNode : getAllNodes()) {
            layoutNode.setX(layoutNode.getX() + deltaX);
        }
    }

    public void shiftContentsVertically(int deltaY) {
        for (LayoutNode layoutNode : getAllNodes()) {
            layoutNode.setY(layoutNode.getY() + deltaY);
        }

    }

}
