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

import java.util.HashMap;
import java.util.Map;

import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.ArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.GraphRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * Manages construction and deletion of graph visualization elements.
 * 
 * @author drusk
 * 
 */
public abstract class AbstractGraphRenderer implements GraphRenderer {

    private NodeRenderer nodeRenderer;

    private ArcRenderer arcRenderer;

    private Map<Node, RenderedNode> renderedNodes = new HashMap<Node, RenderedNode>();

    private Map<Arc, RenderedArc> renderedArcs = new HashMap<Arc, RenderedArc>();

    protected AbstractGraphRenderer(NodeRenderer nodeRenderer,
            ArcRenderer arcRenderer) {
        this.nodeRenderer = nodeRenderer;
        this.arcRenderer = arcRenderer;
    }

    protected abstract void addArcToGraph(RenderedArc arc);

    protected abstract void addNodeToGraph(RenderedNode node);

    @Override
    public void removeArc(Arc arc) {
        assert renderedArcs.containsKey(arc) : "Cannot remove an arc which has not been rendered";
        RenderedArc renderedArc = renderedArcs.get(arc);
        removeNodeConnections(renderedArc);
        renderedArcs.remove(arc);
        removeArcFromGraph(renderedArc);
    }

    protected abstract void removeArcFromGraph(RenderedArc arc);

    @Override
    public void removeNode(Node node) {
        assert renderedNodes.containsKey(node) : "Cannot remove a node which has not been rendered";
        RenderedNode renderedNode = renderedNodes.get(node);
        for (RenderedArc renderedArc : renderedNode.getConnectedArcs()) {
            removeArc(renderedArc.getArc());
        }
        renderedNodes.remove(node);
        removeNodeFromGraph(renderedNode);
    }

    private void removeNodeConnections(RenderedArc arc) {
        renderedNodes.get(arc.getSource().getNode()).removeConnectedArc(arc);
        renderedNodes.get(arc.getTarget().getNode()).removeConnectedArc(arc);
    }

    protected abstract void removeNodeFromGraph(RenderedNode node);

    @Override
    public RenderedArc renderArc(Arc arc, RenderedNode source,
            RenderedNode target) {
        assert !renderedArcs.containsKey(arc) : "Cannot render the same arc multiple times";
        RenderedArc renderedArc = arcRenderer.createRenderedArc(arc, source,
                target);
        renderedArcs.put(arc, renderedArc);
        return renderedArc;
    }

    @Override
    public RenderedNode renderNode(Node node) {
        assert !renderedNodes.containsKey(node) : "Cannot render the same node multiple times";
        RenderedNode renderedNode = nodeRenderer.createRenderedNode(node);
        renderedNodes.put(node, renderedNode);
        return renderedNode;
    }

    @Override
    public void setArcStyle(Arc arc, String styleProperty, String styleValue) {
        RenderedArc renderedArc = renderedArcs.get(arc);

        if (styleProperty.equals(ArcSettings.ARC_COLOR)) {
            renderedArc.setColor(styleValue);
        }

        else if (styleProperty.equals(ArcSettings.ARC_STYLE)) {
            renderedArc.setArcStyle(styleValue);
        }

        else if (styleProperty.equals(ArcSettings.ARC_THICKNESS)) {
            renderedArc.setThickness(styleValue);
        }
    }

    @Override
    public void setNodeStyle(Node node, String styleProperty, String styleValue) {
        RenderedNode renderedNode = renderedNodes.get(node);

        if (styleProperty.equals(GraphDisplay.NODE_BACKGROUND_COLOR)) {
            renderedNode.setBackgroundColor(styleValue);
        }

        else if (styleProperty.equals(GraphDisplay.NODE_FONT_COLOR)) {
            renderedNode.setFontColor(styleValue);
        }

        else if (styleProperty.equals(GraphDisplay.NODE_FONT_WEIGHT)) {
            renderedNode.setFontWeight(styleValue);
        }

        else if (styleProperty.equals(GraphDisplay.NODE_BORDER_COLOR)) {
            renderedNode.setBorderColor(styleValue);
        }
    }

}
