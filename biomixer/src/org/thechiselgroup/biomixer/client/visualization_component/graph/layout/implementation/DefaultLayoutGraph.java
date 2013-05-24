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

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.BoundsDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraphContentChangedEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNodeType;

import com.google.gwt.user.client.ui.Widget;

public class DefaultLayoutGraph extends AbstractLayoutGraph {

    private double minX = 0;

    private double minY = 0;

    private double width = Double.NaN;

    private double height = Double.NaN;

    private List<LayoutNode> layoutNodes = new ArrayList<LayoutNode>();

    private List<LayoutArc> layoutArcs = new ArrayList<LayoutArc>();

    private List<DefaultLayoutNodeType> layoutNodeTypes = new ArrayList<DefaultLayoutNodeType>();

    private List<DefaultLayoutArcType> layoutArcTypes = new ArrayList<DefaultLayoutArcType>();

    private Widget graphWidget;

    public DefaultLayoutGraph(Widget graphWidget, double width, double height) {
        this(graphWidget, 0, 0, width, height);
    }

    public DefaultLayoutGraph(Widget graphWidget, double minX, double minY,
            double width, double height) {
        this.graphWidget = graphWidget;
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;
    }

    /**
     * Adds a arc to the layout graph. Adds the <code>LayoutArcType</code> for
     * that arc if it is not already present.
     * 
     * @param arc
     *            the arc to be added to the graph
     */
    public void addLayoutArc(LayoutArc arc) {
        LayoutArcType type = arc.getType();
        if (!layoutArcTypes.contains(type)) {
            layoutArcTypes.add((DefaultLayoutArcType) type);
        }
        layoutArcs.add(arc);
        fireLayoutGraphContentChangedEvent(new LayoutGraphContentChangedEvent(
                this));
    }

    public void addLayoutArcType(DefaultLayoutArcType arcType) {
        layoutArcTypes.add(arcType);
    }

    /**
     * Adds a node to the layout graph. Adds the <code>LayoutNodeType</code> for
     * that node if it is not already present.
     * 
     * @param node
     *            the node to be added to the graph
     */
    public void addLayoutNode(LayoutNode node) {
        DefaultLayoutNodeType type = (DefaultLayoutNodeType) node.getType();
        if (!layoutNodeTypes.contains(type)) {
            layoutNodeTypes.add(type);
        }
        layoutNodes.add(node);
        fireLayoutGraphContentChangedEvent(new LayoutGraphContentChangedEvent(
                this));
    }

    public void addLayoutNodeType(DefaultLayoutNodeType nodeType) {
        layoutNodeTypes.add(nodeType);
    }

    @Override
    public List<LayoutArc> getAllArcs() {
        return layoutArcs;
    }

    @Override
    public List<LayoutNode> getAllNodes() {
        return layoutNodes;
    }

    @Override
    public List<LayoutArcType> getArcTypes() {
        List<LayoutArcType> types = new ArrayList<LayoutArcType>();
        for (LayoutArcType type : layoutArcTypes) {
            types.add(type);
        }
        return types;
    }

    @Override
    public BoundsDouble getBounds() {
        return new DefaultBoundsDouble(minX, minY, width, height);
    }

    @Override
    public List<LayoutNodeType> getNodeTypes() {
        List<LayoutNodeType> types = new ArrayList<LayoutNodeType>();
        for (LayoutNodeType type : layoutNodeTypes) {
            types.add(type);
        }
        return types;
    }

    public void removeLayoutArc(LayoutArc arc) {
        DefaultLayoutArcType type = (DefaultLayoutArcType) arc.getType();
        assert layoutArcTypes.contains(type);
        type.remove(arc);
        layoutArcs.remove(arc);
        fireLayoutGraphContentChangedEvent(new LayoutGraphContentChangedEvent(
                this));
    }

    public void removeLayoutNode(LayoutNode node) {
        DefaultLayoutNodeType type = (DefaultLayoutNodeType) node.getType();
        assert layoutNodeTypes.contains(type);
        type.remove(node);
        layoutNodes.remove(node);
        fireLayoutGraphContentChangedEvent(new LayoutGraphContentChangedEvent(
                this));
    }

    public void setHeight(double height) {
        this.height = height;
    }

    // XXX not currently used
    public void setMinX(double minX) {
        this.minX = minX;
    }

    // XXX not currently used
    public void setMinY(double minY) {
        this.minY = minY;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    @Override
    public Widget getGraphWidget() {
        return graphWidget;
    }
}
