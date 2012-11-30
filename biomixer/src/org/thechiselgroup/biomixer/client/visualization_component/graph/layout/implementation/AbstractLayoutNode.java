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

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

public abstract class AbstractLayoutNode implements LayoutNode {

    protected boolean isAnchored = false;

    protected double x = Double.NaN;

    protected double y = Double.NaN;

    protected LightweightList<LayoutArc> connectedArcs = CollectionFactory
            .createLightweightList();

    public void addConnectedArc(LayoutArc arc) {
        connectedArcs.add(arc);
    }

    @Override
    public PointDouble getCentre() {
        return new PointDouble(getX() + getSize().getWidth() / 2, getY()
                + getSize().getHeight() / 2);
    }

    @Override
    public LightweightList<LayoutArc> getConnectedArcs() {
        return connectedArcs;
    }

    public LightweightList<LayoutNode> getConnectedNodes() {
        LightweightList<LayoutNode> connectedNodes = CollectionFactory
                .createLightweightList();
        for (LayoutArc connectedArc : getConnectedArcs()) {
            connectedNodes.add(getNodeConnectedByArc(connectedArc));
        }
        return connectedNodes;
    }

    public LayoutNode getNodeConnectedByArc(LayoutArc arc) {
        assert arc.getSourceNode().equals(this)
                || arc.getTargetNode().equals(this);
        return arc.getSourceNode().equals(this) ? arc.getTargetNode() : arc
                .getSourceNode();
    }

    @Override
    public PointDouble getTopLeftForCentreAt(double x, double y) {
        SizeDouble size = getSize();
        return new PointDouble(x - size.getWidth() / 2, y - size.getHeight()
                / 2);
    }

    @Override
    public PointDouble getTopLeftForCentreAt(PointDouble centre) {
        return getTopLeftForCentreAt(centre.getX(), centre.getY());
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public boolean isAnchored() {
        return isAnchored;
    }

    @Override
    public boolean isConnectedTo(LayoutNode otherNode) {
        return getConnectedNodes().contains(otherNode);
    }

    protected boolean isRealChange(double x, double y) {
        return !isAnchored && (this.x != x || this.y != y);
    }

    @Override
    public void setAnchored(boolean anchored) {
        this.isAnchored = anchored;
    }

    @Override
    public void setPosition(double x, double y) {
        if (isRealChange(x, y)) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void setPosition(PointDouble position) {
        setPosition(position.getX(), position.getY());
    }

}
