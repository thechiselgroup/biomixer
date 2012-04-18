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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

public abstract class AbstractRenderedNode implements RenderedNode {

    protected List<RenderedArc> connectedArcs = new ArrayList<RenderedArc>();

    private Node node;

    protected AbstractRenderedNode(Node node) {
        this.node = node;
    }

    @Override
    public void addConnectedArc(RenderedArc arc) {
        connectedArcs.add(arc);
    }

    @Override
    public PointDouble getCentre() {
        return new PointDouble(getLeftX() + getSize().getWidth() / 2, getTopY()
                + getSize().getHeight() / 2);
    }

    @Override
    public List<RenderedArc> getConnectedArcs() {
        return connectedArcs;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public PointDouble getTopLeft() {
        return new PointDouble(getLeftX(), getTopY());
    }

    @Override
    public String getType() {
        return node.getType();
    }

    @Override
    public void removeConnectedArc(RenderedArc arc) {
        connectedArcs.remove(arc);
    }

    protected void updateConnectedArcs() {
        for (RenderedArc arc : connectedArcs) {
            arc.update();
        }
    }

}
