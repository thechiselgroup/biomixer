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

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;

public class IdentifiableLayoutNode extends AbstractLayoutNode implements
        Identifiable {

    private RenderedNode renderedNode;

    private boolean isAnchored = false;

    private final LayoutNodeType nodeType;

    private List<LayoutArc> connectedArcs = new ArrayList<LayoutArc>();

    private final String id;

    public IdentifiableLayoutNode(String id, RenderedNode svgComponent,
            LayoutNodeType nodeType) {
        this.id = id;
        this.renderedNode = svgComponent;
        this.nodeType = nodeType;
    }

    public void addConnectedArc(IdentifiableLayoutArc layoutArc) {
        connectedArcs.add(layoutArc);
        renderedNode.addConnectedArc(layoutArc.getRenderedArc());
    }

    @Override
    public List<LayoutArc> getConnectedArcs() {
        return connectedArcs;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public SizeDouble getLabelSize() {
        // TODO support for external labels
        return new DefaultSizeDouble(0, 0);
    }

    @Override
    public double getLabelX() {
        // TODO support for external labels
        return Double.NaN;
    }

    @Override
    public double getLabelY() {
        // TODO support for external labels
        return Double.NaN;
    }

    public RenderedNode getRenderedNode() {
        return renderedNode;
    }

    @Override
    public SizeDouble getSize() {
        return renderedNode.getSize();
    }

    @Override
    public LayoutNodeType getType() {
        return nodeType;
    }

    @Override
    public double getX() {
        return renderedNode.getLeftX();
    }

    @Override
    public double getY() {
        return renderedNode.getTopY();
    }

    @Override
    public boolean hasLabel() {
        // currently label has to be in node
        return false;
    }

    @Override
    public boolean isAnchored() {
        return isAnchored;
    }

    @Override
    public void setAnchored(boolean anchored) {
        this.isAnchored = anchored;
    }

    @Override
    public void setLabelPosition(double x, double y) {
        // TODO support for external labels
    }

    @Override
    public void setLabelX(double x) {
        // TODO support for external labels
    }

    @Override
    public void setLabelY(double y) {
        // TODO support for external labels
    }

    @Override
    public void setX(double x) {
        if (!isAnchored) {
            renderedNode.setLeftX(x);
        }
    }

    @Override
    public void setY(double y) {
        if (!isAnchored) {
            renderedNode.setTopY(y);
        }
    }

}
