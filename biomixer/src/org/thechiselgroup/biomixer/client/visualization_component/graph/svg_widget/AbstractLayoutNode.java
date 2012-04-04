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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

public abstract class AbstractLayoutNode implements LayoutNode {

    @Override
    public PointDouble getCentre() {
        return new PointDouble(getX() + getSize().getWidth() / 2, getY()
                + getSize().getHeight() / 2);
    }

    public List<LayoutNode> getConnectedNodes() {
        List<LayoutNode> connectedNodes = new ArrayList<LayoutNode>();
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
    public boolean isConnectedTo(LayoutNode otherNode) {
        return getConnectedNodes().contains(otherNode);
    }

    @Override
    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    @Override
    public void setPosition(PointDouble position) {
        setPosition(position.getX(), position.getY());
    }

}
