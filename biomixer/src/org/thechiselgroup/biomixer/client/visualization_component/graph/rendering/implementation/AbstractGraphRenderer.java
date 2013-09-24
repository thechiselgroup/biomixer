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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SquareSizeDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.ArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.GraphRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeExpanderRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * Manages construction and deletion of graph visualization elements. Also
 * applies styling to nodes and arcs.
 * 
 * @author drusk
 * 
 */
public abstract class AbstractGraphRenderer implements GraphRenderer {

    protected int graphWidth;

    protected int graphHeight;

    private NodeRenderer nodeRenderer;

    private ArcRenderer arcRenderer;

    private NodeExpanderRenderer nodeExpanderRenderer;

    private Map<Node, RenderedNode> renderedNodes = new HashMap<Node, RenderedNode>();

    private final TreeSet<Node> nodeSortedSet;

    private final TreeSet<Arc> arcSortedSet;

    private Map<String, RenderedNode> renderedNodesById = CollectionFactory
            .createStringMap();

    private Map<Arc, RenderedArc> renderedArcs = new HashMap<Arc, RenderedArc>();

    private Map<Node, RenderedNodeExpander> renderedNodeExpanders = new HashMap<Node, RenderedNodeExpander>();

    private boolean renderLabels = true;

    protected final NodeSizeTransformer nodeSizeTransformer;

    protected final ArcSizeTransformer arcSizeTransformer;

    /*
     * Keep track of any node currently in the process of being removed so that
     * concurrent modifications can be detected and avoided.
     */
    private Node nodeBeingRemoved = null;

    protected AbstractGraphRenderer(NodeRenderer nodeRenderer,
            ArcRenderer arcRenderer, NodeExpanderRenderer nodeExpanderRenderer,
            NodeSizeTransformer nodeSizeTransformer,
            ArcSizeTransformer arcSizeTransformer) {
        this.nodeRenderer = nodeRenderer;
        this.arcRenderer = arcRenderer;
        this.nodeExpanderRenderer = nodeExpanderRenderer;
        this.nodeSizeTransformer = nodeSizeTransformer;
        this.arcSizeTransformer = arcSizeTransformer;
        nodeSortedSet = new TreeSet<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if (o1.getSize() > o2.getSize()) {
                    return 1;
                } else if (o1.getSize() < o2.getSize()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        arcSortedSet = new TreeSet<Arc>(new Comparator<Arc>() {
            @Override
            public int compare(Arc o1, Arc o2) {
                if (o1.getWeight() > o2.getWeight()) {
                    return 1;
                } else if (o1.getWeight() < o2.getWeight()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        this.nodeSizeTransformer.addGraphRenderingListener(this);
        this.arcSizeTransformer.addGraphRenderingListener(this);

    }

    protected abstract void addArcToGraph(RenderedArc arc);

    protected abstract void addNodeExpanderToGraph(RenderedNodeExpander expander);

    protected abstract void addNodeToGraph(RenderedNode node);

    @Override
    public SizeDouble getGraphSize() {
        return new DefaultSizeDouble(graphWidth, graphHeight);
    }

    @Override
    public RenderedArc getRenderedArc(Arc arc) {
        return renderedArcs.get(arc);
    }

    @Override
    public RenderedNode getRenderedNode(Node node) {
        return renderedNodes.get(node);
    }

    @Override
    public RenderedNodeExpander getRenderedNodeExpander(Node node) {
        return renderedNodeExpanders.get(node);
    }

    @Override
    public void removeAllNodeExpanders() {
        for (RenderedNodeExpander renderedNodeExpander : renderedNodeExpanders
                .values()) {
            removeNodeExpanderFromGraph(renderedNodeExpander);
        }
        renderedNodeExpanders.clear();
    }

    @Override
    public void removeArc(Arc arc) {
        if (!renderedArcs.containsKey(arc)) {
            // Arcs can be removed when either endpoint is removed, so to
            // facilitate
            // easier access, I am checking here rather than above to see if the
            // arc is removable.
            // Who better to determine that than the renderer?
            return;
        }
        assert renderedArcs.containsKey(arc) : "Cannot remove an arc which has not been rendered";
        RenderedArc renderedArc = renderedArcs.get(arc);
        removeNodeConnections(renderedArc);
        renderedArcs.remove(arc);
        removeArcFromGraph(renderedArc);
        updateTransformedArcSizes(arc, false);
    }

    protected abstract void removeArcFromGraph(RenderedArc arc);

    private void removeConnectionIfNodeNotBeingRemoved(Node node,
            RenderedArc arc) {
        if (!node.equals(nodeBeingRemoved)) {
            renderedNodes.get(node).removeConnectedArc(arc);
        }
    }

    @Override
    public void setArcRenderLabels(boolean newValue) {
        boolean different = renderLabels == newValue;
        renderLabels = newValue;
        if (different) {
            for (RenderedArc arc : renderedArcs.values()) {
                arc.setLabelRendering(newValue);
            }
        }
    }

    @Override
    public boolean getArcRenderLabels() {
        return renderLabels;
    }

    @Override
    public void removeNode(Node node) {
        assert renderedNodes.containsKey(node) : "Cannot remove a node which has not been rendered";
        nodeBeingRemoved = node;
        RenderedNode renderedNode = renderedNodes.get(node);
        for (Iterator<RenderedArc> it = renderedNode.getConnectedArcs()
                .iterator(); it.hasNext();) {
            removeArc(it.next().getArc());
        }
        renderedNodes.remove(node);
        renderedNodesById.remove(node.getId());
        nodeSortedSet.remove(node);
        removeNodeFromGraph(renderedNode);
        updateTransformedNodeSizes(node, true);
        nodeBeingRemoved = null;
    }

    private void removeNodeConnections(RenderedArc arc) {
        removeConnectionIfNodeNotBeingRemoved(arc.getSource().getNode(), arc);
        removeConnectionIfNodeNotBeingRemoved(arc.getTarget().getNode(), arc);
    }

    @Override
    public void removeNodeExpander(RenderedNodeExpander expander) {
        assert renderedNodeExpanders.containsKey(expander.getNode());
        renderedNodeExpanders.remove(expander.getNode());
        removeNodeExpanderFromGraph(expander);
    }

    protected abstract void removeNodeExpanderFromGraph(
            RenderedNodeExpander expander);

    protected abstract void removeNodeFromGraph(RenderedNode node);

    @Override
    public RenderedArc renderArc(Arc arc) {
        assert !renderedArcs.containsKey(arc) : "Cannot render the same arc multiple times";
        RenderedNode renderedSource = renderedNodesById.get(arc
                .getSourceNodeId());
        RenderedNode renderedTarget = renderedNodesById.get(arc
                .getTargetNodeId());
        RenderedArc renderedArc = arcRenderer.createRenderedArc(arc,
                this.renderLabels, renderedSource, renderedTarget);
        renderedArcs.put(arc, renderedArc);
        addArcToGraph(renderedArc);
        updateTransformedArcSizes(arc, false);
        return renderedArc;
    }

    @Override
    public RenderedNode renderNode(Node node) {
        assert !renderedNodes.containsKey(node) : "Cannot render the same node multiple times";
        RenderedNode renderedNode = nodeRenderer.createRenderedNode(node);
        renderedNodes.put(node, renderedNode);
        renderedNodesById.put(node.getId(), renderedNode);
        nodeSortedSet.add(node);
        addNodeToGraph(renderedNode);
        updateTransformedNodeSizes(node, false);
        return renderedNode;
    }

    @Override
    public RenderedNodeExpander renderNodeExpander(PointDouble topLeftLocation,
            Set<String> expanderLabels, Node node) {
        RenderedNodeExpander expander = nodeExpanderRenderer
                .renderNodeExpander(topLeftLocation, expanderLabels, node);
        renderedNodeExpanders.put(expander.getNode(), expander);
        addNodeExpanderToGraph(expander);
        return expander;
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

        else if (styleProperty.equals(ArcSettings.ARC_HEAD)) {
            renderedArc.setArcHead(styleValue);
        }

        else if (styleProperty.equals(ArcSettings.ARC_THICKNESS)) {
            // renderedArc.setThickness(styleValue);
            try {
                double parsedThickness = Double.parseDouble(styleValue);
                arc.setWeight(parsedThickness);
                renderedArc.setThickness(arcSizeTransformer
                        .transform(parsedThickness));
            } catch (Exception e) {
                // This is for the transformation, which shouldn't have a
                // problem. Still could be double parse issues, which was never
                // handled
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setGraphHeight(int height) {
        this.graphHeight = height;
    }

    @Override
    public void setGraphWidth(int width) {
        this.graphWidth = width;
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

        else if (styleProperty.equals(GraphDisplay.NODE_SIZE)) {
            try {
                double parsedSize = Double.parseDouble(styleValue);
                node.setSize(parsedSize);
                renderedNode.setSize(nodeSizeTransformer
                        .transform(new SquareSizeDouble(parsedSize)));
            } catch (Exception e) {
                // This is for the transformation, which shouldn't have a
                // problem. Still could be double parse issues, which was never
                // handled
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateTransformedNodeSizes(Node changedNode, boolean removing) {
        boolean changed = false;
        if (removing) {
            changed = nodeSizeTransformer.removingScalingContextRange(
                    changedNode, nodeSortedSet);
        } else {
            changed = nodeSizeTransformer
                    .addingScalingContextRange(changedNode);
        }

        if (changed) {
            refreshAllNodeSizes();
        }
    }

    @Override
    public void updateTransformedArcSizes(Arc changedArc, boolean removing) {
        boolean changed = false;
        if (removing) {
            changed = arcSizeTransformer.removingScalingContextRange(
                    changedArc, arcSortedSet);
        } else {
            changed = arcSizeTransformer.addingScalingContextRange(changedArc);
        }

        if (changed) {
            refreshAllArcSizes();
        }
    }

    public void refreshAllNodeSizes() {
        for (Node node : renderedNodes.keySet()) {
            RenderedNode renderedNode = renderedNodes.get(node);
            try {
                renderedNode.setSize(nodeSizeTransformer
                        .transform(new SquareSizeDouble(node.getSize())));
            } catch (Exception e) {
                // Won't happen.
                e.printStackTrace();
            }
        }
    }

    public void refreshAllArcSizes() {
        for (Arc arc : renderedArcs.keySet()) {
            RenderedArc renderedArc = renderedArcs.get(arc);
            try {
                renderedArc.setThickness(arcSizeTransformer.transform(arc
                        .getWeight()));
            } catch (Exception e) {
                // Won't happen.
                e.printStackTrace();
            }
        }
    }

    public NodeSizeTransformer getNodeSizeTransformer() {
        return nodeSizeTransformer;
    }

    public ArcSizeTransformer getArcSizeTransformer() {
        return arcSizeTransformer;
    }
}
